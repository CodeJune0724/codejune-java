#include "wgc_capture.h"
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Graphics.Capture.h>
#include <cstring>
#include <thread>

template <typename T>
static winrt::com_ptr<T> GetDXGIInterfaceFromObject(
    winrt::Windows::Foundation::IInspectable const& object) {
    auto access = object.as<
        ::Windows::Graphics::DirectX::Direct3D11::IDirect3DDxgiInterfaceAccess>();
    winrt::com_ptr<T> result;
    winrt::check_hresult(
        access->GetInterface(winrt::guid_of<T>(), result.put_void()));
    return result;
}

bool WGCCapture::Init(HWND hwnd) {
    m_hwnd = hwnd;
    m_stopping = false;
    // 1. 创建 D3D11 设备
    D3D_FEATURE_LEVEL featureLevels[] = {
        D3D_FEATURE_LEVEL_11_1, D3D_FEATURE_LEVEL_11_0
    };
    UINT flags = D3D11_CREATE_DEVICE_BGRA_SUPPORT;
    HRESULT hr = D3D11CreateDevice(
        nullptr, D3D_DRIVER_TYPE_HARDWARE, nullptr,
        flags, featureLevels, ARRAYSIZE(featureLevels),
        D3D11_SDK_VERSION,
        m_d3dDevice.GetAddressOf(), nullptr, m_d3dContext.GetAddressOf());
    if (FAILED(hr)) return false;
    // 2. 查询 IDXGIDevice
    ComPtr<IDXGIDevice> dxgiDevice;
    hr = m_d3dDevice.As(&dxgiDevice);
    if (FAILED(hr)) return false;
    // 3. 包装成 WinRT 的 IDirect3DDevice
    winrt::com_ptr<::IInspectable> inspectableDevice;
    hr = CreateDirect3D11DeviceFromDXGIDevice(
        dxgiDevice.Get(), inspectableDevice.put());
    if (FAILED(hr)) return false;
    auto winrtDevice = inspectableDevice.as<
        winrt::Windows::Graphics::DirectX::Direct3D11::IDirect3DDevice>();

    // ========= 重点修改：CreateForWindow，不再直接check_hresult抛异常 =========
    auto interopFactory = winrt::get_activation_factory<
        GraphicsCaptureItem, IGraphicsCaptureItemInterop>();

    hr = interopFactory->CreateForWindow(
        hwnd, winrt::guid_of<GraphicsCaptureItem>(),
        winrt::put_abi(m_captureItem));

    // 这里就会捕获 RPC_S_CALL_FAILED(0x800706BA)，CEF沙箱窗口会走到这里失败返回false
    if (FAILED(hr))
    {
        // 这里可以输出日志 hr值，便于定位就是CreateForWindow失败
        return false;
    }

    auto size = m_captureItem.Size();
    // 5. 创建帧池（使用 item 的 Size，这是初始尺寸）
    m_framePool = Direct3D11CaptureFramePool::CreateFreeThreaded(
        winrtDevice,
        winrt::Windows::Graphics::DirectX::DirectXPixelFormat::B8G8R8A8UIntNormalized,
        2, size);
    // 6. 注册回调，保存 token
    m_frameArrivedToken = m_framePool.FrameArrived(
        [this](Direct3D11CaptureFramePool const& sender,
               winrt::Windows::Foundation::IInspectable const&) {
            if (m_stopping.load()) return;
            m_callbackCount.fetch_add(1);
            try {
                OnFrameArrived(sender);
            } catch (...) {
                // 回调内部异常绝不外泄
            }
            m_callbackCount.fetch_sub(1);
        });
    // 7. 创建会话并启动
    m_session = m_framePool.CreateCaptureSession(m_captureItem);
    m_session.IsCursorCaptureEnabled(false);
    m_session.StartCapture();
    return true;
}

void WGCCapture::OnFrameArrived(Direct3D11CaptureFramePool const& sender) {
    auto frame = sender.TryGetNextFrame();
    if (!frame) return;

    auto texture = GetDXGIInterfaceFromObject<ID3D11Texture2D>(frame.Surface());

    // 获取内容尺寸（这是真正想要的尺寸）
    auto contentSize = frame.ContentSize();
    int contentWidth  = contentSize.Width;
    int contentHeight = contentSize.Height;
    if (contentWidth <= 0 || contentHeight <= 0) return;

    // 获取纹理尺寸（可能因 padding 大于内容尺寸）
    D3D11_TEXTURE2D_DESC srcDesc = {};
    texture->GetDesc(&srcDesc);

    std::lock_guard<std::mutex> lock(m_frameMutex);

    // 如果 staging texture 不存在，或者纹理尺寸发生变化，则重建
    if (!m_stagingTexture
        || m_textureWidth  != (int)srcDesc.Width
        || m_textureHeight != (int)srcDesc.Height) {

        D3D11_TEXTURE2D_DESC desc = {};
        desc.Width = srcDesc.Width;
        desc.Height = srcDesc.Height;
        desc.MipLevels = 1;
        desc.ArraySize = 1;
        desc.Format = srcDesc.Format;
        desc.SampleDesc.Count = 1;
        desc.Usage = D3D11_USAGE_STAGING;
        desc.CPUAccessFlags = D3D11_CPU_ACCESS_READ;

        HRESULT hrc = m_d3dDevice->CreateTexture2D(
            &desc, nullptr, m_stagingTexture.ReleaseAndGetAddressOf());
        if (FAILED(hrc)) return;

        m_textureWidth  = srcDesc.Width;
        m_textureHeight = srcDesc.Height;
    }

    // 更新内容尺寸（可能窗口大小已变化）
    m_frameWidth  = contentWidth;
    m_frameHeight = contentHeight;

    m_d3dContext->CopyResource(m_stagingTexture.Get(), texture.get());

    D3D11_MAPPED_SUBRESOURCE mapped = {};
    if (FAILED(m_d3dContext->Map(
            m_stagingTexture.Get(), 0, D3D11_MAP_READ, 0, &mapped))) {
        return;
    }

    // 使用内容尺寸来分配输出缓冲区
    const size_t requiredSize =
        static_cast<size_t>(m_frameWidth) * m_frameHeight * 4;
    if (m_latestFrame.size() != requiredSize) {
        m_latestFrame.resize(requiredSize);
    }

    // 逐行拷贝，只拷贝内容区域（跳过纹理可能存在的右侧/底部 padding）
    for (int y = 0; y < m_frameHeight; y++) {
        memcpy(
            m_latestFrame.data() + static_cast<size_t>(y) * m_frameWidth * 4,
            static_cast<uint8_t*>(mapped.pData)
                + static_cast<size_t>(y) * mapped.RowPitch,
            static_cast<size_t>(m_frameWidth) * 4);
    }
    m_d3dContext->Unmap(m_stagingTexture.Get(), 0);

    m_frameCv.notify_all();
}

bool WGCCapture::GetFrame(int& outWidth, int& outHeight, void** outData) {
    std::unique_lock<std::mutex> lock(m_frameMutex);

    // 等待首帧
    if (m_latestFrame.empty()) {
        if (!m_frameCv.wait_for(
                lock, std::chrono::seconds(2),
                [this] { return !m_latestFrame.empty() || m_stopping.load(); })) {
            return false;
        }
        if (m_stopping.load()) return false;
    }

    // 拷贝到输出缓冲，容量复用，尺寸不变时零分配
    m_outputFrame.assign(m_latestFrame.begin(), m_latestFrame.end());

    outWidth  = m_frameWidth;
    outHeight = m_frameHeight;
    *outData  = m_outputFrame.data();
    return true;
}

int WGCCapture::GetWidth() {
    std::lock_guard<std::mutex> lock(m_frameMutex);
    return m_frameWidth;
}

int WGCCapture::GetHeight() {
    std::lock_guard<std::mutex> lock(m_frameMutex);
    return m_frameHeight;
}

void WGCCapture::Stop() {
    m_stopping.store(true);
    m_frameCv.notify_all();

    try {
        if (m_framePool && m_frameArrivedToken.value) {
            m_framePool.FrameArrived(m_frameArrivedToken);
            m_frameArrivedToken = {};
        }
    } catch (...) {}

    for (int i = 0; i < 100 && m_callbackCount.load() > 0; ++i) {
        std::this_thread::sleep_for(std::chrono::milliseconds(5));
    }

    try { if (m_session)   { m_session.Close();   m_session = nullptr; } } catch (...) {}
    try { if (m_framePool) { m_framePool.Close(); m_framePool = nullptr; } } catch (...) {}
    try { m_captureItem = nullptr; } catch (...) {}
    try { m_stagingTexture.Reset(); } catch (...) {}
    try { m_d3dContext.Reset(); } catch (...) {}
    try { m_d3dDevice.Reset(); } catch (...) {}
}

WGCCapture::~WGCCapture() {
    Stop();
}
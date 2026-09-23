#include "wgc_capture.h"
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Graphics.Capture.h>
#include <cstring>
#include <thread>

template <typename T>
static winrt::com_ptr<T> GetDXGIInterfaceFromObject(
    winrt::Windows::Foundation::IInspectable const& object)
{
    auto access = object.as<::Windows::Graphics::DirectX::Direct3D11::IDirect3DDxgiInterfaceAccess>();
    winrt::com_ptr<T> result;
    winrt::check_hresult(
        access->GetInterface(winrt::guid_of<T>(), result.put_void()));
    return result;
}

bool WGCCapture::Init(HWND hwnd)
{
    m_hwnd = hwnd;
    m_stopping = false;

    D3D_FEATURE_LEVEL featureLevels[] = {
        D3D_FEATURE_LEVEL_11_1,
        D3D_FEATURE_LEVEL_11_0
    };
    UINT flags = D3D11_CREATE_DEVICE_BGRA_SUPPORT;

    HRESULT hr = D3D11CreateDevice(
        nullptr,
        D3D_DRIVER_TYPE_HARDWARE,
        nullptr,
        flags,
        featureLevels,
        ARRAYSIZE(featureLevels),
        D3D11_SDK_VERSION,
        m_d3dDevice.GetAddressOf(),
        nullptr,
        m_d3dContext.GetAddressOf());

    if (FAILED(hr))
    {
        hr = D3D11CreateDevice(
            nullptr,
            D3D_DRIVER_TYPE_WARP,
            nullptr,
            flags,
            featureLevels,
            ARRAYSIZE(featureLevels),
            D3D11_SDK_VERSION,
            m_d3dDevice.GetAddressOf(),
            nullptr,
            m_d3dContext.GetAddressOf());
        if (FAILED(hr))
        {
            return false;
        }
    }

    ComPtr<IDXGIDevice> dxgiDevice;
    hr = m_d3dDevice.As(&dxgiDevice);
    if (FAILED(hr))
    {
        return false;
    }

    winrt::com_ptr<::IInspectable> inspectableDevice;
    hr = CreateDirect3D11DeviceFromDXGIDevice(dxgiDevice.Get(), inspectableDevice.put());
    if (FAILED(hr))
    {
        return false;
    }

    auto winrtDevice = inspectableDevice.as<winrt::Windows::Graphics::DirectX::Direct3D11::IDirect3DDevice>();
    m_winrtDevice = winrtDevice;

    auto interopFactory = winrt::get_activation_factory<GraphicsCaptureItem, IGraphicsCaptureItemInterop>();

    hr = interopFactory->CreateForWindow(
        hwnd,
        winrt::guid_of<GraphicsCaptureItem>(),
        winrt::put_abi(m_captureItem));
    if (FAILED(hr))
    {
        return false;
    }

    auto size = m_captureItem.Size();
    m_poolWidth  = size.Width;
    m_poolHeight = size.Height;

    m_framePool = Direct3D11CaptureFramePool::CreateFreeThreaded(
        winrtDevice,
        winrt::Windows::Graphics::DirectX::DirectXPixelFormat::B8G8R8A8UIntNormalized,
        2,
        size);

    m_frameArrivedToken = m_framePool.FrameArrived(
        [this](Direct3D11CaptureFramePool const& sender,
               winrt::Windows::Foundation::IInspectable const&) {
            m_callbackCount.fetch_add(1, std::memory_order_acquire);
            if (!m_stopping.load(std::memory_order_acquire))
            {
                try
                {
                    OnFrameArrived(sender);
                }
                catch (...)
                {
                }
            }
            m_callbackCount.fetch_sub(1, std::memory_order_release);
        });

    try
    {
        m_session = m_framePool.CreateCaptureSession(m_captureItem);
        // Win10‑1903 MAINSTA下访问IsCursorCaptureEnabled抛异常，注释；默认光标关闭
        // m_session.IsCursorCaptureEnabled(false);
        m_session.StartCapture();
    }
    catch (...)
    {
        return false;
    }

    return true;
}

void WGCCapture::OnFrameArrived(Direct3D11CaptureFramePool const& sender)
{
    auto frame = sender.TryGetNextFrame();
    if (!frame)
    {
        return;
    }

    auto frameCleanup = [&](void*){
        if(frame)
        {
            frame.Close();
        }
    };
    std::unique_ptr<void, decltype(frameCleanup)> frameScope((void*)1, frameCleanup);

    auto contentSize = frame.ContentSize();
    int contentWidth  = contentSize.Width;
    int contentHeight = contentSize.Height;
    if (contentWidth <= 0 || contentHeight <= 0)
    {
        return;
    }

    if (contentWidth != m_poolWidth || contentHeight != m_poolHeight)
    {
        try
        {
            m_framePool.Recreate(
                m_winrtDevice,
                winrt::Windows::Graphics::DirectX::DirectXPixelFormat::B8G8R8A8UIntNormalized,
                2,
                contentSize);
            m_poolWidth  = contentWidth;
            m_poolHeight = contentHeight;
        }
        catch (...)
        {
        }
        return;
    }

    auto texture = GetDXGIInterfaceFromObject<ID3D11Texture2D>(frame.Surface());
    D3D11_TEXTURE2D_DESC srcDesc = {};
    texture->GetDesc(&srcDesc);

    if (contentWidth  > (int)srcDesc.Width || contentHeight > (int)srcDesc.Height)
    {
        return;
    }

    std::lock_guard<std::mutex> lock(m_frameMutex);
    if (m_stopping.load())
    {
        return;
    }

    if (!m_stagingTexture
        || m_textureWidth  != (int)srcDesc.Width
        || m_textureHeight != (int)srcDesc.Height)
    {
        D3D11_TEXTURE2D_DESC desc = {};
        desc.Width  = srcDesc.Width;
        desc.Height = srcDesc.Height;
        desc.MipLevels = 1;
        desc.ArraySize = 1;
        desc.Format = srcDesc.Format;
        desc.SampleDesc.Count = 1;
        desc.Usage = D3D11_USAGE_STAGING;
        desc.CPUAccessFlags = D3D11_CPU_ACCESS_READ;

        HRESULT hrc = m_d3dDevice->CreateTexture2D(
            &desc, nullptr, m_stagingTexture.ReleaseAndGetAddressOf());
        if (FAILED(hrc))
        {
            return;
        }
        m_textureWidth  = srcDesc.Width;
        m_textureHeight = srcDesc.Height;
    }

    m_d3dContext->CopyResource(m_stagingTexture.Get(), texture.get());

    D3D11_MAPPED_SUBRESOURCE mapped = {};
    if (FAILED(m_d3dContext->Map(m_stagingTexture.Get(), 0, D3D11_MAP_READ, 0, &mapped)))
    {
        return;
    }

    const size_t requiredSize = static_cast<size_t>(contentWidth) * contentHeight * 4;
    std::vector<uint8_t> newFrame;
    try
    {
        newFrame.resize(requiredSize);
    }
    catch (...)
    {
        m_d3dContext->Unmap(m_stagingTexture.Get(), 0);
        return;
    }

    for (int y = 0; y < contentHeight; y++)
    {
        memcpy(
            newFrame.data() + static_cast<size_t>(y) * contentWidth * 4,
            static_cast<uint8_t*>(mapped.pData) + static_cast<size_t>(y) * mapped.RowPitch,
            static_cast<size_t>(contentWidth) * 4);
    }
    m_d3dContext->Unmap(m_stagingTexture.Get(), 0);

    m_latestFrame.swap(newFrame);
    m_frameWidth  = contentWidth;
    m_frameHeight = contentHeight;
    m_frameCv.notify_all();
}

bool WGCCapture::GetFrame(int& outWidth, int& outHeight, void** outData)
{
    std::unique_lock<std::mutex> lock(m_frameMutex);
    if (m_latestFrame.empty())
    {
        if (!m_frameCv.wait_for(
                lock, std::chrono::seconds(2),
                [this] { return !m_latestFrame.empty() || m_stopping.load(); }))
        {
            return false;
        }
        if (m_stopping.load())
        {
            return false;
        }
        if (m_latestFrame.empty())
        {
            return false;
        }
    }

    const size_t expected = static_cast<size_t>(m_frameWidth) * m_frameHeight * 4;
    if (m_frameWidth <= 0 || m_frameHeight <= 0 || m_latestFrame.size() != expected)
    {
        return false;
    }

    m_outputFrame.assign(m_latestFrame.begin(), m_latestFrame.end());
    m_outputWidth  = m_frameWidth;
    m_outputHeight = m_frameHeight;

    outWidth  = m_outputWidth;
    outHeight = m_outputHeight;
    *outData  = m_outputFrame.data();
    return true;
}

int WGCCapture::GetWidth()
{
    std::lock_guard<std::mutex> lock(m_frameMutex);
    return m_outputWidth;
}

int WGCCapture::GetHeight()
{
    std::lock_guard<std::mutex> lock(m_frameMutex);
    return m_outputHeight;
}

void WGCCapture::Stop()
{
    m_stopping.store(true, std::memory_order_release);
    m_frameCv.notify_all();

    try
    {
        if (m_framePool && m_frameArrivedToken.value)
        {
            m_framePool.FrameArrived(m_frameArrivedToken);
            m_frameArrivedToken = {};
        }
    }
    catch (...)
    {
    }

    for (int i = 0; i < 200 && m_callbackCount.load() > 0; ++i)
    {
        std::this_thread::sleep_for(std::chrono::milliseconds(5));
    }

    try { if (m_session)   { m_session.Close();   m_session = nullptr; } } catch (...) {}
    try { if (m_framePool) { m_framePool.Close(); m_framePool = nullptr; } } catch (...) {}

    m_captureItem = nullptr;
    m_winrtDevice = nullptr;

    m_stagingTexture.Reset();
    m_d3dContext.Reset();
    m_d3dDevice.Reset();

    std::lock_guard<std::mutex> lock(m_frameMutex);
    m_latestFrame.clear();
    m_outputFrame.clear();
    m_frameWidth = 0;
    m_frameHeight = 0;
    m_outputWidth = 0;
    m_outputHeight = 0;
    m_textureWidth = 0;
    m_textureHeight = 0;
    m_poolWidth = 0;
    m_poolHeight = 0;
}

WGCCapture::~WGCCapture()
{
    Stop();
}
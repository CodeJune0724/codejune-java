#pragma once
#include <windows.h>
#include <d3d11.h>
#include <wrl.h>
#include <winrt/Windows.Graphics.Capture.h>
#include <winrt/Windows.Graphics.DirectX.Direct3D11.h>
#include <windows.graphics.capture.interop.h>
#include <windows.graphics.directx.direct3d11.interop.h>
#include <mutex>
#include <condition_variable>
#include <chrono>
#include <vector>
#include <atomic>
#include <cstdint>

using namespace Microsoft::WRL;
using namespace winrt::Windows::Graphics::Capture;

class WGCCapture {
public:
    bool Init(HWND hwnd);
    bool GetFrame(int& outWidth, int& outHeight, void** outData);
    int  GetWidth();
    int  GetHeight();
    void Stop();
    ~WGCCapture();

private:
    HWND m_hwnd = nullptr;
    ComPtr<ID3D11Device> m_d3dDevice;
    ComPtr<ID3D11DeviceContext> m_d3dContext;
    ComPtr<ID3D11Texture2D> m_stagingTexture;

    GraphicsCaptureItem m_captureItem{ nullptr };
    Direct3D11CaptureFramePool m_framePool{ nullptr };
    GraphicsCaptureSession m_session{ nullptr };

    winrt::event_token m_frameArrivedToken{};

    std::mutex m_frameMutex;
    std::condition_variable m_frameCv;

    std::vector<uint8_t> m_latestFrame;
    std::vector<uint8_t> m_outputFrame;

    // 实际内容尺寸（来自 ContentSize）
    int m_frameWidth = 0;
    int m_frameHeight = 0;

    // 纹理尺寸（可能因 padding 大于内容尺寸）
    int m_textureWidth = 0;
    int m_textureHeight = 0;

    std::atomic<bool> m_stopping{ false };
    std::atomic<int>  m_callbackCount{ 0 };

    void OnFrameArrived(Direct3D11CaptureFramePool const& sender);
};
#include <jni.h>
#include "wgc_capture.h"
#include <unordered_map>
#include <memory>
#include <mutex>
#include <roapi.h>
#include <windows.h>

static std::unordered_map<long, std::unique_ptr<WGCCapture>> g_sessions;
static std::mutex g_sessionsMutex;
static long g_nextId = 1;

static void ThrowJavaException(JNIEnv* env, const char* msg) {
    jclass cls = env->FindClass("java/lang/RuntimeException");
    if (cls != nullptr) {
        env->ThrowNew(cls, msg);
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_codejune_uiauto_WGCapture_nativeStartCapture(
        JNIEnv* env, jclass, jlong hwnd) {
    HWND hWndRaw = reinterpret_cast<HWND>(static_cast<intptr_t>(hwnd));
    // 校验窗口是否有效
    if (!IsWindow(hWndRaw)) {
        ThrowJavaException(env, "nativeStartCapture: hwnd is not valid window");
        return 0;
    }
    try {
        // 修复：检查RoInitialize返回值，不要无条件调用，忽略模式切换错误
        HRESULT hrRo = RoInitialize(RO_INIT_MULTITHREADED);
        // RPC_E_CHANGED_MODE = 0x80010106 代表当前线程已经是其他COM单元，允许继续运行
        if (FAILED(hrRo) && hrRo != RPC_E_CHANGED_MODE)
        {
            char buf[256];
            snprintf(buf, sizeof(buf), "RoInitialize failed hr=0x%08X", (unsigned int)hrRo);
            ThrowJavaException(env, buf);
            return 0;
        }

        auto capture = std::make_unique<WGCCapture>();
        if (!capture->Init(hWndRaw)) {
            return 0;
        }
        std::lock_guard<std::mutex> lock(g_sessionsMutex);
        long id = g_nextId++;
        g_sessions[id] = std::move(capture);
        return id;
    } catch (const std::exception& e) {
        ThrowJavaException(env, e.what());
        return 0;
    } catch (...) {
        ThrowJavaException(env, "nativeStartCapture: unknown C++ exception");
        return 0;
    }
}

// 返回DirectByteBuffer：注意Java层不允许长期持有返回的buffer，用完立刻读取
extern "C" JNIEXPORT jobject JNICALL
Java_com_codejune_uiauto_WGCapture_nativeGetFrameBuffer(
        JNIEnv* env, jclass, jlong sessionId) {
    try {
        std::unique_ptr<WGCCapture>* target = nullptr;
        {
            std::lock_guard<std::mutex> lock(g_sessionsMutex);
            auto it = g_sessions.find(sessionId);
            if (it == g_sessions.end()) return nullptr;
            target = &it->second;
        }
        int w = 0, h = 0;
        void* data = nullptr;
        if (!(*target)->GetFrame(w, h, &data)) return nullptr;
        if (data == nullptr || w <= 0 || h <= 0) return nullptr;
        return env->NewDirectByteBuffer(
            data, static_cast<jlong>(w) * h * 4);
    } catch (const std::exception& e) {
        ThrowJavaException(env, e.what());
        return nullptr;
    } catch (...) {
        ThrowJavaException(env, "nativeGetFrameBuffer: unknown C++ exception");
        return nullptr;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_codejune_uiauto_WGCapture_nativeGetWidth(
        JNIEnv* env, jclass, jlong sessionId) {
    try {
        std::lock_guard<std::mutex> lock(g_sessionsMutex);
        auto it = g_sessions.find(sessionId);
        return (it == g_sessions.end()) ? 0 : it->second->GetWidth();
    } catch (...) {
        return 0;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_codejune_uiauto_WGCapture_nativeGetHeight(
        JNIEnv* env, jclass, jlong sessionId) {
    try {
        std::lock_guard<std::mutex> lock(g_sessionsMutex);
        auto it = g_sessions.find(sessionId);
        return (it == g_sessions.end()) ? 0 : it->second->GetHeight();
    } catch (...) {
        return 0;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_codejune_uiauto_WGCapture_nativeStopCapture(
        JNIEnv* env, jclass, jlong sessionId) {
    try {
        std::unique_ptr<WGCCapture> toDestroy;
        {
            std::lock_guard<std::mutex> lock(g_sessionsMutex);
            auto it = g_sessions.find(sessionId);
            if (it == g_sessions.end()) return;
            toDestroy = std::move(it->second);
            g_sessions.erase(it);
        }
        if (toDestroy) {
            try { toDestroy->Stop(); } catch (...) {}
            toDestroy.reset();
        }
    } catch (const std::exception& e) {
        ThrowJavaException(env, e.what());
    } catch (...) {
        ThrowJavaException(env, "nativeStopCapture: unknown C++ exception");
    }
}
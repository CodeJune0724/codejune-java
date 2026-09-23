#include <jni.h>
#include "wgc_capture.h"
#include <unordered_map>
#include <memory>
#include <mutex>
#include <windows.h>
#include <cstdio>
#include <objbase.h>
#include <queue>
#include <condition_variable>

static void ThrowJavaException(JNIEnv* env, const char* msg) {
    jclass cls = env->FindClass("java/lang/RuntimeException");
    if (cls != nullptr) {
        env->ThrowNew(cls, msg);
    }
}

static bool IsSupportWgcWindowCapture()
{
    OSVERSIONINFOEXW osvi = {0};
    osvi.dwOSVersionInfoSize = sizeof(osvi);
    GetVersionExW((LPOSVERSIONINFOW)&osvi);
    if(osvi.dwMajorVersion <10) return false;
    if(osvi.dwMinorVersion >0) return true;
    return osvi.dwBuildNumber >= 18362;
}

enum TaskType {
    TASK_START,
    TASK_STOP,
    TASK_GETFRAME,
    TASK_GETWIDTH,
    TASK_GETHEIGHT,
    TASK_EXIT
};

struct Task {
    TaskType type;
    HWND hwnd{nullptr};
    jlong sessionId{0};

    bool boolRet{false};
    jlong longRet{0};
    int intRet{0};
    int outW{0};
    int outH{0};
    void* outData{nullptr};

    std::mutex mtx;
    std::condition_variable cv;
    bool done{false};
};

static std::thread g_workerThread;
static std::mutex g_taskMtx;
static std::condition_variable g_taskCv;
static std::queue<std::shared_ptr<Task>> g_taskQueue;

static std::unordered_map<long, std::unique_ptr<WGCCapture>> g_sessions;
static std::mutex g_sessionsMutex;
static long g_nextId = 1;

static std::atomic<int>  g_activeSessionCount{0};
static std::atomic<bool> g_workerStarted{false};
static std::atomic<bool> g_workerDied{false};

// 投递任务；worker未启动/已死亡返回nullptr
static std::shared_ptr<Task> PostTask(std::shared_ptr<Task> task)
{
    if(!g_workerStarted.load() || g_workerDied.load())
    {
        return nullptr;
    }
    {
        std::lock_guard<std::mutex> lk(g_taskMtx);
        g_taskQueue.push(task);
    }
    g_taskCv.notify_one();

    std::unique_lock<std::mutex> lk(task->mtx);
    // 超时5秒，防止死锁挂JNI线程
    if(!task->cv.wait_for(lk, std::chrono::seconds(5), [&]{ return task->done; }))
    {
        return nullptr;
    }
    return task;
}

static void WorkerThreadMain()
{
    HRESULT hrCo = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    (void)hrCo;
    try
    {
        for(;;)
        {
            std::shared_ptr<Task> task;
            {
                std::unique_lock<std::mutex> lk(g_taskMtx);
                g_taskCv.wait(lk, [&](){ return !g_taskQueue.empty(); });
                task = g_taskQueue.front();
                g_taskQueue.pop();
            }

            if(task->type == TASK_EXIT)
            {
                {
                    std::lock_guard<std::mutex> lk(task->mtx);
                    task->done = true;
                }
                task->cv.notify_one();
                break;
            }

            // 每个任务独立捕获异常，保证worker线程不会死亡
            try
            {
                switch(task->type)
                {
                case TASK_START:
                {
                    auto capture = std::make_unique<WGCCapture>();
                    bool ok = capture->Init(task->hwnd);
                    if(!ok)
                    {
                        task->longRet = 0;
                        task->boolRet = false;
                    }
                    else
                    {
                        std::lock_guard<std::mutex> lock(g_sessionsMutex);
                        long id = g_nextId++;
                        g_sessions[id] = std::move(capture);
                        task->longRet = id;
                        task->boolRet = true;
                    }
                    break;
                }
                case TASK_STOP:
                {
                    std::unique_ptr<WGCCapture> toDestroy;
                    {
                        std::lock_guard<std::mutex> lock(g_sessionsMutex);
                        auto it = g_sessions.find(task->sessionId);
                        if(it != g_sessions.end())
                        {
                            toDestroy = std::move(it->second);
                            g_sessions.erase(it);
                        }
                    }
                    if(toDestroy)
                    {
                        try{ toDestroy->Stop(); }catch(...){}
                        toDestroy.reset();
                    }
                    break;
                }
                case TASK_GETFRAME:
                {
                    WGCCapture* cap = nullptr;
                    {
                        std::lock_guard<std::mutex> lock(g_sessionsMutex);
                        auto it = g_sessions.find(task->sessionId);
                        if(it != g_sessions.end()) cap = it->second.get();
                    }
                    if(cap)
                    {
                        int w=0,h=0;void* d=nullptr;
                        bool ok = cap->GetFrame(w,h,&d);
                        task->boolRet = ok;
                        task->outW = w;
                        task->outH = h;
                        task->outData = d;
                    }
                    else
                    {
                        task->boolRet = false;
                    }
                    break;
                }
                case TASK_GETWIDTH:
                {
                    WGCCapture* cap = nullptr;
                    {
                        std::lock_guard<std::mutex> lock(g_sessionsMutex);
                        auto it = g_sessions.find(task->sessionId);
                        if(it != g_sessions.end()) cap = it->second.get();
                    }
                    task->intRet = cap ? cap->GetWidth() : 0;
                    break;
                }
                case TASK_GETHEIGHT:
                {
                    WGCCapture* cap = nullptr;
                    {
                        std::lock_guard<std::mutex> lock(g_sessionsMutex);
                        auto it = g_sessions.find(task->sessionId);
                        if(it != g_sessions.end()) cap = it->second.get();
                    }
                    task->intRet = cap ? cap->GetHeight() : 0;
                    break;
                }
                default: break;
                }
            }
            catch (...)
            {
                // 吃掉任务内部所有winrt/c++异常，worker不退出
            }

            {
                std::lock_guard<std::mutex> lk(task->mtx);
                task->done = true;
            }
            task->cv.notify_one();
        }
    }
    catch (...)
    {
        g_workerDied.store(true);
    }

    CoUninitialize();
    g_workerStarted.store(false);
}

// 懒启动worker线程，第一次startCapture调用
static bool EnsureWorkerStarted()
{
    if(g_workerStarted.load())
        return true;
    if(g_workerDied.load())
        return false;

    std::lock_guard<std::mutex> lk(g_taskMtx);
    if(g_workerStarted.load())
        return true;
    if(g_workerDied.load())
        return false;

    g_workerThread = std::thread(WorkerThreadMain);
    g_workerStarted.store(true);
    return true;
}

// 检查：当g_activeSessionCount==0，发送EXIT任务，join销毁worker
static void TryShutdownWorkerIfNoSessions()
{
    if(g_activeSessionCount.load() != 0)
        return;
    if(!g_workerStarted.load())
        return;
    if(g_workerDied.load())
        return;

    auto exitTask = std::make_shared<Task>();
    exitTask->type = TASK_EXIT;
    auto res = PostTask(exitTask);
    if(res != nullptr && g_workerThread.joinable())
    {
        g_workerThread.join();
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_codejune_uiauto_WGCCapture_nativeStartCapture(
        JNIEnv* env, jclass, jlong hwnd)
{
    try
    {
        HWND hWndRaw = reinterpret_cast<HWND>(static_cast<intptr_t>(hwnd));
        if (!IsWindow(hWndRaw)) {
            ThrowJavaException(env, "nativeStartCapture: hwnd is not valid window");
            return 0;
        }

        if(!IsSupportWgcWindowCapture())
        {
            ThrowJavaException(env,"nativeStartCapture: System version not support WGC window capture, require Win10 1903+(build>=18362)");
            return 0;
        }

        if(!EnsureWorkerStarted())
        {
            ThrowJavaException(env,"nativeStartCapture: worker thread start failed");
            return 0;
        }

        auto task = std::make_shared<Task>();
        task->type = TASK_START;
        task->hwnd = hWndRaw;
        auto resTask = PostTask(task);
        if(!resTask)
        {
            ThrowJavaException(env,"nativeStartCapture: post task failed");
            return 0;
        }

        jlong retId = resTask->longRet;
        if(retId == 0)
        {
            ThrowJavaException(env,"WGCCapture Init() failed");
            return 0;
        }
        g_activeSessionCount.fetch_add(1);
        return retId;
    }
    catch(...)
    {
        ThrowJavaException(env,"nativeStartCapture unknown c++ exception");
        return 0;
    }
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_codejune_uiauto_WGCCapture_nativeGetFrameBuffer(
        JNIEnv* env, jclass, jlong sessionId)
{
    try
    {
        if(!g_workerStarted.load() || g_workerDied.load())
            return nullptr;

        auto task = std::make_shared<Task>();
        task->type = TASK_GETFRAME;
        task->sessionId = sessionId;
        auto resTask = PostTask(task);
        if(!resTask) return nullptr;

        if(!resTask->boolRet)
        {
            return nullptr;
        }
        int w = resTask->outW;
        int h = resTask->outH;
        void* data = resTask->outData;
        if (data == nullptr || w <= 0 || h <= 0)
        {
            return nullptr;
        }

        const jlong size = static_cast<jlong>(w) * static_cast<jlong>(h) * 4;
        if (size <= 0 || size > (jlong)2 * 1024 * 1024 * 1024) return nullptr;
        jobject buf = env->NewDirectByteBuffer(data, size);
        return buf;
    }
    catch(...)
    {
        ThrowJavaException(env,"nativeGetFrameBuffer unknown c++ exception");
        return nullptr;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_codejune_uiauto_WGCCapture_nativeGetWidth(
        JNIEnv* env, jclass, jlong sessionId)
{
    try
    {
        if(!g_workerStarted.load() || g_workerDied.load())
            return 0;
        auto task = std::make_shared<Task>();
        task->type = TASK_GETWIDTH;
        task->sessionId = sessionId;
        auto resTask = PostTask(task);
        if(!resTask) return 0;
        return static_cast<jint>(resTask->intRet);
    }
    catch(...)
    {
        return 0;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_codejune_uiauto_WGCCapture_nativeGetHeight(
        JNIEnv* env, jclass, jlong sessionId)
{
    try
    {
        if(!g_workerStarted.load() || g_workerDied.load())
            return 0;
        auto task = std::make_shared<Task>();
        task->type = TASK_GETHEIGHT;
        task->sessionId = sessionId;
        auto resTask = PostTask(task);
        if(!resTask) return 0;
        return static_cast<jint>(resTask->intRet);
    }
    catch(...)
    {
        return 0;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_codejune_uiauto_WGCCapture_nativeStopCapture(
        JNIEnv* env, jclass, jlong sessionId)
{
    try
    {
        if(!g_workerStarted.load() || g_workerDied.load())
            return;

        auto task = std::make_shared<Task>();
        task->type = TASK_STOP;
        task->sessionId = sessionId;
        PostTask(task);

        // session计数减一；全部会话销毁后关闭worker线程
        g_activeSessionCount.fetch_sub(1);
        TryShutdownWorkerIfNoSessions();
    }
    catch(...)
    {
        ThrowJavaException(env,"nativeStopCapture unknown c++ exception");
    }
}

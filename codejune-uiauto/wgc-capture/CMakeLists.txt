cmake_minimum_required(VERSION 3.20)
project(wgc_capture VERSION 1.0 LANGUAGES CXX)

# 关键：静态链接 CRT，消除 msvcp140.dll / vcruntime140.dll 依赖
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_SYSTEM_VERSION 10.0.19041.0)

# --- JNI 手动指定（按你的实际 JDK 路径改） ---
set(JAVA_HOME "C:/Program Files/jdk-25.0.3")
set(JNI_INCLUDE_DIRS
    "${JAVA_HOME}/include"
    "${JAVA_HOME}/include/win32"
)
find_library(JAVA_JVM_LIBRARY NAMES jvm
    PATHS "${JAVA_HOME}/lib" NO_DEFAULT_PATH)
if(NOT JAVA_JVM_LIBRARY)
    message(FATAL_ERROR "找不到 jvm.lib")
endif()

add_library(wgc_capture SHARED
    src/wgc_capture.cpp
    src/jni_bridge.cpp
)

target_include_directories(wgc_capture PRIVATE ${JNI_INCLUDE_DIRS})

target_link_libraries(wgc_capture PRIVATE
    d3d11 dxgi windowsapp
)

if (MSVC)
    # 确保 CRT 静态链接（再显式设置一次，保险）
    target_compile_options(wgc_capture PRIVATE
        /MT /await /std:c++17
        /D_SILENCE_EXPERIMENTAL_COROUTINE_DEPRECATION_WARNINGS
    )
    # 强制禁用默认库，避免链接 msvcrt
    target_link_options(wgc_capture PRIVATE /NODEFAULTLIB:MSVCRT)
endif()
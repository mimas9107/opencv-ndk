cmake_minimum_required(VERSION 3.22.1)

project("opencvndk")

# 強制開啟 C++17 支援
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 1. 設定 OpenCV 的 CMake 檢索路徑 (指向我們 build_opencv_android.sh 的安裝位置)
set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../build/opencv_android_install/sdk/native/jni")

message(STATUS "OpenCV_DIR is set to: ${OpenCV_DIR}")

# 2. 尋找 OpenCV 庫
find_package(OpenCV REQUIRED)

message(STATUS "OpenCV library found:")
message(STATUS "   version: ${OpenCV_VERSION}")
message(STATUS "   libraries: ${OpenCV_LIBS}")
message(STATUS "   include path: ${OpenCV_INCLUDE_DIRS}")

# 3. 定義我們自己的 JNI 動態庫
add_library(${PROJECT_NAME} SHARED
        opencv-jni.cpp
)

# 4. 引入 OpenCV 標頭檔
target_include_directories(${PROJECT_NAME} PRIVATE
        ${OpenCV_INCLUDE_DIRS}
)

# 5. 連結所需要的 Android 系統日誌庫 (log) 與 OpenCV 庫
#    OpenCV_LIBS 由安裝包提供，已包含 opencv_dnn。
target_link_libraries(${PROJECT_NAME}
        ${OpenCV_LIBS}
        log
        android
        jnigraphics
)

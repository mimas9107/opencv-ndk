#!/usr/bin/env bash

# ==============================================================================
# OpenCV Android (ARM64-v8a) 交叉編譯腳本
# ==============================================================================

# 確保腳本在遇到錯誤時立即停止
set -e

# 1. 環境變數強制鎖定 JDK 21 與相關路徑
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export PATH="${JAVA_HOME}/bin:${PATH}"

# 目標與源碼路徑設定
OPENCV_SOURCE_DIR="/home/mimas/projects/opencv/opencv"
OPENCV_CONTRIB_DIR="/home/mimas/projects/opencv/opencv_contrib"
ANDROID_NDK_ROOT="/home/mimas/Android/Sdk/ndk/30.0.14904198"
PROJECT_ROOT="/home/mimas/projects/opencv-ndk"
BUILD_DIR="${PROJECT_ROOT}/build/opencv_android_arm64"
INSTALL_DIR="${PROJECT_ROOT}/build/opencv_android_install"

echo "=== 開始進行 OpenCV Android 編譯前準備 ==="
echo "JAVA_HOME: ${JAVA_HOME}"
echo "Java Version:"
java -version
echo "NDK Path: ${ANDROID_NDK_ROOT}"
echo "OpenCV Source: ${OPENCV_SOURCE_DIR}"

# 建立編譯與輸出目錄
mkdir -p "${BUILD_DIR}"
mkdir -p "${INSTALL_DIR}"

# 進入編譯目錄 (CMake 建議採用獨立建置方式)
cd "${BUILD_DIR}"

echo "=== 執行 CMake 配置 ==="
cmake \
    -GNinja \
    -DCMAKE_TOOLCHAIN_FILE="${ANDROID_NDK_ROOT}/build/cmake/android.toolchain.cmake" \
    -DANDROID_NDK="${ANDROID_NDK_ROOT}" \
    -DANDROID_ABI="arm64-v8a" \
    -DANDROID_PLATFORM="android-26" \
    -DANDROID_STL="c++_shared" \
    -DCMAKE_BUILD_TYPE="Release" \
    -DCMAKE_INSTALL_PREFIX="${INSTALL_DIR}" \
    \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_STATIC_LIBS=OFF \
    \
    -DBUILD_ANDROID_PROJECTS=OFF \
    -DBUILD_ANDROID_EXAMPLES=OFF \
    -DBUILD_DOCS=OFF \
    -DBUILD_TESTS=OFF \
    -DBUILD_PERF_TESTS=OFF \
    \
    -DBUILD_opencv_apps=OFF \
    -DBUILD_opencv_java=ON \
    \
    -DOPENCV_ENABLE_NONFREE=OFF \
    -DOPENCV_EXTRA_MODULES_PATH="" \
    \
    -DWITH_CUDA=OFF \
    -DWITH_OPENCL=ON \
    -DWITH_IPP=OFF \
    -DWITH_TBB=ON \
    -DWITH_WEBP=ON \
    -DWITH_PNG=ON \
    -DWITH_JPEG=ON \
    -DWITH_TIFF=OFF \
    \
    "${OPENCV_SOURCE_DIR}"

echo "=== 開始執行編譯 (使用 Ninja) ==="
ninja -j$(nproc)

echo "=== 執行安裝 (輸出庫檔案與標頭檔) ==="
ninja install

echo "=== 編譯完成！ ==="
echo "已成功將 Android 端 OpenCV 庫安裝至: ${INSTALL_DIR}"

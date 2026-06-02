#!/usr/bin/env bash

# ==============================================================================
# 階段 02 — 執行 CMake 配置 (Configure)
# ==============================================================================

# 確保在遇到任何命令出錯時立即中斷
set -e

# 引入階段 01 的環境變數
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/01_init_env.sh"

echo ">>> [開始執行 CMake 交叉編譯配置] <<<"

# 確保編譯目錄存在
mkdir -p "${BUILD_DIR}"
cd "${BUILD_DIR}"

# 執行 CMake 生成 Ninja 編譯檔
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

echo ">>> [CMake 配置成功完成！已生成 Ninja 建置檔] <<<"

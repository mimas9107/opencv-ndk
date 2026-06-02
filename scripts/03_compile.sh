#!/usr/bin/env bash

# ==============================================================================
# 階段 03 — 執行 OpenCV 源碼編譯 (Compile)
# ==============================================================================

set -e

# 引入階段 01 的環境變數
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/01_init_env.sh"

echo ">>> [開始編譯 OpenCV 原始碼] <<<"

# 進入建置目錄
if [ ! -d "${BUILD_DIR}" ]; then
    echo "錯誤: 偵測不到建置目錄 ${BUILD_DIR}，請先執行 02_cmake_configure.sh！"
    exit 1
fi

cd "${BUILD_DIR}"

# 執行 Ninja 編譯 (自動抓取 CPU 最大核心數平行處理)
ninja -j$(nproc)

echo ">>> [OpenCV 編譯成功完成！] <<<"

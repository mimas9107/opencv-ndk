#!/usr/bin/env bash

# ==============================================================================
# 階段 04 — 執行安裝與結構輸出 (Install)
# ==============================================================================

set -e

# 引入階段 01 的環境變數
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/01_init_env.sh"

echo ">>> [將編譯結果安裝至安裝輸出目錄] <<<"

if [ ! -d "${BUILD_DIR}" ]; then
    echo "錯誤: 偵測不到建置目錄 ${BUILD_DIR}，請先執行 03_compile.sh！"
    exit 1
fi

cd "${BUILD_DIR}"

# 執行安裝
ninja install

echo ">>> [OpenCV 安裝安裝與標頭檔封裝完成！] <<<"
echo "安裝路徑為: ${INSTALL_DIR}"

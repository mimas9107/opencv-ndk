#!/usr/bin/env bash

# ==============================================================================
# 階段 05 — 部署動態連結庫至 Android App 專案 (Deploy)
# ==============================================================================

set -e

# 引入階段 01 的環境變數
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/01_init_env.sh"

echo ">>> [部署編譯好的 .so 動態庫至 App jniLibs 目錄] <<<"

# 定義目的地路徑
APP_JNI_DIR="${PROJECT_ROOT}/app/src/main/jniLibs/arm64-v8a"
SO_SOURCE_DIR="${INSTALL_DIR}/sdk/native/libs/arm64-v8a"

if [ ! -d "${SO_SOURCE_DIR}" ]; then
    echo "錯誤: 找不到已安裝的庫目錄 ${SO_SOURCE_DIR}，請確認 04_install.sh 是否執行成功！"
    exit 1
fi

# 確保 Android jniLibs 目錄存在
mkdir -p "${APP_JNI_DIR}"

# 執行複製動作（我們主要複製 core, imgproc 與並行加速庫 tbb）
echo "開始拷貝 OpenCV .so 動態庫至 ${APP_JNI_DIR}..."
cp "${SO_SOURCE_DIR}"/*.so "${APP_JNI_DIR}/"

echo ">>> [.so 動態庫部署成功完成！] <<<"
echo "目前 App jniLibs/arm64-v8a 內的庫檔案："
ls -la "${APP_JNI_DIR}"

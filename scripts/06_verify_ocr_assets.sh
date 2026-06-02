#!/usr/bin/env bash

set -e

PROJECT_ROOT="/usr/local/home/mimas/project/opencv-ndk"
APK_PATH="${PROJECT_ROOT}/app/build/outputs/apk/debug/app-debug.apk"

echo ">>> 檢查 OCR assets 與 APK 打包結果 <<<"

if [[ ! -f "${PROJECT_ROOT}/app/src/main/assets/ocr/text_detection.onnx" ]]; then
    echo "缺少: app/src/main/assets/ocr/text_detection.onnx"
    exit 1
fi

if [[ ! -f "${PROJECT_ROOT}/app/src/main/assets/ocr/text_recognition.onnx" ]]; then
    echo "缺少: app/src/main/assets/ocr/text_recognition.onnx"
    exit 1
fi

if [[ ! -f "${PROJECT_ROOT}/app/src/main/assets/ocr/charset.txt" ]]; then
    echo "缺少: app/src/main/assets/ocr/charset.txt"
    exit 1
fi

echo "本地 assets 檢查通過"

if [[ ! -f "${APK_PATH}" ]]; then
    echo "尚未找到 APK: ${APK_PATH}"
    exit 1
fi

zipinfo -1 "${APK_PATH}" | rg '^assets/ocr/(text_detection\.onnx|text_recognition\.onnx|charset\.txt)$'

echo "APK 內的 OCR assets 檢查通過"


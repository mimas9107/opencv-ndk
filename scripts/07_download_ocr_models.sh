#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="/usr/local/home/mimas/project/opencv-ndk"
OCR_DIR="${PROJECT_ROOT}/app/src/main/assets/ocr"

DETECTION_URL="https://huggingface.co/opencv/text_detection_ppocr/resolve/main/text_detection_cn_ppocrv3_2023may.onnx"
RECOGNITION_URL="https://huggingface.co/opencv/text_recognition_crnn/resolve/main/text_recognition_CRNN_CN_2021nov.onnx"
CHARSET_URLS=(
    "https://huggingface.co/opencv/text_recognition_crnn/resolve/4fcc8b234f71acb1f88c464aab7c792856ad6365/charset_3944_CN.txt"
    "https://huggingface.co/opencv/text_recognition_crnn/resolve/main/charset_3944_CN.txt"
)

TMP_DIR="${OCR_DIR}/.download_tmp"

mkdir -p "${OCR_DIR}"
rm -rf "${TMP_DIR}"
mkdir -p "${TMP_DIR}"

download_file() {
    local url="$1"
    local output="$2"

    echo "下載: ${url}"
    curl -L --fail --retry 3 --retry-delay 2 -o "${output}" "${url}"
}

download_first_working() {
    local output="$1"
    shift

    local url
    for url in "$@"; do
        if download_file "${url}" "${output}"; then
            return 0
        fi
        echo "下載失敗，嘗試下一個來源..."
    done

    return 1
}

echo ">>> 開始下載 OCR 模型檔 <<<"

download_file "${DETECTION_URL}" "${TMP_DIR}/text_detection.onnx"
download_file "${RECOGNITION_URL}" "${TMP_DIR}/text_recognition.onnx"
download_first_working "${TMP_DIR}/charset.txt" "${CHARSET_URLS[@]}"

mv "${TMP_DIR}/text_detection.onnx" "${OCR_DIR}/text_detection.onnx"
mv "${TMP_DIR}/text_recognition.onnx" "${OCR_DIR}/text_recognition.onnx"
mv "${TMP_DIR}/charset.txt" "${OCR_DIR}/charset.txt"

rm -rf "${TMP_DIR}"

echo ">>> OCR 模型檔下載完成 <<<"
echo "放置位置: ${OCR_DIR}"
ls -lh "${OCR_DIR}"

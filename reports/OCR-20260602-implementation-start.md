---
title: "OCR Implement 起始紀錄"
date: 2026-06-02
description: "opencv-ndk 專案 OCR MVP 第一階段基礎實作紀錄"
---

# OCR Implement 起始紀錄

## 已確認事項

- 第一版模型組合固定為 `PP-OCRv3 Text Detection` + `CRNN_CN`
- 模型來源採 OpenCV 官方模型生態，不在第一版自行訓練
- 部署策略選定為 `app 私有目錄載入`，由 `assets/ocr/` 同步到 `filesDir/ocr/`
- OpenCV 原始碼中的 `TextDetectionModel_DB` 與 `TextRecognitionModel` API 已確認存在
- 本地 OpenCV Android 安裝包已包含 `opencv_dnn`
- `opencv-jni.cpp` 已通過 NDK `-fsyntax-only` 檢查

## 本次開始落地的內容

- Kotlin 端新增 OCR native method
- MainActivity 增加 OCR 節流與結果顯示欄位
- native 端建立模型快取、尺寸門檻與 JSON 回傳格式
- 建立 `app/src/main/assets/ocr/` 結構
- asset 同步固定只接受 `text_detection.onnx`、`text_recognition.onnx`、`charset.txt`
- detection 預處理已對齊 OpenCV Zoo 的 `736x736` / mean / scale 設定
- recognition 預處理已對齊 OpenCV Zoo 的 `100x32` / `127.5` 設定

## 第一版限制

- 若 `text_detection.onnx`、`text_recognition.onnx`、`charset.txt` 尚未放入 `assets/ocr/`，OCR 會回傳 `model_missing`
- 第一版先以灰階影像轉 BGR 後進行偵測與辨識，不處理版面結構
- 尺寸門檻固定為 `w > 32 && h > 32`，以及 `w > 256 || h > 256` 直接略過

## 後續補件

- 將模型檔補到 `app/src/main/assets/ocr/`
- 驗證 app 私有目錄載入流程
- 在實機上確認 OCR 節流與 preview 不互相阻塞
- 補上模型檔後再做實機辨識結果驗證

## 下載來源與腳本

- 下載腳本: [scripts/07_download_ocr_models.sh](/home/mimas/projects/opencv-ndk/scripts/07_download_ocr_models.sh)
- 文字偵測來源: `https://huggingface.co/opencv/text_detection_ppocr`
- 文字辨識來源: `https://huggingface.co/opencv/text_recognition_crnn`
- 字庫來源: `https://huggingface.co/opencv/text_recognition_crnn` (commit `4fcc8b234f71acb1f88c464aab7c792856ad6365`)

## 建置前提

- Gradle wrapper: `8.5`
- JAVA_HOME: `/usr/lib/jvm/java-21-openjdk-amd64`
- 建置腳本: [scripts/08_build_app_debug.sh](/home/mimas/projects/opencv-ndk/scripts/08_build_app_debug.sh)

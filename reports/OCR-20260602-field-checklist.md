---
title: "OCR 現場檢查表"
date: 2026-06-02
description: "把模型放置、APK 打包、安裝與 preview / OCR 驗證壓縮成一頁"
---

# OCR 現場檢查表

## A. 模型放置

- [ ] 執行 `bash scripts/07_download_ocr_models.sh`
- [ ] 將 `text_detection.onnx` 放入 `app/src/main/assets/ocr/`
- [ ] 將 `text_recognition.onnx` 放入 `app/src/main/assets/ocr/`
- [ ] 將 `charset.txt` 放入 `app/src/main/assets/ocr/`
- [ ] 確認 `app/src/main/assets/ocr/README.md` 仍保留
- [ ] 若腳本失敗，確認字庫來源使用 `opencv/text_recognition_crnn` 的 pinned commit

## B. 打包驗證

- [ ] 確認 `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`
- [ ] 確認 Gradle wrapper 使用 8.5
- [ ] 執行 `bash scripts/08_build_app_debug.sh`
- [ ] 執行 `bash scripts/06_verify_ocr_assets.sh`
- [ ] 確認本地 assets 檢查通過
- [ ] 確認 APK 內含 `assets/ocr/text_detection.onnx`
- [ ] 確認 APK 內含 `assets/ocr/text_recognition.onnx`
- [ ] 確認 APK 內含 `assets/ocr/charset.txt`

## C. 安裝與啟動

- [ ] 執行 `adb devices`
- [ ] 執行 `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- [ ] 啟動 App
- [ ] 授予相機權限
- [ ] 需要時執行 `adb logcat -v time | rg "OpenCV-NDK-JNI|MainActivity|AndroidRuntime|FATAL EXCEPTION"`

## D. 畫面檢查

- [ ] 灰階 preview 正常顯示
- [ ] 旋轉修正正常
- [ ] OCR 結果欄位不再長期停在缺檔
- [ ] OCR 不拖慢 preview

## E. 結果回填

```text
日期:
裝置:
模型狀態:
APK 驗證:
Preview:
Rotation:
OCR:
備註:
```

## F. 最短版

- 直接看 [OCR 實機快速檢查表](/home/mimas/projects/opencv-ndk/reports/OCR-20260602-quick-device-checklist.md)

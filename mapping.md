---
name:            "mapping.md"
description:     "opencv-ndk 專案導航地圖 — 供 AI Agent 快速理解架構與開發流程"
created_date:    "2026/06/06 14:05:00"
modified_date:   "2026/06/06 14:05:00"
project_version: "0.2.0"
document_version: "1.0.0"
agent_sign:      ['human/mimas', 'gemini cli/gemini-2.0-flash']
---

# opencv-ndk 專案導航地圖 (Mapping)

本文件旨在提供 AI Agent 快速進入專案所需的最小上下文，避免全域程式碼掃描。

## 1. 專案核心定位
- **目標裝置**: 華為 P30 Pro (Android 12 / EMUI 12 / ARM64-v8a)。
- **核心功能**: OpenCV 4.x NDK 移植、即時灰階 Preview、OCR 文字辨識 (MVP 已完成)。
- **技術棧**: Kotlin (Jetpack CameraX) + JNI (C++) + OpenCV DNN。

## 2. 關鍵檔案索引 (Entry Points)

### 2.1 Java/Kotlin 層 (UI & Bridge)
- `app/src/main/java/com/example/opencvndk/MainActivity.kt`: 主程式進入點，負責 CameraX 串接、OCR 節流 (Throttle) 與結果顯示。
- `app/src/main/java/com/example/opencvndk/OpenCVBridge.kt`: JNI 介面定義層。

### 2.2 Native 層 (C++/OpenCV)
- `app/src/main/cpp/opencv-jni.cpp`: 核心演算法實作，包含 YUV 轉 Gray、OCR DNN 初始化、偵測與辨識邏輯。
- `app/src/main/cpp/CMakeLists.txt`: Native 編譯配置。

### 2.3 資產與配置
- `app/src/main/assets/ocr/`: 存放 `text_detection.onnx`, `text_recognition.onnx`, `charset.txt`。
- `scripts/`: 包含從環境初始化到 OpenCV 交叉編譯與部署的完整腳本 (01-08)。

## 3. 核心工作流程 (Workflow)

### 3.1 重新編譯 OpenCV 庫與部署
若修改了 Native 層或需更新 OpenCV 庫：
```bash
bash scripts/run_all_stages.sh
```
此腳本會依序執行：環境檢查 -> CMake 配置 -> Ninja 編譯 -> Install 封裝 -> 部署至 `app/src/main/jniLibs`。

### 3.2 建立並安裝 APK
```bash
bash scripts/08_build_app_debug.sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 4. 目標狀態與門檻 (Parameters)
- **OCR 節流**: 300ms (在 `MainActivity.kt` 控制)。
- **ROI Gate**: 最小 `64x64`, 最大 `256x256` (在 `opencv-jni.cpp` 控制)。
- **影像格式**: CameraX 輸出 `YUV_420_888`，JNI 只取 Y 通道作為灰階影像。

## 5. 文件查閱路徑
- **功能設計**: `SPEC.md`
- **任務進度**: `OCR_implement_task.md`
- **歷史問題與測試結果**: `reports/` 目錄。
- **開發紀要**: `MEMOIR.md`

## 6. AI Agent 快速診斷建議
- **Build 失敗**: 優先檢查 `scripts/01_init_env.sh` 中的 `JAVA_HOME` 與 `ANDROID_NDK_ROOT`。
- **OCR 沒結果**: 檢查 `adb logcat` 是否有模型載入失敗或 `ROI gate` 過濾紀錄。
- **Preview 沒畫面**: 檢查 `MainActivity.kt` 中的 `CameraPermission` 流程。

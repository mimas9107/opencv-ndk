---
name:            "README.md"
description:     "OpenCV NDK 移植專案 — 為 Android (華為 P30 Pro / EMUI 12) 編譯 OpenCV 4.x"
created_date:    "2026/06/02 13:29:51"
modified_date:   "2026/06/02 17:41:22"
project_version: "0.2.2"
document_version: "1.3.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity', 'codex/GPT-5', 'gemini cli/gemini-2.0-flash']
---

# opencv-ndk

> 用於在 Android 上運行 OpenCV 的 NDK 編譯與移植專案。

## 概要

本專案為 MVP（最小可行性產品）專案，旨在針對華為 P30 Pro (Android 12 / EMUI v12) 平台，使用 NDK 編譯 OpenCV 4.x，並將其整合至個人架設的服務中。  
開發策略為首先實現 1 至 3 個核心功能的 MVP，隨後再逐步擴展至更多元化的應用程式。

## 目前成果

- 已完成即時灰階 preview 與旋轉修正。
- 已完成 OCR MVP 第一輪驗證。
- OCR 模型組合：`PP-OCRv3 Text Detection` + `CRNN_CN`。
- P30 Pro 實機已辨識 `logitech (conf=0.99)`。
- OCR ROI gate 已調整為最小 `64x64`、最大 `256x256`。
- OCR dispatch throttle 已調整為 `300ms`。
- Debug APK 約 `105M`，OCR assets 約 `72M`。

## 目錄結構（初期）

```
opencv-ndk/
├── AGENTS.md          # 針對 AI 代理的專案定義規範
├── README.md          # 本檔案（正體中文版）
├── CHANGELOG.md       # 變更歷史紀錄
├── SPEC.md            # 系統設計規格書
├── MEMOIR.md          # 開發備忘錄與學習日誌
├── docs/              # 補充文件目錄
├── scripts/           # 建置輔助指令稿
├── app/               # Android 應用程式原始碼（後續規劃）
└── reports/           # 測試與建置報告目錄
```

## 環境資訊

| 項目 | 數值 / 路徑 |
|------|-----------|
| 主機作業系統 | Debian 13 (Linux 6.12.90+deb13.1) |
| 桌面視窗管理員 | Wayland + Sway WM |
| Python 版本 | 3.13.5 (系統全域) |
| Java 版本 | OpenJDK 21 (Gradle 推薦環境，透過 JAVA_HOME 強制指定) |
| CMake 版本 | 3.31.6 |
| Android NDK | r30 (30.0.14904198) — `~/Android/Sdk/ndk/30.0.14904198` |
| Android SDK | `~/Android/Sdk` |
| SDK 管理工具 | `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager` |
| ADB 工具 | `/usr/bin/adb` |
| Android Studio | `/opt/android-studio` |
| OpenCV 原始碼 | `/home/mimas/projects/opencv/opencv` (v4.14.0-pre) |
| opencv_contrib | `/home/mimas/projects/opencv/opencv_contrib` |
| 目標測試裝置 | 華為 P30 Pro, Android 12, EMUI v12 |
| 專案根目錄 | `/home/mimas/projects/opencv-ndk` |

## 快速上手

```bash
# 1. 下載 OCR 模型
bash scripts/07_download_ocr_models.sh

# 2. 建置 debug APK
bash scripts/08_build_app_debug.sh

# 3. 確認 APK 內含 OCR assets
bash scripts/06_verify_ocr_assets.sh

# 4. 透過 ADB 確認裝置連接狀態
adb devices

# 5. 安裝 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## OCR 相關文件

- [OCR_implement_plan.md](/home/mimas/projects/opencv-ndk/OCR_implement_plan.md)
- [OCR_implement_task.md](/home/mimas/projects/opencv-ndk/OCR_implement_task.md)
- [OCR 實機 logcat 驗證紀錄](/home/mimas/projects/opencv-ndk/reports/OCR-20260602-device-log-run.md)
- [OCR 部署體積與回退評估](/home/mimas/projects/opencv-ndk/reports/OCR-20260602-deployment-rollback-assessment.md)

## 參考連結

- [OpenCV Android SDK 建置指南](https://docs.opencv.org/4.x/d0/d76/tutorial_arm_crosscompile_with_cmake.html)
- [OpenCV platforms/android/ 原始碼目錄](file:///home/mimas/projects/opencv/opencv/platforms/android/)
- NDK 設定範例： `ndk-25.config.py` (platforms/android/)

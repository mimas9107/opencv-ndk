---
name:            "README.md"
description:     "OpenCV NDK 移植專案 — 為 Android (華為 P30 Pro / EMUI 12) 編譯 OpenCV 4.x"
created_date:    "2026/06/02 13:29:51"
modified_date:   "2026/06/02 13:41:00"
project_version: "0.1.0"
document_version: "1.0.1"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
---

# opencv-ndk

> 用於在 Android 上運行 OpenCV 的 NDK 編譯與移植專案。

## 概要

本專案為 MVP（最小可行性產品）專案，旨在針對華為 P30 Pro (Android 12 / EMUI v12) 平台，使用 NDK 編譯 OpenCV 4.x，並將其整合至個人架設的服務中。  
開發策略為首先實現 1 至 3 個核心功能的 MVP，隨後再逐步擴展至更多元化的應用程式。

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
| OpenCV 原始碼 | `/usr/local/home/mimas/myvenv01/opencv/opencv` (v4.14.0-pre) |
| opencv_contrib | `/usr/local/home/mimas/myvenv01/opencv/opencv_contrib` |
| 目標測試裝置 | 華為 P30 Pro, Android 12, EMUI v12 |
| 專案根目錄 | `/home/mimas/project/opencv-ndk` |

## 快速上手（規劃中）

```bash
# 1. 確認 NDK 設定與路徑
ls ~/Android/Sdk/ndk/30.0.14904198/

# 2. 為 Android 平台編譯 OpenCV（建置指令稿尚未建立）
# scripts/build_opencv_android.sh

# 3. 透過 ADB 確認裝置連接狀態
adb devices
```

## 參考連結

- [OpenCV Android SDK 建置指南](https://docs.opencv.org/4.x/d0/d76/tutorial_arm_crosscompile_with_cmake.html)
- [OpenCV platforms/android/ 原始碼目錄](file:///usr/local/home/mimas/myvenv01/opencv/opencv/platforms/android/)
- NDK 設定範例： `ndk-25.config.py` (platforms/android/)

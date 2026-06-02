---
name:            "CHANGELOG.md"
description:     "opencv-ndk 專案變更歷史紀錄"
created_date:    "2026/06/02 13:29:51"
modified_date:   "2026/06/02 13:41:00"
project_version: "0.1.0"
document_version: "1.0.1"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
---

# 變更歷史紀錄 (CHANGELOG)

本專案的所有重大變更都將記錄於此檔案中。
格式基於 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.0.0/) 規範。
版本號遞增遵循 10 進位原則（當 PATCH/MINOR 滿 10 時進位）。

---

## [0.1.0] — 2026-06-02

### Added
- 專案初始化建置 (AI 代理: Antigravity)
- [README.md](file:///home/mimas/project/opencv-ndk/README.md) — 提供環境配置資訊與基本目錄結構。
- [CHANGELOG.md](file:///home/mimas/project/opencv-ndk/CHANGELOG.md) — 建立變更歷史紀錄檔（本檔案）。
- [SPEC.md](file:///home/mimas/project/opencv-ndk/SPEC.md) — 撰寫設計規格書初版。
- [MEMOIR.md](file:///home/mimas/project/opencv-ndk/MEMOIR.md) — 撰寫開發備忘錄與決策流程初版。
- [AGENTS.md](file:///home/mimas/project/opencv-ndk/AGENTS.md) — 導入既有的 AI 代理運作規範。

### Changed
- 將專案建議與強制使用的 Java 版本統一修正為 **JDK 21**，以確保與 Android 產出工具鏈（如 Gradle）的最佳相容性，並全面更新了 README、SPEC 與 MEMOIR 文件。

---

## [未發佈 (Unreleased)]

### Planned
- 建立 `scripts/build_opencv_android.sh` 建置腳本，實現 NDK 交叉編譯。
- 驗證 OpenCV 4.14.0 的 Android AAR 產出與相容性。
- 選定第一個核心 MVP 功能（人臉偵測 / 邊緣偵測 / QR Code 讀取 其中之一）。
- 使用 ADB 進行實機部署與驗證。

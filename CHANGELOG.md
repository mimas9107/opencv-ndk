---
name:            "CHANGELOG.md"
description:     "opencv-ndk 專案變更歷史紀錄"
created_date:    "2026/06/02 13:29:51"
modified_date:   "2026/06/02 17:41:22"
project_version: "0.2.4"
document_version: "1.5.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity', 'codex/GPT-5', 'gemini cli/gemini-2.0-flash']
---

# 變更歷史紀錄 (CHANGELOG)

本專案的所有重大變更都將記錄於此檔案中。
格式基於 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.0.0/) 規範。
版本號遞增遵循 10 進位原則（當 PATCH/MINOR 滿 10 時進位）。

---

## [0.2.5] — 2026-06-06

### Changed
- 修修正 UI 顯示邏輯：當 OCR 辨識結果因字符過濾（中/英/數）被排除時，UI 不再顯示該偵測項目的編號與信心值，確保介面純淨。
- 連動偵測點 (Cyan Point)：僅在結果被視為「有效 (usable)」時才繪製偵測點，使視覺反饋與過濾設定完全一致。

## [0.2.4] — 2026-06-06

### Added
- 新增 OCR 字符類別過濾功能：可透過 UI 切換是否保留「中文」、「英文」或「數字」辨識結果。
- 實作 Native 層 UTF-8 字符類別過濾器，降低傳輸與顯示無用資訊的開銷。

### Changed
- 更新 `activity_main.xml`：新增三個 `SwitchCompat` 用於類別過濾控制。
- 更新 `OpenCVBridge.kt` 與 `opencv-jni.cpp`：擴充 JNI 介面以支援傳遞過濾旗標。

## [0.2.3] — 2026-06-06

### Changed
- 簡化 OCR 偵測可視化：將繪製完整的「偵測矩形框」改為僅繪製「左上角小圓點」，以追求極致的渲染效能。
- 更新 `MainActivity.kt` 中的繪製邏輯與 `Paint` 設定。

## [0.2.2] — 2026-06-06

### Added
- 新增「中心 50% 區域偵測」功能：OCR 僅處理視野中央寬度與高度各 50% 的區域。
- 實作 Native 層座標映射邏輯，將裁切區域的偵測結果正確還原至全圖座標。

### Changed
- 更新 `opencv-jni.cpp` 中的 `runOcrPipeline`，導入影像裁切與座標偏移補償。

## [0.2.1] — 2026-06-06

### Added
- 新增 OCR 偵測外框即時繪製功能，並在 UI 增加切換開關 (Toggle Switch)。
- 在 `MainActivity.kt` 實作基於 `Canvas` 的即時外框繪製邏輯。

### Changed
- 調整 OCR ROI gate 門檻：下限調整為 `48x60`，上限調整為 `448x448`。
- 強化 OCR 偵測過濾：新增信心值門檻，僅放行信心值 ≥ `0.96` 的候選框。
- 更新 `opencv-jni.cpp` 以支援嚴格的信心值篩選與新的 ROI 尺寸限制。

## [0.2.0] — 2026-06-02

### Added
- 完成 OCR MVP 第一輪驗證，採用 `PP-OCRv3 Text Detection` + `CRNN_CN`。
- 新增 OCR native pipeline：模型載入、文字偵測、ROI gate、文字辨識、JSON 回傳與 logcat 驗證輸出。
- 新增 OCR UI 顯示欄位，能在 P30 Pro 上顯示 `logitech (conf=0.99)`。
- 新增 OCR assets 下載、驗證、debug APK 建置腳本。
- 新增 OCR 實機驗證、preview protection、permission flow、ROI gate、throttle、deployment rollback 等 reports。
- 新增 `.gitignore` 規則，排除 Gradle / CMake / Android build generated files，並將既有 generated/local files 從 Git tracking 移除。

### Changed
- OCR ROI gate 由初始 `32x32` 收斂為 `64x64`，降低小框誤判。
- OCR dispatch throttle 由 `400ms` 調整為 `300ms`。
- APK 打包策略維持模型隨 APK 打包並同步至 app 私有目錄；debug APK 約 `105M`，OCR assets 約 `72M`。

### Verified
- P30 Pro 上完成 app 啟動、相機權限拒絕 / 重授權、灰階 preview、旋轉修正、OCR result 顯示與 logcat 驗證。
- 調整後回歸測試未觀察到特別問題。

## [0.1.0] — 2026-06-02

### Added
- 專案初始化建置 (AI 代理: Antigravity)
- [README.md](file:///home/mimas/projects/opencv-ndk/README.md) — 提供環境配置資訊與基本目錄結構。
- [CHANGELOG.md](file:///home/mimas/projects/opencv-ndk/CHANGELOG.md) — 建立變更歷史紀錄檔（本檔案）。
- [SPEC.md](file:///home/mimas/projects/opencv-ndk/SPEC.md) — 撰寫設計規格書初版。
- [MEMOIR.md](file:///home/mimas/projects/opencv-ndk/MEMOIR.md) — 撰寫開發備忘錄與決策流程初版。
- [AGENTS.md](file:///home/mimas/projects/opencv-ndk/AGENTS.md) — 導入既有的 AI 代理運作規範。

### Changed
- 將專案建議與強制使用的 Java 版本統一修正為 **JDK 21**，以確保與 Android 產出工具鏈（如 Gradle）的最佳相容性，並全面更新了 README、SPEC 與 MEMOIR 文件。

---

## [未發佈 (Unreleased)]

### Planned
- 擴大 OCR 測試樣本，包含更多英文、數字與中文場景。
- 若繁體中文效果不足，評估替換字庫或模型。
- 若 release APK 體積需要壓縮，評估模型外部下載或分離部署。

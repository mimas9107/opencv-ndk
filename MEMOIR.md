---
name:            "MEMOIR.md"
description:     "opencv-ndk 專案開發備忘錄、學習日誌與疑難排解紀錄"
created_date:    "2026/06/02 13:33:16"
modified_date:   "2026/06/02 17:41:22"
project_version: "0.2.1"
document_version: "1.2.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity', 'codex/GPT-5', 'gemini cli/gemini-2.0-flash']
---

# MEMOIR — 開發備忘錄與學習日誌

此檔案旨在記錄「為什麼這樣做」以及「遇到了什麼問題與坑洞」。  
以便後續自己或是 AI 代理能夠藉此快速復原開發時的上下文情境。

---

## 2026-06-02 — 專案初始化

### 1. 檢測結果總結

**環境掃描結果 (由 AI 代理 Antigravity 執行):**

| 項目分類 | 內容與說明 |
|---------|-----------|
| NDK 路徑 | `~/Android/Sdk/ndk/30.0.14904198` — r30 已預先安裝完畢 |
| ADB 工具 | `/usr/bin/adb` — 已存在於系統 Path 中 |
| CMake 版本 | 3.31.6 (`/usr/bin/cmake`) — 系統 CMake 版本符合需求 |
| Ninja 工具 | `/usr/bin/ninja` — 系統已內建並可直接使用 |
| sdkmanager | `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager` |
| OpenCV 原始碼 | 4.14.0-pre (基於 master 分支) — `/home/mimas/projects/opencv/opencv` |
| opencv_contrib | 同目錄下的 `opencv_contrib/` 資料夾 |
| Java 環境 | **JDK 21** (使用 Gradle 推薦版本，透過環境變數強制指定) |

**Java 版本選定決策流程:**
- 系統中同時存在 JDK 21 (為 update-alternatives 的預設配置) 與 JDK 25 (為當前命令列終端機的預設版本)。
- Android Gradle Plugin (AGP) 及相關的 Android 軟體工具鏈，通常尚未對最新版的 JDK 25 提供完整相容支援。
- 為了最大程度確保建置過程的穩定度，我們決定將本專案的標準 JDK 版本錨定在 **JDK 21** (路徑: `/usr/lib/jvm/java-21-openjdk-amd64`)。

**在 `platforms/android/` 目錄下發現的 NDK 設定檔:**
- 該目錄下僅有 `ndk-25.config.py` 和 `ndk-22.config.py` 等舊版設定。
- 雖然目前沒有 r30 的專屬設定檔，但可高度參考 `ndk-25.config.py` 的參數進行調整。

### 2. 重要決策與方針備忘

- **Java 環境控制**: 為避免 Gradle 誤調用當前終端機預設的 JDK 25，未來撰寫的所有編編譯腳本在執行前，均必須明確執行 `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`。
- NDK r30 完整支援 API 26+。華為 P30 Pro 的運行系統為 Android 12 (API 31)，兩者在相容性上完全沒有問題。
- 針對編譯 OpenCV Android SDK，目前有兩個主要路徑：
  1. 使用內建的 `build_sdk.py` 指令稿。
  2. 直接調用 CMake 進行獨立的交叉編譯。
  → **決策：我們將優先嘗試「CMake 直接交叉編譯」**，此舉相依性較低且易於進行編譯除錯。
- 華為 EMUI v12 系統缺乏 Google Play 框架，將採用 ADB 直接側載 APK 的方式進行部署測試。
- 初期的 MVP 核心功能預計將**不需要**使用 opencv_contrib 擴充模組，先以 OpenCV 的基本模組完成。

- **建置腳本模組化拆解**: 為了提高 OpenCV 編譯的可除錯性（Troubleshooting），避免「大一統」腳本出錯時無從下手的困局，我們做出重大架構拆解：
  - 將建置流切分為 5 個明確的獨立階段階段腳本 (`01_init_env.sh` 至 `05_deploy_to_app.sh`)，分別主導環境、配置、編譯、安裝與 App 部署。
  - 保留原始 `build_opencv_android.sh` 作為設計對照。
  - 實作 `run_all_stages.sh` 用於整合一鍵執行，大幅提升了整個專案 NDK 建置環節的穩健性。

### 3. 下一步行動計畫

1. **已完成**：選定核心 MVP 「選項 D：即時相機灰階影像預覽」，並克服相機偏轉角（左旋 90°）與 `jnigraphics` 資源連結等問題，順利產出調試 APK。
2. **已完成**：將原本龐大的建置腳本重構並拆解為 5 個階段性控制指令稿與 1 個一鍵串聯指令稿，並成功通過快取增量建置驗證。
3. **已完成**：OCR MVP 第一輪驗證，包含文字偵測、ROI gate、文字辨識、UI 顯示、logcat 驗證、preview protection 與權限流程回歸。
4. 下一步可擴大 OCR 場景樣本，或開始下一核心 MVP（例如 QR Code 讀取 / Canny / 人臉偵測）。

---

## 2026-06-02 — OCR MVP 第一輪驗證完成

### 背景描述

在灰階 preview MVP 穩定後，第二條分析管線加入 OCR。目標是先在 P30 Pro 上跑通最小可用流程，而不是一開始追求完整排版理解或大規模中文泛化。

### 實作內容與過程

- 採用 `PP-OCRv3 Text Detection` + `CRNN_CN`。
- 模型放入 `app/src/main/assets/ocr/`，啟動後同步到 `filesDir/ocr`。
- Kotlin 端新增 OCR dispatch throttle 與獨立 executor。
- Native 端建立「偵測 -> ROI gate -> 辨識 -> JSON 回傳」流程。
- UI 新增 OCR 結果顯示欄位。
- 下載 / 驗證 / build 腳本固定為：
  - `scripts/07_download_ocr_models.sh`
  - `scripts/06_verify_ocr_assets.sh`
  - `scripts/08_build_app_debug.sh`

### 成果與遭遇問題

- P30 Pro 實機可辨識滑鼠上的 `logitech`，UI 顯示約 `conf=0.99`。
- logcat 可看到 `OCR 辨識結果 #0: text="logitech"`。
- 初期出現小框誤判成單字元 `t`，因此先收緊 ROI gate。
- ROI gate 從 `32x32` 調整為 `64x64`。
- OCR dispatch throttle 從 `400ms` 調整為 `300ms`。
- 調整後 app 回歸測試沒有觀察到特別問題。

### 決策備忘

- Debug APK 約 `105M`，OCR assets 約 `72M`。目前作為個人 P30 Pro 測試版可接受。
- 第一版維持模型隨 APK 打包，不改成外部下載，避免新增部署失敗點。
- 若後續繁體中文效果不足，再評估字庫或模型替換。
- 若後續效能壓力變高，優先調高 OCR 節流間隔，不破壞 preview。

### 下一步規劃

1. 擴大 OCR 測試樣本，加入更多英文、數字與中文場景。
2. 評估 release APK 體議是否需要拆分模型部署。
3. 根據實際應用需求選擇下一個 OpenCV MVP。

---

## 2026-06-06 — OCR ROI 調優與偵測可視化 (v0.2.1)

### 背景描述

為了提升 OCR 的精準度並提供更好的調試工具，需要收緊信心值過濾，並讓使用者能直觀看到「哪些框被偵測到了」。

### 實作內容與過程

- **ROI Gate 調整**：上限放寬至 `448x448` 以支援較大字體，下限微調為 `48x60`。
- **信心值硬過濾**：在 Native 層加入 `0.96` 的門檻，低於此值的候選框不回傳且不辨識，大幅減少誤報。
- **可視化回饋**：
    - 在 `MainActivity` 增加 `Canvas` 繪製邏輯，將 JNI 回傳的 Rect 座標畫在預覽圖層上。
    - 增加 UI `SwitchCompat` 連動 `AtomicBoolean` 旗標，實現無縫開關繪製功能。

### 成果與遭遇問題

- 使用者可手動切換開關觀察偵測狀態。
- 繪製邏輯實作在 `processImageFrame` 的灰階 Bitmap 上，不影響 CameraX 原始幀，效能負擔極小。
- 修正了初期小框誤判的問題，偵測結果更為純淨。

### 下一步規劃

1. 考慮加入多色辨識（例如辨識成功與失敗使用不同顏色）。
2. 評估是否需要將信心值門檻也做成 UI 滑桿供動態調整。

---

## 範本檔案（後續記錄使用）

```
## YYYY-MM-DD — <記錄主題>

### 背景描述

### 實作內容與過程

### 成果與遭遇問題

### 下一步規劃
```

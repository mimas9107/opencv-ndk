---
name:            "SPEC.md"
description:     "opencv-ndk 設計規格書 — 將 OpenCV 移植至 Android NDK"
created_date:    "2026/06/02 13:33:16"
modified_date:   "2026/06/02 17:41:22"
project_version: "0.2.2"
document_version: "1.3.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity', 'codex/GPT-5', 'gemini cli/gemini-2.0-flash']
---

# SPEC — opencv-ndk 設計規格書

## 1. 專案目標

使用 Android NDK 編譯 OpenCV 4.x，並將其整合至運行於華為 P30 Pro 的個人架設服務中。作為 MVP（最小可行性產品），初期將選定 1 至 3 個核心功能開始實作。

---

## 2. 目標運作環境

| 項目 | 規格與細節 |
|------|-----------|
| 測試裝置 | 華為 P30 Pro |
| Android 版本 | 12 (EMUI v12) |
| CPU 架構 | ARM64-v8a (主要) / armeabi-v7a (次要) |
| 最低支援 API 等級 | 26 (Android 8.0) ※ 依據 EMUI v12 實機建置經驗設定 |
| 目標 API 等級 | 31 (Android 12) |

---

## 3. 開發與建置工具鏈

| 元件名稱 | 版本與路徑資訊 |
|---------|--------------|
| OpenCV 原始碼 | 4.14.0-pre — `/home/mimas/projects/opencv/opencv` |
| opencv_contrib | `/home/mimas/projects/opencv/opencv_contrib` |
| Android NDK | r30 (30.0.14904198) — `~/Android/Sdk/ndk/30.0.14904198` |
| CMake | 3.31.6 (`/usr/bin/cmake`) |
| Ninja | 系統全域 (`/usr/bin/ninja`) |
| ADB 工具 | `/usr/bin/adb` |
| SDK 管理工具 | `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager` |
| 主機 Python | 3.13.5 (系統全域) |
| Java 環境 | OpenJDK 21 (透過環境變數 JAVA_HOME 強制指定使用) |

---

## 4. MVP 功能狀態

第一輪 MVP 已選定並完成：

| 標號 | 功能名稱 | 使用之 OpenCV 模組 | 實作難易度 |
|---|---|---|---|
| D | 相機影像擷取 + 即時灰階 preview | `imgproc` / CameraX / JNI | 已完成 |
| E | OCR 文字偵測與辨識 | `dnn` | 已完成第一輪驗證 |

後續可再評估：

| 標號 | 功能名稱 | 使用之 OpenCV 模組 |
|---|---|---|
| A | QR Code 讀取 | `objdetect` |
| B | Canny 邊緣檢測 | `imgproc` |
| C | 人臉偵測 (Haar Cascade) | `objdetect` |

---

## 5. 建置架構

```
[開發主機: Debian 13]
  OpenCV 原始碼
      │
      ▼ 執行 cmake + NDK 交叉編譯
  產出 libopencv_*.so 檔案 (arm64-v8a)
      │
      ▼ 封裝為 AAR 或撰寫 JNI 介面
  Android 應用程式 (app/)
      │
      ▼ 執行 adb install 部署
  目標裝置：華為 P30 Pro (Android 12)
```

### 5.1 階段性建置流程

本專案採用拆解式的階段性建置機制，以便於各階段獨立調試與故障排除：

1. **環境初始化**: 載入 `scripts/01_init_env.sh` 設定 `JAVA_HOME` (JDK 21) 與 NDK 參數。
2. **CMake 配置**: 執行 `scripts/02_cmake_configure.sh` 進行 NDK 交叉編譯參數檢查並生成 Ninja 建置檔。
3. **編譯源碼**: 執行 `scripts/03_compile.sh` 使用 Ninja 多線程高速編譯 C++ 原始碼。
4. **本機安裝**: 執行 `scripts/04_install.sh` 將編譯好的 `.so` 動態庫與標頭檔封裝至安裝輸出目錄。
5. **專案部署**: 執行 `scripts/05_deploy_to_app.sh` 將生成的相依連結庫自動拷貝至 App 專案的 `jniLibs` 中。
6. **一鍵串聯**: 另有主控指令稿 `scripts/run_all_stages.sh` 可一鍵完整執行上述 02 至 05 的所有程序。

---

## 6. 目錄配置規範

```
opencv-ndk/
├── scripts/            # 階段性建置腳本目錄
│   ├── 01_init_env.sh          # 階段 1 - 環境變數定義
│   ├── 02_cmake_configure.sh   # 階段 2 - CMake 交叉編譯配置
│   ├── 03_compile.sh           # 階段 3 - Ninja 多線程編譯
│   ├── 04_install.sh           # 階段 4 - 本機標頭檔與庫封裝
│   ├── 05_deploy_to_app.sh     # 階段 5 - 庫部署至 Android app/
│   ├── run_all_stages.sh       # 一鍵主控執行指令稿
│   └── build_opencv_android.sh  # 備份參考 (原一大包腳本)
├── app/                # Android 應用程式專案
│   └── src/main/
│       ├── cpp/        # JNI / C++ 包裝層代碼 (opencv-jni.cpp, CMakeLists.txt)
│       └── jniLibs/    # 已部署完成的 OpenCV .so 庫檔案 (arm64-v8a)
├── docs/               # 設計備忘與研究文獻
├── reports/            # 測試報告與變更回報 (TEST-YYYYMMDD.md)
└── build/              # CMake 獨立建置輸出目錄 (已加入 gitignore)
```

---

## 7. 品質檢驗標準

- 專案建置必須成功，且在實機上執行時，`adb logcat` 中不可出現任何崩潰 (Crash) 紀錄。
- MVP 核心功能必須能在實機相機或輸入圖像上正常運作。
- 所有的測試過程與結果皆須記錄於 `./reports/TEST-[YYYYMMDD].md`。

---

## 8. 待辦與未決事項 (TODO)

- [x] 選定第一個核心 MVP 功能。 -> **已決定採用「選項 D：即時相機灰階影像預覽」**。
- [x] 建立 Android 專案架構 (app/)。
- [x] 整合 OpenCV 交叉編譯產出的 `.so` 庫。
- [x] 基於 Jetpack CameraX 實現相機框架，並透過 JNI 將圖像幀（YUV_420_888）傳送至 C++ 層。
- [x] C++ 層利用 OpenCV `imgproc` 將圖像轉換為灰階 (Grayscale) 並渲染回畫面。
- [x] 完成 OCR MVP：文字偵測、ROI gate、文字辨識與 UI 顯示。
- [ ] 評估採用封裝好的 AAR 形式，還是直接引用獨立 `.so` 檔。
- [ ] 確認是否需要使用 opencv_contrib 擴充模組。（初期 MVP 不需使用）。
- [ ] 制定華為 EMUI 系統限制（例如無內建 Google Play 服務）的應對機制。

---

## 9. 「選項 D：即時相機灰階影像預覽」架構規格

### 9.1 技術組成
- **前端畫面**: Android Jetpack Compose 或傳統 XML View，以最簡化 UI 呈現相機畫面。
- **相機框架**: `androidx.camera:camera-core`、`camera-camera2`、`camera-lifecycle` (CameraX)。
- **影像處理**: CameraX `ImageAnalysis.Analyzer` 介面獲取影像幀 `ImageProxy`。
- **JNI 數據交換**: 提取 `ImageProxy` 中的 Y 通道與 UV 通道 ByteBuffers 直接傳遞至 C++，避免整張點陣圖在 Java 複製產生的記憶體與效能開銷。
- **C++ 圖像變換**: 
  ```cpp
  // C++ JNI 範例邏輯
  cv::Mat yuv(height + height/2, width, CV_8UC1, pYUVData);
  cv::Mat gray;
  cv::cvtColor(yuv, gray, cv::COLOR_YUV2GRAY_NV21);
  ```
- **渲染機制**: 將灰階後的影像以 `Bitmap` 格式直接更新至畫面的 ImageView，或透過 ANativeWindow 直接在 C++ 渲染至 Surface。

---

## 10. OCR MVP 架構規格

### 10.1 模型組合

- 文字偵測: `PP-OCRv3 Text Detection`
- 文字辨識: `CRNN_CN`
- 模型來源: OpenCV 官方模型生態

### 10.2 資源與部署

- 模型放置於 `app/src/main/assets/ocr/`
- App 啟動後同步至 app 私有目錄 `filesDir/ocr`
- Debug APK 約 `105M`
- OCR assets 約 `72M`

### 10.3 Native Pipeline

```text
CameraX Y plane
    -> JNI gray frame
    -> OpenCV DNN TextDetectionModel_DB
    -> ROI gate
    -> OpenCV DNN TextRecognitionModel
    -> JSON result
    -> Android UI
```

### 10.4 第一輪收斂參數

- 最小 ROI: `64x64`
- 最大 ROI: `256x256`
- OCR dispatch throttle: `300ms`
- Preview 與 OCR 使用不同 executor，OCR 不能阻塞 preview

### 10.5 第二輪收斂參數 (v0.2.1)

- 最小 ROI: `48x60`
- 最大 ROI: `448x448`
- 偵測信心值門檻 (Confidence): `0.96`
- 新增功能: UI 可選之偵測外框繪製 (Cyan stroke)

### 10.6 中心區域偵測 (v0.2.2)

- 偵測範圍限制：僅處理影像中心 50% 寬度與 50% 高度的區域。
- 座標處理：在 Native 層自動補償裁切位移，UI 維持全圖座標顯示。

### 10.7 驗證結果

- P30 Pro 實機 app 啟動正常，無 `FATAL EXCEPTION`
- 相機權限拒絕後重新允許，App 可恢復正常
- 灰階 preview 與旋轉修正正常
- OCR UI 已顯示 `logitech (conf=0.99)`

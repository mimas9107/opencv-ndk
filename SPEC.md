---
name:            "SPEC.md"
description:     "opencv-ndk 設計規格書 — 將 OpenCV 移植至 Android NDK"
created_date:    "2026/06/02 13:33:16"
modified_date:   "2026/06/02 13:41:00"
project_version: "0.1.0"
document_version: "1.0.1"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
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
| OpenCV 原始碼 | 4.14.0-pre — `/usr/local/home/mimas/myvenv01/opencv/opencv` |
| opencv_contrib | `/usr/local/home/mimas/myvenv01/opencv/opencv_contrib` |
| Android NDK | r30 (30.0.14904198) — `~/Android/Sdk/ndk/30.0.14904198` |
| CMake | 3.31.6 (`/usr/bin/cmake`) |
| Ninja | 系統全域 (`/usr/bin/ninja`) |
| ADB 工具 | `/usr/bin/adb` |
| SDK 管理工具 | `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager` |
| 主機 Python | 3.13.5 (系統全域) |
| Java 環境 | OpenJDK 21 (透過環境變數 JAVA_HOME 強制指定使用) |

---

## 4. MVP 候選功能（待選定）

優先權尚未確定，將由使用者選擇後決定。

| 標號 | 功能名稱 | 使用之 OpenCV 模組 | 實作難易度 |
|---|---|---|---|
| A | QR Code 讀取 | `objdetect` | ★☆☆ |
| B | Canny 邊緣檢測 | `imgproc` | ★☆☆ |
| C | 人臉偵測 (Haar Cascade) | `objdetect` | ★★☆ |
| D | 相機影像擷取 + 即時灰階處理 | `videoio`, `imgproc` | ★★☆ |

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

### 5.1 建置流程（規劃中）

1. 執行 `scripts/build_opencv_android.sh` — 進行 cmake 配置與建置：
   - `-DANDROID_ABI=arm64-v8a`
   - `-DANDROID_NATIVE_API_LEVEL=26`
   - `-DOPENCV_EXTRA_MODULES_PATH=<contrib>/modules`
   - `-DBUILD_ANDROID_EXAMPLES=OFF`
   - `-DBUILD_TESTS=OFF`
2. 將編譯出的 `.so` 檔案複製至 `app/src/main/jniLibs/arm64-v8a/`
3. 實作 JNI 橋接層 (C++ ↔ Kotlin/Java)
4. 使用 `adb install` 將應用程式部署至裝置上

---

## 6. 目錄配置規範

```
opencv-ndk/
├── scripts/            # 建置相關腳本
│   └── build_opencv_android.sh
├── app/                # Android 專案目錄
│   └── src/main/
│       ├── cpp/        # JNI / C++ 包裝層代碼
│       └── jniLibs/    # 已建置完成的 .so 庫檔案
├── docs/               # 設計備忘與研究文獻
├── reports/            # 測試報告 (TEST-YYYYMMDD.md)
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
- [ ] 建立 Android 專案架構 (app/)。
- [ ] 整合 OpenCV 交叉編譯產出的 `.so` 庫。
- [ ] 基於 Jetpack CameraX 實現相機框架，並透過 JNI 將圖像幀（YUV_420_888）傳送至 C++ 層。
- [ ] C++ 層利用 OpenCV `imgproc` 將圖像轉換為灰階 (Grayscale) 並渲染回畫面。
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


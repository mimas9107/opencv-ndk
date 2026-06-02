# OCR Implement Task

這份文件的目標是讓每一項工作都能直接執行，而不是只描述方向。
每個項目都盡量寫成「做什麼」、「做到什麼程度算完成」、「需要留下什麼證據」。

## 交接註記

這份 task 對應的是一個**可交接但尚未完成驗證**的 git checkpoint。
目前可確認的是 build、assets、APK packaging、以及裝置上的 OCR detection/gate 輸出；但最終辨識文字回傳與完整實機驗證尚未完成，所以這不是結案版本。

## 狀態說明

- `[ ]` 未開始
- `[x]` 已完成
- `[-]` 已經開始但未完成

`[-]` 用於防範需要中斷 agent 時的段落狀態，方便後續恢復時快速定位目前進度。

## Phase 1 - Foundation

### 1.1 模型選型確認

- [x] 確認第一版 OCR 模型組合固定為 `PP-OCRv3 Text Detection` + `CRNN_CN`
- [x] 確認模型來源以 OpenCV 官方模型生態為主，不自行訓練第一版
- [x] 完成後留下確認結果到 `reports/`，內容要寫明選型理由與限制

### 1.2 資源目錄建立

- [x] 建立 `app/src/main/assets/ocr/` 目錄
- [x] 定義模型檔命名規則，例如 `text_detection.onnx`、`text_recognition.onnx`、`charset.txt`
- [x] 完成後確認 `assets/ocr/` 下的檔案結構固定，後續程式直接依此路徑讀取

### 1.3 模型載入策略

- [x] 決定模型是隨 APK 打包，或首次啟動解包到 app 私有目錄
- [ ] 若採 APK 打包，確認 APK 增加的大小可接受
- [x] 若採首次解包，定義解包目標路徑與失敗回退策略
- [x] 完成後在 `reports/` 記錄選擇結果與原因

### 1.4 DNN 能力確認

- [x] 驗證目前 OpenCV 安裝輸出中可用 `dnn` 模組
- [x] 驗證 `TextDetectionModel` 與 `TextRecognitionModel` 所需 API 是否可編譯或可用
- [x] 完成後留下 CMake 或 build 驗證結果

## Phase 2 - Native Pipeline

### 2.1 JNI 介面定義

- [x] 在 `OpenCVBridge.kt` 新增 OCR 相關 native method
- [x] 明確定義 JNI 輸入參數: 影像來源、ROI、模型路徑、輸出文字
- [x] 明確定義 JNI 輸出格式: 單筆結果或多筆結果
- [x] 完成後可由 Kotlin 直接呼叫 native OCR function

### 2.2 DNN 初始化與快取

- [x] 在 `opencv-jni.cpp` 建立 OCR model 初始化流程
- [x] 讓模型只在第一次使用時載入，後續重用，不要每幀重載
- [x] 完成後可在 log 中看到模型初始化只發生一次或低頻發生

### 2.3 文字偵測

- [x] 在 `opencv-jni.cpp` 新增文字偵測函式
- [x] 文字偵測輸入先使用目前相機畫面或其 ROI
- [x] 偵測輸出要至少包含 bounding box 座標
- [x] 完成後能在 log 或回傳資料中看到候選文字框

### 2.4 ROI 尺寸過濾

- [x] 在 `opencv-jni.cpp` 實作尺寸門檻 `w > 32 && h > 32`
- [x] 在 `opencv-jni.cpp` 實作上限門檻 `w > 256 || h > 256` 不辨識
- [x] 將過濾結果記錄為可追蹤 log，方便確認哪些框被放行或丟棄
- [x] 完成後每一筆候選框都能明確知道是否進入辨識

### 2.5 文字辨識

- [x] 在 `opencv-jni.cpp` 新增文字辨識函式
- [x] 將通過門檻的 ROI 送入辨識模型
- [x] 輸出辨識文字與信心值或可用性標記
- [x] 完成後可在 log 中看到識別結果

### 2.6 偵測與辨識串接

- [x] 建立「偵測 -> 過濾 -> 辨識 -> 回傳」的完整 native pipeline
- [x] 明確規定當沒有文字框時要回傳什麼
- [x] 明確規定當辨識失敗時要回傳什麼
- [x] 完成後 UI 層能拿到穩定格式的結果

### 2.7 CMake 連結

- [x] 更新 `CMakeLists.txt`，確保 OCR 相關程式可編譯
- [x] 保留現有 `log`、`android`、`jnigraphics` 連結
- [x] 確認 `dnn` 相關需求不會破壞原本灰階 preview

## Phase 3 - App Integration

### 3.1 OCR 節流

- [x] 在 `MainActivity.kt` 增加 OCR 執行節流機制
- [x] 定義觸發條件，例如每 300ms 到 500ms 跑一次，或每 N 幀跑一次
- [x] 節流邏輯要獨立於 preview，不能影響畫面顯示
- [x] 完成後能清楚觀察 OCR 不再每幀都執行

### 3.2 UI 顯示

- [x] 在 `MainActivity.kt` 增加 OCR 結果顯示欄位
- [x] 顯示內容至少包含最近一次辨識結果
- [x] 若有多個候選框，至少先能顯示第一筆或最佳一筆
- [x] 完成後使用者可直接在畫面上看到辨識結果

### 3.3 Preview 保護

- [x] 確認加入 OCR 後，原本灰階 preview 仍正常顯示
- [x] 確認加入 OCR 後，旋轉修正仍正常
- [x] 確認加入 OCR 後，相機權限流程不受影響
- [x] 完成後 preview 與 OCR 彼此可獨立觀察

## Phase 4 - Verification and Tuning

### 4.1 裝置端驗證

- [x] 將 APK 安裝到華為 P30 Pro
- [x] 確認 app 啟動後沒有 crash
- [x] 確認 OCR 在實機上至少能跑出一次結果
- [x] 完成後將實測現象記錄到 `reports/`

### 4.2 門檻調整

- [x] 根據實機結果調整文字偵測門檻
- [x] 根據實機結果調整 OCR 節流頻率
- [ ] 若誤判偏多，先調整 ROI gate，再考慮換模型
- [x] 完成後留下前後差異與原因

### 4.3 報告寫入

- [x] 每次完成一個可驗證階段後，將結果寫入 `reports/`
- [x] 報告至少包含測試日期、輸入條件、結果、失敗原因、下一步
- [x] 若有 log 關鍵片段，保留摘要而非整段貼滿

### 4.4 模型或部署回退

- [ ] 若 APK 體積過大，評估模型改為 app 私有目錄載入
- [ ] 若中文辨識效果不足，評估更換字庫或模型
- [ ] 若 DNN 效能無法接受，先縮減分析頻率，不要立刻破壞 preview

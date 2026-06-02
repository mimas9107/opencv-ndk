# OCR Implement Plan

## 交接註記

這份 plan 對應的是一個**可交接但尚未完成驗證**的 git checkpoint。
目前 OCR 主流程已接通，且裝置上可看到 detection / gate 的候選框輸出，但最終辨識文字回傳與完整實機驗證尚未收斂，因此**不能視為完成版**。

## 狀態說明

- `[ ]` 未開始
- `[x]` 已完成
- `[-]` 已經開始但未完成

`[-]` 用於防範需要中斷 agent 時的段落狀態，方便後續恢復時快速定位目前進度。

## 1. 目標

在既有的 OpenCV NDK 相機灰階即時預覽基礎上，加入 OpenCV DNN 的文字偵測與文字辨識能力，讓 App 能在畫面中辨識可讀文字，並回傳辨識結果。

本階段維持 MVP 原則：
- 先做可用版本
- 先保留現有 preview 流程穩定
- 先控制模型與算力成本
- 先做可驗證、可迭代的最小功能集合

本文件會和 `OCR_implement_task.md` 形成閉環：
- plan 定義目標、技術選擇、完成條件
- task 定義實際執行步驟與勾選狀態
- 只有當對應 task 完成，plan 才算真正收斂

## 2. 現況基線

目前專案已完成：
- CameraX 取得影像幀
- Kotlin 端將 `ImageProxy` plane 與 stride 傳入 JNI
- C++ 端以 OpenCV 做灰階與旋轉修正
- 畫面成功回寫到 `Bitmap` 顯示

因此 OCR 的加入方式，不應直接取代原本 preview，而應作為第二條分析管線疊加上去。

## 3. 技術路線

### 3.1 建議模型組合

採用 OpenCV 官方模型生態系中的兩段式 OCR：

- 文字偵測: `PP-OCRv3 Text Detection`
- 文字辨識: `CRNN_CN`

官方參考來源：
- https://docs.opencv.org/4.x/d4/d43/tutorial_dnn_text_spotting.html
- https://huggingface.co/opencv/text_detection_ppocr
- https://huggingface.co/opencv/text_recognition_crnn

### 3.2 為什麼不用單步到位

OpenCV DNN 的文字辨識 API 本質上是建立在文字區塊已經被切出來的前提下。
因此直接拿整張相機畫面去做辨識，會有：
- 雜訊太多
- 誤判率偏高
- 不易做尺寸門檻控制
- 算力成本高

所以應拆成：
1. 偵測文字區塊
2. 過濾 ROI 尺寸
3. 對合格 ROI 做辨識

## 4. 尺寸門檻規則

你指定的規則會作為第一版 gate：

- `w > 32 px` 且 `h > 32 px` 才進入辨識
- `w > 256 px` 或 `h > 256 px` 不辨識

這個條件會放在偵測後、辨識前。

目的：
- 避免太小的假陽性框
- 避免過大的無意義框
- 控制辨識成本
- 讓辨識結果更穩定

## 5. 架構設計

### 5.1 不破壞現有 preview

現有 preview 保持原樣：
- CameraX 仍持續擷取
- 原本灰階畫面仍持續顯示

OCR 改成背景分析：
- 低頻率執行
- 可節流
- 結果回寫 UI 的文字區塊

### 5.2 建議資料流

```text
CameraX ImageProxy
    -> 灰階預覽路徑 (維持現況)
    -> OCR 分析路徑
        -> 文字偵測 DNN
        -> ROI 尺寸過濾
        -> 文字辨識 DNN
        -> 去重 / 聚合
        -> UI 顯示結果
```

### 5.3 建議節流策略

OCR 不要每幀都跑，建議：
- 每 300ms 至 500ms 執行一次
- 或每 10 幀分析一次
- 或當畫面靜止程度較高時才提升頻率

第一版建議採固定節流，實作簡單，容易驗證。

## 6. 實作切點

### 6.1 Kotlin 層

預計改動：
- `MainActivity.kt`
- `OpenCVBridge.kt`

職責：
- 保持現有 preview
- 增加 OCR 啟動與節流控制
- 接收 JNI 回傳的文字結果
- 更新 UI 上的 OCR 顯示欄位

### 6.2 C++ 層

預計改動：
- `app/src/main/cpp/opencv-jni.cpp`
- `app/src/main/cpp/CMakeLists.txt`

職責：
- 建立 DNN model 初始化流程
- 執行文字偵測
- 對偵測框做尺寸過濾
- 對合格 ROI 執行文字辨識
- 回傳辨識字串與框資訊

### 6.3 模型與資源

預計新增：
- `app/src/main/assets/`
- 模型檔 `.onnx`
- charset 檔

資源管理原則：
- 先能跑
- 再談縮包與最佳化
- 若 APK 體積過大，再評估首次啟動解包到 app 私有目錄

## 7. 第一版輸出形式

第一版 OCR 先不要追求完整排版理解，先輸出：
- 偵測到的文字框數量
- 每個框的文字內容
- 每個框的信心或可用性狀態

UI 顯示可先採：
- 目前最新辨識結果
- 或一個簡單的結果清單

## 8. 風險與限制

### 8.1 模型字庫限制

`CRNN_CN` 不是完整通用繁中字庫。
如果你要的是廣泛的繁體中文場景，後續可能需要：
- 換模型
- 自建字庫
- 或訓練專用模型

### 8.2 性能風險

文字偵測 + 辨識都屬於 DNN 推論，對手機算力有壓力。
所以必須節流，不能和 preview 同頻率硬跑。

### 8.3 解析度風險

目前 preview 使用 `640x480`，這對部分小字可能不夠。
若偵測品質不足，第二版才考慮提高分析解析度或做 ROI 放大。

## 9. 驗收標準

第一版完成時，至少要滿足：
- 現有灰階 preview 正常
- OCR 不造成明顯卡頓或崩潰
- 能夠偵測畫面中的文字區塊
- 符合尺寸門檻的區塊會進入辨識
- 可在 UI 上看到辨識結果

## 10. 建議執行順序

1. 導入模型資源與載入路徑
2. 建立 DNN 偵測 API
3. 建立 ROI 尺寸過濾
4. 建立文字辨識 API
5. 加入節流機制
6. 加入 UI 顯示與結果去重
7. 實機調整閾值與性能

## 11. 任務閉環控制

### 11.1 角色分工

- `OCR_implement_plan.md` 是設計與收斂依據
- `OCR_implement_task.md` 是執行與驗收清單
- 若 task 的完成狀態改變，plan 也要同步更新可驗證的內容

### 11.2 收斂規則

1. plan 中每一個核心技術決策，都必須能在 task 中找到對應項目。
2. 若實作中發現模型、效能、API 介面或資料流有變動，先修 plan，再修 task，再進入程式碼修改。
3. task 的 `[ ]` 只有在對應程式碼、建置或實機驗證完成後才能勾掉。
4. 若某個 task 長期無法完成，優先回頭檢查 plan 是否缺少可執行拆解，而不是只持續堆疊任務。
5. 任何新加入的 OCR 子功能，都必須先進 plan，再拆成 task。

### 11.3 對應矩陣

| Plan 區塊 | 對應 Task 範圍 | 收斂重點 |
|---|---|---|
| 第 3 節 技術路線 | Phase 1 - 1.1 / 1.4, Phase 2 - 2.3 / 2.5 | 模型選型、偵測 / 辨識流程是否一致 |
| 第 4 節 尺寸門檻規則 | Phase 2 - 2.4 | ROI gate 是否真的落在 native 層 |
| 第 5 節 架構設計 | Phase 3 - 3.1 / 3.2 / 3.3 | preview 與 OCR 是否解耦、是否節流 |
| 第 6 節 實作切點 | Phase 2 - 2.1 / 2.2 / 2.7, Phase 3 - 3.1 | Kotlin / C++ / CMake 是否都已接上 |
| 第 7 節 第一版輸出形式 | Phase 2 - 2.5 / 2.6, Phase 3 - 3.2 | 是否可在 UI 與報告中驗證結果 |
| 第 8 節 風險與限制 | Phase 4 - 4.2 / 4.4 | 模型與部署策略是否需要回修 |
| 第 9 節 驗收標準 | Phase 4 - 4.1 / 4.2 / 4.3 | 是否完成裝置端驗證與調參 |

### 11.4 完成判定

本 OCR MVP 只有在以下兩者都成立時，才視為完成：
- `OCR_implement_task.md` 中與 MVP 直接相關的項目已勾選完成
- 對應的 plan 章節已被實測結果或建置結果支撐，而不是只停留在假設

## 12. 參考來源

- OpenCV DNN Text Spotting Tutorial
  - https://docs.opencv.org/4.x/d4/d43/tutorial_dnn_text_spotting.html
- OpenCV `TextDetectionModel` API
  - https://docs.opencv.org/4.x/javadoc/org/opencv/dnn/TextDetectionModel.html
- OpenCV `TextRecognitionModel` API
  - https://docs.opencv.org/4.x/javadoc/org/opencv/dnn/TextRecognitionModel.html
- OpenCV Zoo
  - https://github.com/opencv/opencv_zoo
- OpenCV Text Detection PP-OCR model card
  - https://huggingface.co/opencv/text_detection_ppocr
- OpenCV Text Recognition CRNN model card
  - https://huggingface.co/opencv/text_recognition_crnn

---
name:            "TEST-20260602-rotation-fix.md"
description:     "opencv-ndk 專案即時影像預覽轉向修正測試報告"
created_date:    "2026/06/02 14:29:11"
modified_date:   "2026/06/02 14:29:11"
project_version: "0.1.1"
document_version: "1.0.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
---

# 即時影像預覽轉向修正報告 (TEST-20260602-rotation-fix)

本報告記錄了解決「手機直立持握 (Portrait) 時相機畫面旋轉 90 度」問題的分析過程、實作方案與建置驗證結果。

---

## 1. 問題描述與根因分析

### 1.1 現象
- 在華為 P30 Pro 上運行 MVP（即時灰階影像預覽）時，手持方向為 **Portrait（直向）**，但畫面渲染出的影像卻是向左（逆時針）偏轉了 90 度的狀態。
- 影像實際解析度為 `720x720`（正方形），JNI 處理耗時維持在 `~2ms`。

### 1.2 根因
- Android 手機內建的相機感測器（Camera Sensor）在硬體組裝上的「自然方向」通常是橫向 (Landscape)。
- 直立持握時，感測器捕捉到的影像資料實質上已經逆時針旋轉了 90 度。
- CameraX 的 `ImageProxy` 在 `imageInfo.rotationDegrees` 中提供了正確的偏轉補償角度（直握時通常是 270 或 90 度），但專案最初的 JNI 影像處理層直接取用 Y 亮度通道並貼圖，忽略了該角度的修正，導致顯示畫面偏移。

---

## 2. 解決方案設計

### 2.1 Kotlin 端：動態解析度對調
在 [MainActivity.kt](file:///home/mimas/project/opencv-ndk/app/src/main/java/com/example/opencvndk/MainActivity.kt) 中：
- 獲取 `image.imageInfo.rotationDegrees`。
- 如果旋轉角度為 90 或 270 度，代表渲染出的圖像其寬與高會與原始影像幀互換。
- 據此，動態分配 `Bitmap.createBitmap(targetWidth, targetHeight, ...)`，將寬高對調。
- 將 `rotationDegrees` 作為參數傳遞予 JNI 接口。

### 2.2 C++ JNI 原生層：利用 OpenCV 高效旋轉
在 [opencv-jni.cpp](file:///home/mimas/project/opencv-ndk/app/src/main/cpp/opencv-jni.cpp) 中：
- 接收 `jint rotation_degrees`。
- 在提取 Y 亮度通道建置成 `cv::Mat mat_y` 後，調用 OpenCV 的 `cv::rotate()` 進行矩陣高速變換：
  - `90` 度 -> `cv::ROTATE_90_CLOCKWISE`
  - `180` 度 -> `cv::ROTATE_180`
  - `270` 度 -> `cv::ROTATE_90_COUNTERCLOCKWISE`
- 輸出至鎖定的 Bitmap 記憶體時，結構尺寸動態變更為旋轉後的維度 `mat_y.rows` (高) 與 `mat_y.cols` (寬)。

---

## 3. 建置測試結果

- **建置指令**: `./gradlew assembleDebug` (強設 JDK 21)
- **建置狀態**: **建置成功 (BUILD SUCCESSFUL)**。
- **建置耗時**: 10 秒 (增量編譯與重新連結)。
- **產出包位置**: `app/build/outputs/apk/debug/app-debug.apk` (約 39.5 MB)。
- **變更點提交**: 程式碼與文件變更已成功納入 Git 版本管理系統。

---

## 4. 實機二次驗證建議

請執行以下步驟重新安裝並驗證：

```bash
# 1. 確保手機 USB 連接正常
adb devices

# 2. 覆蓋安裝新版 APK
adb install -r /home/mimas/project/opencv-ndk/app/build/outputs/apk/debug/app-debug.apk
```

**驗證重點:**
1. 直立持握 (Portrait) 手機時，ImageView 上的即時灰階相機預覽應為**正立且方向正確**的畫面。
2. 由於 `cv::rotate` 是在高度優化的 OpenCV 矩陣層上執行，請觀察底部效能統計：
   - JNI 影像處理耗時應依然維持在極低的水準（預期在 `~2.5ms` 左右）。
   - FPS 幀率應保持穩定流暢。

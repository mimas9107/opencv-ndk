---
title: "OCR Preview Protection 靜態檢查"
date: 2026-06-02
description: "確認 OCR 加入後 preview 路徑未被取代，且分析工作已獨立於顯示管線"
---

# OCR Preview Protection 靜態檢查

## 檢查範圍

- [MainActivity.kt](/usr/local/home/mimas/project/opencv-ndk/app/src/main/java/com/example/opencvndk/MainActivity.kt)
- [opencv-jni.cpp](/usr/local/home/mimas/project/opencv-ndk/app/src/main/cpp/opencv-jni.cpp)
- [activity_main.xml](/usr/local/home/mimas/project/opencv-ndk/app/src/main/res/layout/activity_main.xml)

## 靜態結論

- preview 仍由 `processFrameToGray()` 負責，沒有被 OCR 取代
- OCR 由 `dispatchOcrIfNeeded()` 透過獨立 executor 執行
- OCR 已加節流，不會每幀觸發
- OCR 發生例外時，`ocrInFlight` 會在 `finally` 中釋放，避免卡死後續分析
- UI 仍保留原本灰階 preview 與狀態列，只是增加 OCR 結果欄位

## 尚未完成的驗證

- 尚未在 P30 Pro 實機確認 preview 畫面與 OCR 同時運作
- 旋轉修正已在實機路徑與既有 rotation fix 報告中確認，但仍建議保留回歸測項
- 相機權限流程已做靜態檢查，實機的拒絕 / 重試回歸仍未完整跑過
- 尚未量測加入 OCR 後的實際 FPS 與延遲變化

---
title: "OCR Permission Flow 靜態檢查"
date: 2026-06-02
description: "確認加入 OCR 後，相機權限流程沒有被 OCR pipeline 直接干擾"
---

# OCR Permission Flow 靜態檢查

## 檢查範圍

- [MainActivity.kt](/usr/local/home/mimas/project/opencv-ndk/app/src/main/java/com/example/opencvndk/MainActivity.kt)

## 靜態結論

- 權限請求仍在 `onCreate()` 之後、相機啟動之前執行
- `allPermissionsGranted()` 為唯一判斷入口，沒有被 OCR 流程改寫
- 權限通過後才會進入 `startCamera()`
- 權限拒絕時會提示並 `finish()`，沒有改成 OCR 專用分支
- OCR 的 `syncOcrAssetsToPrivateDir()` 與 `dispatchOcrIfNeeded()` 都不會攔截權限流程本身

## 實機狀態

- 已確認「授權後可正常啟動相機並進入 preview / OCR」。
- 尚未完整回歸「先拒絕，再重新授權」的流程，因此這一項維持 `[-]`。

## 待補驗證

- 拒絕權限一次後，重新授權是否仍可恢復 preview 與 OCR
- 拒絕時是否仍維持合理提示文字


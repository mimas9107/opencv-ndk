---
title: "OCR 實機 logcat 驗證紀錄"
date: 2026-06-02
description: "P30 Pro 上傳後的首輪 logcat 驗證摘要"
---

# OCR 實機 logcat 驗證紀錄

## 版本註記

這份紀錄對應一個**可交接但尚未完成驗證**的版本點。
它足以證明 OCR detection 與 ROI gate 在 P30 Pro 上有正常運作，但還不能單獨當作最終辨識完成的證據。

## 結論摘要

- App 可正常啟動
- 沒有 `FATAL EXCEPTION`
- OCR 模型成功載入到 `/data/user/0/com.example.opencvndk/files/ocr`
- OCR pipeline 有正常執行，且在 `/tmp/log-opencv-ocr0.txt` 中可觀察到持續的候選框輸出
- 這份新的證據比先前的 `/tmp/log-opencv-ocr.txt` 更完整，能直接證明 detection 與尺寸 gate 已在裝置上運作
- 裝置 UI 已成功顯示辨識結果 `#1 logitech (conf=0.99)`
- 因此 `4.1` 的最低驗證目標已達成，後續只剩 preview 保護與門檻調整的收斂工作

## 觀察到的關鍵訊號

- `MainActivity: OCR assets 已同步至: /data/user/0/com.example.opencvndk/files/ocr`
- `OpenCV-NDK-JNI: OCR 模型已載入: /data/user/0/com.example.opencvndk/files/ocr`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 2`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 25`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 41`
- `OpenCV-NDK-JNI: OCR 候選框 #... 放行`
- `OpenCV-NDK-JNI: OCR 候選框 #... 被略過: 太小`
- UI 顯示 `#1 logitech (conf=0.99)`

## 目前狀態

- preview 與權限流程未出現 crash
- OCR detection 管線已可穩定進入候選框輸出階段
- ROI 尺寸過濾也已在裝置上生效，能明確區分放行與略過
- OCR 最小可用結果已在 UI 上可見
- 下一個待確認點是 preview 保護與門檻調整是否需要收斂

## 下一步

- 確認 preview、旋轉修正與權限流程仍維持正常
- 依實機觀察決定是否需要調整 detection 門檻或 OCR 節流頻率
- 若辨識場景有波動，再補充更多不同文字樣本做回歸驗證

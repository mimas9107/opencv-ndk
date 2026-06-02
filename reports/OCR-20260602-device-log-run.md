---
title: "OCR 實機 logcat 驗證紀錄"
date: 2026-06-02
description: "P30 Pro 上傳後的首輪 logcat 驗證摘要"
---

# OCR 實機 logcat 驗證紀錄

## 版本註記

這份紀錄對應一個**可交接但尚未完成驗證**的版本點。
它足以證明 OCR detection 與 ROI gate 在 P30 Pro 上有正常運作，但還不能單獨當作最終辨識完成的證據。

## 測試條件

- 裝置: Huawei P30 Pro, Android 12 / EMUI v12
- 畫面內容: 鏡頭對準滑鼠上的 `Logitech` 字樣
- 執行狀態: 既有 OCR pipeline 與模型已完成載入，並以 logcat 觀察輸出

## 結果與限制

- 主結果: UI 可穩定顯示 `#1 logitech (conf=0.99)`
- 輔助結果: logcat 可看到同一文字場景下重複輸出 `logitech`
- 限制: 偶爾會有次要小框被辨識成單字元 `t`，這屬於場景噪音與候選框多樣性，不影響主結果確認
- 後續已將 ROI gate 尺寸門檻調整為 `64x64`，優先抑制這類次要小框誤判

## 結論摘要

- App 可正常啟動
- 沒有 `FATAL EXCEPTION`
- OCR 模型成功載入到 `/data/user/0/com.example.opencvndk/files/ocr`
- OCR pipeline 有正常執行，且在 `/tmp/log-opencv-ocr0.txt` 中可觀察到持續的候選框輸出
- 這份新的證據比先前的 `/tmp/log-opencv-ocr.txt` 更完整，能直接證明 detection 與尺寸 gate 已在裝置上運作
- 裝置 UI 已成功顯示辨識結果 `#1 logitech (conf=0.99)`
- 因此 `4.1` 的最低驗證目標已達成
- `3.3` preview 保護已完成，`4.2` 的 ROI gate 與節流調整也已完成第一輪收斂

## 觀察到的關鍵訊號

- `MainActivity: OCR assets 已同步至: /data/user/0/com.example.opencvndk/files/ocr`
- `OpenCV-NDK-JNI: OCR 模型已載入: /data/user/0/com.example.opencvndk/files/ocr`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 2`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 25`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 41`
- `OpenCV-NDK-JNI: OCR 候選框 #... 放行`
- `OpenCV-NDK-JNI: OCR 候選框 #... 被略過: 太小`
- UI 顯示 `#1 logitech (conf=0.99)`
- `OCR 辨識結果 #0: text="logitech" conf=0.994 rect=(216,395,190,69) usable=true`
- `OCR 辨識摘要: candidate=2 accepted=1 usable=1`

## 補充 logcat 片段

- `OCR 辨識結果 #0: text="logitech" conf=0.994 rect=(216,395,190,69) usable=true`
- `OCR 辨識摘要: candidate=2 accepted=1 usable=1`
- `OCR 辨識結果 #0: text="logitech" conf=0.995 rect=(213,396,190,68) usable=true`
- `OCR 辨識結果 #1: text="t" conf=0.928 rect=(258,64,60,66) usable=true`
- `OCR 辨識摘要: candidate=2 accepted=2 usable=2`
- `OCR 辨識結果 #0: text="logitech" conf=0.995 rect=(214,396,190,68) usable=true`
- `OCR 辨識結果 #1: text="t" conf=0.863 rect=(259,66,59,62) usable=true`
- `OCR 辨識摘要: candidate=1 accepted=1 usable=1`

## 目前狀態

- preview 與權限流程未出現 crash
- OCR detection 管線已可穩定進入候選框輸出階段
- ROI 尺寸過濾也已在裝置上生效，能明確區分放行與略過
- OCR 最小可用結果已在 UI 上可見
- OCR 辨識在同一文字場景下可重複輸出 `logitech`
- ROI gate 已由 `32x32` 收緊到 `64x64`
- OCR dispatch throttle 已由 `400ms` 下修到 `300ms`

## 下一步

- 若辨識場景有波動，再補充更多不同文字樣本做回歸驗證
- 若效能壓力變高，再回調 OCR dispatch throttle

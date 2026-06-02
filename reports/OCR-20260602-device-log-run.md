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
- 目前 log 還沒有直接顯示最終辨識文字結果，因此「偵測成功」與「辨識文字已成功回傳」仍需分開看待

## 觀察到的關鍵訊號

- `MainActivity: OCR assets 已同步至: /data/user/0/com.example.opencvndk/files/ocr`
- `OpenCV-NDK-JNI: OCR 模型已載入: /data/user/0/com.example.opencvndk/files/ocr`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 2`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 25`
- `OpenCV-NDK-JNI: OCR 偵測候選框數量: 41`
- `OpenCV-NDK-JNI: OCR 候選框 #... 放行`
- `OpenCV-NDK-JNI: OCR 候選框 #... 被略過: 太小`

## 目前狀態

- preview 與權限流程未出現 crash
- OCR detection 管線已可穩定進入候選框輸出階段
- ROI 尺寸過濾也已在裝置上生效，能明確區分放行與略過
- 下一個待確認點是辨識文字是否有被穩定回傳到 UI 或 log

## 下一步

- 直接確認 UI 上最近一次 OCR 結果是否有更新
- 如果要從 log 追認辨識結果，補一個更明確的 recognition log 或將回傳 JSON 內容印出來
- 若 UI 沒有穩定更新，再針對節流頻率與文字場景做調整

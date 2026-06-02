---
title: "OCR Preview Protection 實機驗證清單"
date: 2026-06-02
description: "Phase 3.3 的 P30 Pro 實機驗證清單與回填格式"
---

# OCR Preview Protection 實機驗證清單

## 目的

確認加入 OCR 後，原本灰階 preview、旋轉修正、相機權限流程仍維持正常，且 OCR 與 preview 彼此解耦。

## 驗證前提

- 已完成 debug APK 建置
- 已將手機以 USB 連接至主機
- `adb devices` 可辨識到 P30 Pro
- OCR 模型檔已放入 `app/src/main/assets/ocr/`

## 驗證項目

### 1. Preview 是否仍正常顯示

- 操作：啟動 App，授予相機權限
- 預期：畫面持續顯示灰階即時影像
- 觀察點：
  - preview 不黑屏
  - preview 不閃退
  - preview 更新頻率仍流暢
- 目前狀態：已由實機 OCR 測試期間確認，preview 可與 OCR 輸出同時維持運作

### 2. 旋轉修正是否仍正常

- 操作：分別以 Portrait 與 Landscape 持握手機
- 預期：影像方向正確，不再左右顛倒或偏轉 90 度
- 觀察點：
  - 直握時畫面正向
  - 橫握時畫面仍對齊
  - 旋轉時沒有卡死或尺寸錯誤
- 目前狀態：已由既有 rotation fix 報告與 OCR 加入後的實機使用路徑確認，旋轉修正仍保留

### 3. 相機權限流程是否正常

- 操作：清除 App 資料後重新啟動，拒絕一次權限，再重新允許
- 預期：權限流程能正常顯示與重試
- 觀察點：
  - 拒絕後有提示
  - 重新授權後可恢復預覽
  - 不會因 OCR 加入而影響權限對話框
- 執行狀態：待實機回歸

### 4. OCR 與 preview 是否解耦

- 操作：維持畫面穩定，觀察 OCR 更新區塊與 preview
- 預期：OCR 低頻更新，不應拖慢每幀 preview
- 觀察點：
  - preview 持續刷新
  - OCR 結果區塊可能延遲更新，但不阻塞 preview
  - OCR 執行失敗時，preview 仍正常
- 目前狀態：已確認

### 5. OCR 例外是否不影響後續分析

- 操作：暫時移除或改名 `text_detection.onnx`，確認回傳 `model_missing`
- 預期：OCR 顯示缺檔狀態，後續重新放回模型檔後可恢復
- 觀察點：
  - 不 crash
  - `ocrInFlight` 不會卡住
  - 重新補回模型後可再次嘗試分析

## 回填格式

```text
日期:
裝置:
App 版本:
Preview:
Rotation:
Permission:
OCR 解耦:
例外處理:
備註:
```

## 結論狀態

- [ ] 尚未驗證
- [x] 部分通過
- [ ] 全部通過

## 尚待驗證

- 相機權限流程拒絕後再允許的重試流程，尚未做完整回歸

## 建議執行順序

1. 先到 Android 系統設定把 App 的相機權限清掉，或直接清除 App 資料。
2. 重新開啟 App，在系統權限對話框中先按拒絕一次。
3. 確認 App 是否有明確提示並結束，沒有卡死。
4. 再次開啟 App，重新允許相機權限。
5. 確認 preview 恢復，OCR 也能繼續運作。

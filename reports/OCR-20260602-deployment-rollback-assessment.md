---
title: "OCR 部署體積與回退評估"
date: 2026-06-02
description: "確認 OCR APK 體積、模型部署策略與第一版回退條件"
---

# OCR 部署體積與回退評估

## APK 與模型體積

- Debug APK: `105M`
- OCR assets 目錄: `72M`
- `text_detection.onnx`: `2.4M`
- `text_recognition.onnx`: `70M`
- `charset.txt`: `16K`

## 部署判斷

- 第一版目標是 P30 Pro 上的個人自用 / self-host 測試，不是上架發佈版。
- 目前 APK 體積可接受，保留「模型隨 APK 打包，首次啟動同步到 app 私有目錄」策略。
- 暫不改成外部下載或遠端模型部署，避免增加第一版失敗點。

## 模型與效能回退

- 已能穩定辨識 `logitech`，沒有觸發更換字庫或模型的條件。
- P30 Pro 回歸測試沒有觀察到特別問題，沒有觸發 DNN 效能回退。
- OCR dispatch throttle 已從 `400ms` 調整為 `300ms`，若後續觀察到壓力再回調。
- ROI gate 已從 `32x32` 收緊到 `64x64`，優先以 gate 收斂誤判，不更換模型。

## 後續觸發條件

- 若 release APK 體積需要壓縮，再評估模型外部下載或分離部署。
- 若繁體中文場景效果不足，再評估替換模型、字庫或建立專用模型。
- 若 preview FPS 明顯下降或 OCR 延遲不可接受，先調高節流間隔，不直接破壞 preview。


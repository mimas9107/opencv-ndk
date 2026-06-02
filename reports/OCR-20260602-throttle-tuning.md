---
title: "OCR 節流頻率調整紀錄"
date: 2026-06-02
description: "根據實機 OCR log 與辨識穩定度，調整 OCR dispatch throttle"
---

# OCR 節流頻率調整紀錄

## 調整前

- `OCR_THROTTLE_MS = 400`
- OCR 已可在 P30 Pro 上穩定輸出結果，但更新節奏偏保守

## 調整後

- `OCR_THROTTLE_MS = 300`

## 原因

- 實機 log 已證明 OCR pipeline 可穩定回傳 `logitech`
- 在 preview 仍正常的前提下，適度降低節流可讓 UI 上的 OCR 更新更即時
- 300ms 仍保留節流空間，不會退回到每幀觸發

## 預期差異

- OCR 結果更新會比 400ms 更即時
- preview 與 OCR 解耦不應受影響
- 若後續觀察到 CPU 或延遲壓力，再考慮回調到更保守值


---
title: "OCR 實機快速檢查表"
date: 2026-06-02
description: "上機測試時可直接照做的最短版清單"
---

# OCR 實機快速檢查表

## 先決條件

- `bash scripts/08_build_app_debug.sh` 已成功
- `bash scripts/06_verify_ocr_assets.sh` 已成功
- 手機已開啟 USB 偵錯
- `adb devices` 看得到 P30 Pro

## 安裝

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

若要先清舊版：

```bash
adb uninstall com.example.opencvndk
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 啟動後先看三件事

- App 不 crash
- 相機權限有跳出
- 灰階 preview 有畫面

## OCR 先看兩件事

- OCR 欄位不是長期卡在 `model_missing`
- OCR 更新不應阻塞 preview

## logcat 快速檢查

```bash
adb logcat -v time | rg "OpenCV-NDK-JNI|MainActivity|AndroidRuntime|FATAL EXCEPTION"
```

判讀：
- `AndroidRuntime` / `FATAL EXCEPTION` 出現 = 直接視為 crash
- `model_missing` = 模型檔未正確進入 `filesDir/ocr/`
- `OCR 初始化失敗` = 模型路徑、字庫或載入流程有問題
- `OCR 辨識完成` = 第一筆 OCR 已跑通

## 測試結論欄位

```text
日期:
裝置:
安裝:
啟動:
Preview:
Rotation:
Permission:
OCR:
Logcat:
結論:
```


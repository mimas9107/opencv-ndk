---
title: "OCR 實機部署與跑測 Runbook"
date: 2026-06-02
description: "Phase 4.1 的 P30 Pro 安裝與初步跑測操作步驟"
---

# OCR 實機部署與跑測 Runbook

## 目的

把 debug APK 安裝到華為 P30 Pro，確認 App 能啟動、權限正常、preview 正常、OCR 至少能跑出一次結果。

## 前置條件

- 主機上已完成 APK 建置
- 手機開啟開發者模式與 USB 偵錯
- USB 線連線正常
- `adb devices` 可見裝置
- OCR 模型檔已準備好

## 1. 安裝前檢查

```bash
adb devices
```

預期：
- 裝置狀態為 `device`
- 不應顯示 `unauthorized`

## 2. 建置 debug APK

- 確認 `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`
- 確認 Gradle wrapper 版本為 `8.5`

```bash
bash scripts/08_build_app_debug.sh
```

預期：
- 產出 `app/build/outputs/apk/debug/app-debug.apk`

## 3. 覆蓋安裝

```bash
adb install -r /usr/local/home/mimas/project/opencv-ndk/app/build/outputs/apk/debug/app-debug.apk
```

預期：
- 安裝成功
- 若失敗，先確認版本簽章與舊版殘留資料

## 4. 啟動 App

- 直接點開 App
- 或透過 `adb shell monkey` / launcher 啟動

預期：
- App 不 crash
- 相機權限請求正常出現

## 5. 初始觀察

- 授予相機權限
- 觀察灰階 preview 是否正常
- 觀察底部狀態列是否持續更新 FPS / JNI 時間
- 觀察 OCR 結果區塊是否顯示 `model_missing` 或實際結果

## 6. logcat 觀察

建議抓以下 tag：

```bash
adb logcat | rg "OpenCV-NDK-JNI|MainActivity|AndroidRuntime"
```

重點：
- 不應出現 `AndroidRuntime FATAL EXCEPTION`
- 不應出現 JNI load failure
- OCR 初始化失敗應以可讀訊息回傳，而不是讓 App crash
- 若畫面有 OCR 但沒結果，優先看是否出現 `model_missing`
- 若 preview 正常但 OCR 卡住，優先看 `OCR 初始化失敗` 或 `OCR 執行失敗`

## 7. 成功判準

- App 可成功啟動
- preview 正常
- 旋轉修正正常
- 相機權限流程正常
- OCR 至少有一次回傳結果，或至少能明確回傳模型缺檔狀態

## 8. 實測紀錄欄位

```text
日期:
裝置:
APK 版本:
安裝結果:
啟動結果:
Preview:
Rotation:
Permission:
OCR 首次結果:
Logcat 摘要:
下一步:
```

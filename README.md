---
name:            "README.md"
description:     "OpenCV NDK porting project — build OpenCV 4.x for Android (Huawei P30 Pro / EMUI 12)"
created_date:    "2026/06/02 13:29:51"
modified_date:   "2026/06/02 13:29:51"
project_version: "0.1.0"
document_version: "1.0.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
---

# opencv-ndk

> Android 上で OpenCV を動かすための NDK ビルド・ポーティングプロジェクト。

## 概要

Huawei P30 Pro (Android 12 / EMUI v12) をターゲットに OpenCV 4.x を NDK でビルドし、
自己ホストサービスへ組み込むことを目的とした MVP プロジェクト。  
まず 1〜3 機能の MVP を動かし、そこから汎用アプリへ育てていく方針。

## ディレクトリ構成（初期）

```
opencv-ndk/
├── AGENTS.md          # AI agent 向けプロジェクト定義
├── README.md          # 本ファイル
├── CHANGELOG.md       # 変更履歴
├── SPEC.md            # 設計仕様
├── MEMOIR.md          # 開発メモ・学習ログ
├── docs/              # 追加ドキュメント
├── scripts/           # ビルド補助スクリプト
├── app/               # Android アプリソース（今後）
└── reports/           # テスト・ビルドレポート
```

## 環境情報

| 項目 | 値 |
|------|-----|
| Host OS | Debian 13 (Linux 6.12.90+deb13.1) |
| Desktop | Wayland + Sway WM |
| Python | 3.13.5 (system-wide) |
| Java | OpenJDK 21 (Gradle 推奨、明示的に指定して使用) |
| CMake | 3.31.6 |
| Android NDK | r30 (30.0.14904198) — `~/Android/Sdk/ndk/30.0.14904198` |
| Android SDK | `~/Android/Sdk` |
| SDK Manager | `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager` |
| ADB | `/usr/bin/adb` |
| Android Studio | `/opt/android-studio` |
| OpenCV source | `/usr/local/home/mimas/myvenv01/opencv/opencv` (v4.14.0-pre) |
| opencv_contrib | `/usr/local/home/mimas/myvenv01/opencv/opencv_contrib` |
| Target device | Huawei P30 Pro, Android 12, EMUI v12 |
| Project dir | `/home/mimas/project/opencv-ndk` |

## クイックスタート（予定）

```bash
# 1. NDK ビルド設定確認
ls ~/Android/Sdk/ndk/30.0.14904198/

# 2. OpenCV を Android 向けにビルド（スクリプト未作成）
# scripts/build_opencv_android.sh

# 3. ADB で接続確認
adb devices
```

## 参考リンク

- [OpenCV Android SDK ビルドガイド](https://docs.opencv.org/4.x/d0/d76/tutorial_arm_crosscompile_with_cmake.html)
- [OpenCV platforms/android/](file:///usr/local/home/mimas/myvenv01/opencv/opencv/platforms/android/)
- NDK config 例: `ndk-25.config.py` (platforms/android/)

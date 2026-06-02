---
name:            "MEMOIR.md"
description:     "opencv-ndk 開発メモ・学習ログ・トラブルシュート記録"
created_date:    "2026/06/02 13:33:16"
modified_date:   "2026/06/02 13:33:16"
project_version: "0.1.0"
document_version: "1.0.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
---

# MEMOIR — 開発メモ・学習ログ

このファイルは「なぜそうしたか」「何がハマったか」を記録する場所。
後から自分や AI agent が読んで文脈を素早く復元できるように書く。

---

## 2026-06-02 — プロジェクト初期化

### 調査結果まとめ

**環境スキャン (Antigravity 実施):**

| 発見事項 | 内容 |
|---------|------|
| NDK パス | `~/Android/Sdk/ndk/30.0.14904198` — r30 が既インストール済み |
| ADB | `/usr/bin/adb` — システムパスに存在 |
| CMake | 3.31.6 (`/usr/bin/cmake`) — システム cmake が十分新しい |
| Ninja | `/usr/bin/ninja` — 利用可能 |
| sdkmanager | `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager` |
| OpenCV | 4.14.0-pre (master-ish) — `/usr/local/home/mimas/myvenv01/opencv/opencv` |
| opencv_contrib | 同ディレクトリ配下 `opencv_contrib/` |
| Java | **JDK 21** を使用 (Gradle 推奨環境。環境変数にて明示指定) |

**Java バージョンの決定プロセス:**
- システムには JDK 21 (update-alternatives デフォルト) と JDK 25 (現在のシェル環境のデフォルト) の両方が存在。
- Android Gradle Plugin (AGP) や Android 開発ツールチェーンは最新の JDK 25 には未対応である可能性が高く、ビルドの安定性を最優先するため **JDK 21** (パス: `/usr/lib/jvm/java-21-openjdk-amd64`) を本プロジェクトの標準 JDK として決定した。

**NDK platforms/android/ に既存の設定ファイル:**
- `ndk-25.config.py`, `ndk-22.config.py` などがある。
  r30 用の設定は存在しないが、`ndk-25.config.py` を参考に流用できる可能性あり。

### 判断・方針メモ

- **Java 環境の制御**: 今後実装するすべてのビルドスクリプトにおいて、実行前に `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` を明示し、意図しない JDK 25 での Gradle 動作を防止する。
- NDK r30 は API 26+ をサポート。Huawei P30 Pro は Android 12 (API 31) → 問題なし。
- `build_sdk.py` (platforms/android/) を使う方法と、
  直接 cmake で cross-compile する方法の 2 択がある。
  → **まず cmake 直接ビルドから試す**（依存が少なくデバッグしやすい）。
- Huawei EMUI v12 は GooglePlay 非搭載。APK サイドローディングで対応。
- opencv_contrib は初期 MVP では **不要**の可能性が高い（基本モジュールで完結できる）。

### 次のアクション

1. MVP 機能を 1 つ確定する（SPEC.md §4 参照）
2. `scripts/build_opencv_android.sh` を作成する
3. ビルド → adb install → 実機動作確認

---

## テンプレート（以後のエントリー用）

```
## YYYY-MM-DD — <トピック>

### 背景

### やったこと

### 結果・気づき

### 次のアクション
```

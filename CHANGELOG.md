---
name:            "CHANGELOG.md"
description:     "opencv-ndk プロジェクト変更履歴"
created_date:    "2026/06/02 13:29:51"
modified_date:   "2026/06/02 13:29:51"
project_version: "0.1.0"
document_version: "1.0.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
---

# CHANGELOG

すべての注目すべき変更をこのファイルに記録する。
フォーマットは [Keep a Changelog](https://keepachangelog.com/ja/1.0.0/) に準拠。
バージョニングは 10 進位ルール（PATCH/MINOR が 10 に達したら繰り上げ）に従う。

---

## [0.1.0] — 2026-06-02

### Added
- プロジェクト初期化 (AI agent: Antigravity)
- README.md — 環境情報・ディレクトリ構成
- CHANGELOG.md — 変更履歴（本ファイル）
- SPEC.md — 設計仕様初版
- MEMOIR.md — 開発メモ初版
- AGENTS.md — AI agent 向けプロジェクト定義（既存）

---

## [Unreleased]

### Planned
- `scripts/build_opencv_android.sh` — NDK ビルドスクリプト
- OpenCV 4.14.0 Android AAR ビルド検証
- MVP 機能選定（顔検出 / エッジ検出 / QRコード読み取り のいずれか）
- ADB デプロイ & 実機テスト

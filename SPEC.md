---
name:            "SPEC.md"
description:     "opencv-ndk 設計仕様書 — Android NDK へのOpenCV ポーティング"
created_date:    "2026/06/02 13:33:16"
modified_date:   "2026/06/02 13:33:16"
project_version: "0.1.0"
document_version: "1.0.0"
agent_sign:      ['human/mimas', 'antigravity/Antigravity']
---

# SPEC — opencv-ndk 設計仕様

## 1. プロジェクト目標

OpenCV 4.x を Android NDK でビルドし、Huawei P30 Pro 上で動作する
自己ホストサービスに組み込む。MVP として 1〜3 機能から開始する。

---

## 2. ターゲット環境

| 項目 | 仕様 |
|------|------|
| デバイス | Huawei P30 Pro |
| Android | 12 (EMUI v12) |
| CPU アーキテクチャ | ARM64-v8a (主) / armeabi-v7a (副) |
| Min API Level | 26 (Android 8.0) ※ EMUI v12 の実績値から |
| Target API Level | 31 (Android 12) |

---

## 3. ビルドスタック

| コンポーネント | バージョン・パス |
|--------------|----------------|
| OpenCV source | 4.14.0-pre — `/usr/local/home/mimas/myvenv01/opencv/opencv` |
| opencv_contrib | `/usr/local/home/mimas/myvenv01/opencv/opencv_contrib` |
| Android NDK | r30 (30.0.14904198) — `~/Android/Sdk/ndk/30.0.14904198` |
| CMake | 3.31.6 (`/usr/bin/cmake`) |
| Ninja | system (`/usr/bin/ninja`) |
| ADB | `/usr/bin/adb` |
| SDK Manager | `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager` |
| Host Python | 3.13.5 (system) |
| Java | OpenJDK 21 (環境変数 JAVA_HOME にて明示指定して使用) |

---

## 4. MVP 機能候補（要選定）

優先度は未確定。ユーザーが選択する。

| # | 機能 | OpenCV モジュール | 難易度 |
|---|------|-----------------|--------|
| A | QR コード読み取り | `objdetect` | ★☆☆ |
| B | エッジ検出 (Canny) | `imgproc` | ★☆☆ |
| C | 顔検出 (Haar Cascade) | `objdetect` | ★★☆ |
| D | カメラキャプチャ + グレースケール | `videoio`, `imgproc` | ★★☆ |

---

## 5. ビルドアーキテクチャ

```
[Host: Debian 13]
  OpenCV source
      │
      ▼ cmake + NDK cross-compile
  libopencv_*.so  (arm64-v8a)
      │
      ▼ AAR / JNI ラッパー
  Android アプリ (app/)
      │
      ▼ adb install
  Huawei P30 Pro (Android 12)
```

### 5.1 ビルドフロー（予定）

1. `scripts/build_opencv_android.sh` — cmake configure + build
   - `-DANDROID_ABI=arm64-v8a`
   - `-DANDROID_NATIVE_API_LEVEL=26`
   - `-DOPENCV_EXTRA_MODULES_PATH=<contrib>/modules`
   - `-DBUILD_ANDROID_EXAMPLES=OFF`
   - `-DBUILD_TESTS=OFF`
2. `.so` を `app/src/main/jniLibs/arm64-v8a/` へ配置
3. JNI ブリッジ実装 (C++ ↔ Kotlin/Java)
4. `adb install` でデプロイ

---

## 6. ディレクトリ規約

```
opencv-ndk/
├── scripts/            # ビルドスクリプト群
│   └── build_opencv_android.sh
├── app/                # Android アプリプロジェクト
│   └── src/main/
│       ├── cpp/        # JNI / C++ ラッパー
│       └── jniLibs/    # ビルド済み .so
├── docs/               # 設計メモ・調査結果
├── reports/            # TEST-YYYYMMDD.md
└── build/              # cmake out-of-source ビルド (git ignored)
```

---

## 7. 品質基準

- ビルドが成功し、実機で `adb logcat` にクラッシュがないこと
- MVP 機能が実機カメラ / 画像で動作すること
- `reports/TEST-YYYYMMDD.md` にテスト結果を記録すること

---

## 8. 未決事項（TODO）

- [ ] MVP 機能を 1 つに絞る
- [ ] AAR ビルド vs. 生 `.so` ビルドの選択
- [ ] opencv_contrib モジュールの要否確認
- [ ] Huawei EMUI 制限（GooglePlay 非搭載）への対応方針

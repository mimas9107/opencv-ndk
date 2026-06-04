#!/usr/bin/env bash

# ==============================================================================
# 階段 01 — 環境變數與建置路徑初始化 (共用配置)
# ==============================================================================

# 1. 強制設定 JDK 21 環境 (解決 Gradle 與 Android 工具鏈相容性)
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export PATH="${JAVA_HOME}/bin:${PATH}"

# 2. 定義核心專案與原始碼路徑
export OPENCV_SOURCE_DIR="/home/mimas/projects/opencv/opencv"
export OPENCV_CONTRIB_DIR="/home/mimas/projects/opencv/opencv_contrib"
export ANDROID_NDK_ROOT="/home/mimas/Android/Sdk/ndk/30.0.14904198"
export PROJECT_ROOT="/home/mimas/projects/opencv-ndk"

# 3. 定義編譯與安裝輸出目錄
export BUILD_DIR="${PROJECT_ROOT}/build/opencv_android_arm64"
export INSTALL_DIR="${PROJECT_ROOT}/build/opencv_android_install"

# 印出當前配置以利除錯
echo ">>> [環境變數初始化完成] <<<"
echo "JAVA_HOME:       ${JAVA_HOME}"
echo "ANDROID_NDK:     ${ANDROID_NDK_ROOT}"
echo "OpenCV Source:   ${OPENCV_SOURCE_DIR}"
echo "Build Temp:      ${BUILD_DIR}"
echo "Install Dir:     ${INSTALL_DIR}"
echo "------------------------------------------------"

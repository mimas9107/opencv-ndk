#!/usr/bin/env bash

set -euo pipefail

export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export PATH="${JAVA_HOME}/bin:${PATH}"

# 使用本機已存在的 Gradle 8.5 wrapper 分發包快取。
export GRADLE_USER_HOME="/tmp/codex-gradle"

PROJECT_ROOT="/usr/local/home/mimas/project/opencv-ndk"
cd "${PROJECT_ROOT}"

echo ">>> Build env <<<"
echo "JAVA_HOME=${JAVA_HOME}"
echo "GRADLE_USER_HOME=${GRADLE_USER_HOME}"
echo "Gradle wrapper should resolve to 8.5"

./gradlew :app:assembleDebug


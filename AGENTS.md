---
name:            "AGENTS.md"
description:     "AI Agent 行為準則與專案啟動指引"
created_date:    "2026/06/02 13:29:51"
modified_date:   "2026/06/06 14:12:00"
project_version: "0.2.6"
document_version: "1.7.0"
agent_sign:      ['human/mimas', 'gemini cli/gemini-2.0-flash']
---

# AI Agent 專案行為準則 (AGENTS.md)

> **重要啟動指令**：
> 在開始任何任務之前，請優先閱讀 [mapping.md](./mapping.md)。
> 這份「專案導航地圖」能讓你以最快速度理解專案架構、關鍵檔案進入點以及開發工作流，避免大規模的代碼掃描。

## 專案概要
- **專案名稱**: opencv-ndk
- **主要目標**: 將 OpenCV 4.x 移植至 Android NDK 環境，並在實機（華為 P30 Pro）執行。
- **文件語言**: 繁體中文。
- **開發策略**: MVP (Minimum Viable Product) 優先，從核心功能（如 Preview、OCR）逐步擴展。

## 軟硬體開發環境
- **主機 OS**: Debian 13 (Linux).
- **目標手機**: Huawei P30 Pro, Android 12, EMUI v12.
- **關鍵路徑**:
  - 專案根目錄: `/home/mimas/projects/opencv-ndk`
  - OpenCV 原始碼: `/home/mimas/projects/opencv/opencv`
  - Android Studio: `/opt/android-studio`

## AI Agent 工作原則
1. **導航優先**: 凡事優先查閱 `mapping.md`。
2. **規格對齊**: 修改前務必確認 `SPEC.md` 與 `OCR_implement_task.md` 的當前進度。
3. **實機意識**: 所有變動皆需考慮 Android 實機權限、NDK 編譯環境與 EMUI 系統限制。
4. **紀錄變更**: 完成重要里程碑後，請更新 `CHANGELOG.md` 並在 `reports/` 建立對應測試報告。

# OCR assets

把第一版 OCR 模型檔放在這個目錄下，檔名固定如下：

- `text_detection.onnx`
- `text_recognition.onnx`
- `charset.txt`

說明：

- 這些檔案會在 App 啟動時自動同步到 `filesDir/ocr/`
- `MainActivity.kt` 只會讀取這三個檔名
- 其他檔案不會參與 OCR 載入流程

建議放置流程：

1. 直接執行 `scripts/07_download_ocr_models.sh`
2. 重新建置 debug APK
3. 安裝到裝置
4. 啟動 App 後確認 OCR 顯示欄位不再只停留在缺檔狀態

如果還沒有正式模型檔，可先保留此 README 與 `.gitkeep`，等模型到位再補檔。

下載來源：

- 文字偵測: `https://huggingface.co/opencv/text_detection_ppocr`
- 文字辨識: `https://huggingface.co/opencv/text_recognition_crnn`
- 字庫: `https://huggingface.co/opencv/text_recognition_crnn` (建議使用 commit `4fcc8b234f71acb1f88c464aab7c792856ad6365` 的 `charset_3944_CN.txt`)

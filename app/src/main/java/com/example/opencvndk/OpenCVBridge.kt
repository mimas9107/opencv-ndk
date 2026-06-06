package com.example.opencvndk

import android.graphics.Bitmap
import java.nio.ByteBuffer

object OpenCVBridge {

    init {
        // 載入我們編譯的 C++ Native 庫
        System.loadLibrary("opencvndk")
    }

    /**
     * 調用 C++ NDK 高效影像處理
     *
     * @param yPlane      CameraX 的 Y 通道 ByteBuffer
     * @param uPlane      CameraX 的 U 通道 ByteBuffer
     * @param vPlane      CameraX 的 V 通道 ByteBuffer
     * @param yRowStride  Y 的 Row Stride
     * @param uvRowStride UV 的 Row Stride
     * @param uvPixelStride UV 的 Pixel Stride
     * @param width       影像原始寬度
     * @param height      影像原始高度
     * @param rotationDegrees 影像旋轉角度 (0, 90, 180, 270)
     * @param outBitmap   接收處理結果的 ARGB_8888 格式 Bitmap
     */
    external fun processFrameToGray(
        yPlane: ByteBuffer,
        uPlane: ByteBuffer,
        vPlane: ByteBuffer,
        yRowStride: Int,
        uvRowStride: Int,
        uvPixelStride: Int,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        outBitmap: Bitmap
    )

    /**
     * 在 native 層執行 OCR，回傳 JSON 格式結果字串。
     *
     * @param yPlane      CameraX 的 Y 通道 ByteBuffer
     * @param yRowStride  Y 的 Row Stride
     * @param width       影像原始寬度
     * @param height      影像原始高度
     * @param rotationDegrees 影像旋轉角度 (0, 90, 180, 270)
     * @param modelDir    app 私有目錄中的 OCR 模型路徑
     * @param useChinese  是否保留中文結果
     * @param useEnglish  是否保留英文結果
     * @param useNumbers  是否保留數字結果
     */
    external fun runOcrOnGrayFrame(
        yPlane: ByteBuffer,
        yRowStride: Int,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        modelDir: String,
        useChinese: Boolean,
        useEnglish: Boolean,
        useNumbers: Boolean
    ): String
}

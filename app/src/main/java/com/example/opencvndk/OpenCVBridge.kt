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
     * @param width       影像寬度
     * @param height      影像高度
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
        outBitmap: Bitmap
    )
}

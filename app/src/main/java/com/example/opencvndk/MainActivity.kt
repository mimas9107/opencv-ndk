package com.example.opencvndk

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.opencvndk.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService

    // 用於 JNI 寫入並顯示的雙緩衝 Bitmap
    private var outputBitmap: Bitmap? = null

    // 效能計算輔助變數
    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 檢查並請求相機權限
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // 用於綁定相機生命週期
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 影像分析配置 (ImageAnalysis)
            // 設定輸出分辨率為最適合即時處理的 640x480 以兼顧效能與精細度
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageFrame(imageProxy)
            }

            // 預設使用後置鏡頭
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 在重新綁定之前解綁所有用例
                cameraProvider.unbindAll()

                // 將用例綁定至生命週期
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis
                )
                binding.textStatus.text = "相機初始化完畢，開始擷取即時幀..."
            } catch (exc: Exception) {
                Log.e(TAG, "相機綁定失敗", exc)
                binding.textStatus.text = "相機綁定失敗: ${exc.message}"
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageFrame(image: ImageProxy) {
        val startTime = System.currentTimeMillis()

        // 1. 初始化輸出 Bitmap (僅在尺寸變更或首次載入時建立，防止記憶體碎片化)
        val width = image.width
        val height = image.height

        if (outputBitmap == null || outputBitmap!!.width != width || outputBitmap!!.height != height) {
            outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            Log.d(TAG, "建立輸出點陣圖尺寸: ${width}x${height}")
        }

        // 2. 獲取 YUV 圖像通道
        val planes = image.planes
        val yPlane = planes[0].buffer
        val uPlane = planes[1].buffer
        val vPlane = planes[2].buffer

        // 3. 呼叫 NDK/OpenCV 進行灰階化處理
        outputBitmap?.let { bitmap ->
            OpenCVBridge.processFrameToGray(
                yPlane = yPlane,
                uPlane = uPlane,
                vPlane = vPlane,
                yRowStride = planes[0].rowStride,
                uvRowStride = planes[1].rowStride,
                uvPixelStride = planes[1].pixelStride,
                width = width,
                height = height,
                outBitmap = bitmap
            )

            // 4. 將結果渲染回 UI 畫面
            runOnUiThread {
                binding.imagePreview.setImageBitmap(bitmap)

                // 計算每幀耗時與 FPS
                val processTime = System.currentTimeMillis() - startTime
                frameCount++
                val now = System.currentTimeMillis()
                if (now - lastFpsTimestamp >= 1000) {
                    val fps = frameCount * 1000.0 / (now - lastFpsTimestamp)
                    binding.textStatus.text = String.format(
                        "解析度: %dx%d | JNI處理: %d ms | FPS: %.1f",
                        width, height, processTime, fps
                    )
                    frameCount = 0
                    lastFpsTimestamp = now
                }
            }
        }

        // 5. 必須關閉 image 釋放 CameraX 的緩衝區，否則會阻塞下一幀的解析
        image.close()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "請授予相機權限以啟用灰階影像即時預覽", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

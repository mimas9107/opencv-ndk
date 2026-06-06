package com.example.opencvndk

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var ocrExecutor: ExecutorService
    private lateinit var ocrModelDir: File

    // 用於 JNI 寫入並顯示的雙緩衝 Bitmap
    private var outputBitmap: Bitmap? = null

    // 效能計算輔助變數
    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()
    private var lastOcrDispatchTimestamp = 0L
    private val ocrInFlight = AtomicBoolean(false)
    
    // OCR 偵測結果顯示相關
    private val showDetections = AtomicBoolean(false)
    private val latestDetections = CopyOnWriteArrayList<Rect>()
    private val detectionPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val OCR_THROTTLE_MS = 300L
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        ocrExecutor = Executors.newSingleThreadExecutor()
        ocrModelDir = File(filesDir, "ocr").apply { mkdirs() }
        syncOcrAssetsToPrivateDir()
        binding.textOcrResult.text = "OCR 模型準備中..."

        binding.switchShowDetections.setOnCheckedChangeListener { _, isChecked ->
            showDetections.set(isChecked)
            if (!isChecked) {
                latestDetections.clear()
            }
        }

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

        // 1. 獲取相機幀偏轉角
        val rotationDegrees = image.imageInfo.rotationDegrees

        // 2. 依據旋轉角度，決定輸出點陣圖 (Bitmap) 的寬與高
        // 若偏轉為 90 或 270 度，目標點陣圖的寬度與高度需要對調
        val width = image.width
        val height = image.height
        val targetWidth = if (rotationDegrees == 90 || rotationDegrees == 270) height else width
        val targetHeight = if (rotationDegrees == 90 || rotationDegrees == 270) width else height

        if (outputBitmap == null || outputBitmap!!.width != targetWidth || outputBitmap!!.height != targetHeight) {
            outputBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            Log.d(TAG, "建立輸出點陣圖尺寸 (適配旋轉 ${rotationDegrees}°): ${targetWidth}x${targetHeight}")
        }

        // 3. 獲取 YUV 圖像通道
        val planes = image.planes
        val yPlane = planes[0].buffer
        val uPlane = planes[1].buffer
        val vPlane = planes[2].buffer

        // 4. 呼叫 NDK/OpenCV 進行旋轉與灰階化處理
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
                rotationDegrees = rotationDegrees,
                outBitmap = bitmap
            )

            // 若開啟偵測外框顯示，則在 Bitmap 上繪製
            if (showDetections.get()) {
                val canvas = Canvas(bitmap)
                latestDetections.forEach { rect ->
                    canvas.drawRect(rect, detectionPaint)
                }
            }

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
                        Locale.US,
                        "解析度: %dx%d | JNI處理: %d ms | FPS: %.1f",
                        width, height, processTime, fps
                    )
                    frameCount = 0
                    lastFpsTimestamp = now
                }
            }
        }

        dispatchOcrIfNeeded(
            yPlane = yPlane,
            yRowStride = planes[0].rowStride,
            width = width,
            height = height,
            rotationDegrees = rotationDegrees
        )

        // 5. 必須關閉 image 釋放 CameraX 的緩衝區，否則會阻塞下一幀的解析
        image.close()
    }

    private fun dispatchOcrIfNeeded(
        yPlane: ByteBuffer,
        yRowStride: Int,
        width: Int,
        height: Int,
        rotationDegrees: Int
    ) {
        val now = System.currentTimeMillis()
        if (now - lastOcrDispatchTimestamp < OCR_THROTTLE_MS) {
            return
        }
        if (!ocrInFlight.compareAndSet(false, true)) {
            return
        }

        lastOcrDispatchTimestamp = now

        val copiedBuffer = duplicateDirectBuffer(yPlane)
        ocrExecutor.execute {
            try {
                val ocrStart = System.currentTimeMillis()
                val resultJson = try {
                    OpenCVBridge.runOcrOnGrayFrame(
                        yPlane = copiedBuffer,
                        yRowStride = yRowStride,
                        width = width,
                        height = height,
                        rotationDegrees = rotationDegrees,
                        modelDir = ocrModelDir.absolutePath
                    )
                } catch (e: Throwable) {
                    Log.e(TAG, "OCR 執行失敗", e)
                    buildOcrErrorJson(e.message ?: "unknown error")
                }
                val ocrElapsed = System.currentTimeMillis() - ocrStart

                runOnUiThread {
                    binding.textOcrResult.text = formatOcrSummary(resultJson, ocrElapsed)
                }
            } finally {
                ocrInFlight.set(false)
            }
        }
    }

    private fun duplicateDirectBuffer(source: ByteBuffer): ByteBuffer {
        val duplicate = source.duplicate()
        duplicate.rewind()
        val copy = ByteBuffer.allocateDirect(duplicate.remaining())
        copy.put(duplicate)
        copy.flip()
        return copy
    }

    private fun syncOcrAssetsToPrivateDir() {
        val assetRoot = "ocr"
        val assetNames = runCatching { assets.list(assetRoot) }.getOrNull().orEmpty()
        if (assetNames.isEmpty()) {
            Log.w(TAG, "assets/ocr 尚未放入模型檔，OCR 會以缺檔狀態啟動。")
            return
        }

        val expectedNames = setOf("text_detection.onnx", "text_recognition.onnx", "charset.txt")
        val copyTargets = assetNames
            .filterNot { it.startsWith(".") }
            .filter { it in expectedNames }

        if (copyTargets.isEmpty()) {
            Log.w(TAG, "assets/ocr 目前只有占位檔，尚未提供可用模型。")
            return
        }

        copyTargets
            .forEach { name ->
                val target = File(ocrModelDir, name)
                if (target.exists() && target.length() > 0L) {
                    return@forEach
                }
                assets.open("$assetRoot/$name").use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

        Log.i(TAG, "OCR assets 已同步至: ${ocrModelDir.absolutePath}")
    }

    private fun formatOcrSummary(resultJson: String, ocrElapsedMs: Long): String {
        return try {
            val root = JSONObject(resultJson)
            val status = root.optString("status", "unknown")
            val message = root.optString("message", "")
            val candidateCount = root.optInt("candidateCount", 0)
            val acceptedCount = root.optInt("acceptedCount", 0)
            val results = root.optJSONArray("results") ?: JSONArray()

            // 更新偵測外框清單
            val newDetections = mutableListOf<Rect>()
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val rect = Rect(
                    item.optInt("x", 0),
                    item.optInt("y", 0),
                    item.optInt("x", 0) + item.optInt("w", 0),
                    item.optInt("y", 0) + item.optInt("h", 0)
                )
                newDetections.add(rect)
            }
            latestDetections.clear()
            latestDetections.addAll(newDetections)

            val lines = mutableListOf<String>()
            lines += "OCR: $status | candidates=$candidateCount accepted=$acceptedCount | ${ocrElapsedMs}ms"
            if (message.isNotBlank()) {
                lines += message
            }
            if (results.length() == 0) {
                lines += "目前沒有可顯示的辨識文字"
            } else {
                val maxLines = minOf(results.length(), 3)
                for (index in 0 until maxLines) {
                    val item = results.getJSONObject(index)
                    val text = item.optString("text", "")
                    val confidence = item.optDouble("detectionConfidence", Double.NaN)
                    val rect = Rect(
                        item.optInt("x", 0),
                        item.optInt("y", 0),
                        item.optInt("x", 0) + item.optInt("w", 0),
                        item.optInt("y", 0) + item.optInt("h", 0)
                    )
                    val confText = if (confidence.isNaN()) "n/a" else String.format(Locale.US, "%.2f", confidence)
                    lines += "#${index + 1} $text (conf=$confText) [${rect.left},${rect.top},${rect.width()}x${rect.height()}]"
                }
            }
            lines.joinToString("\n")
        } catch (e: JSONException) {
            Log.e(TAG, "OCR JSON 解析失敗", e)
            "OCR 回傳格式錯誤: ${e.message}\n$resultJson"
        }
    }

    private fun buildOcrErrorJson(message: String): String {
        return JSONObject()
            .put("status", "error")
            .put("message", message)
            .put("candidateCount", 0)
            .put("acceptedCount", 0)
            .put("results", JSONArray())
            .toString()
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
        ocrExecutor.shutdown()
    }
}

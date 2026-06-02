#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "OpenCV-NDK-JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

/**
 * 直接在 Native 層處理 CameraX 傳遞過來的 YUV_420_888 數據並渲染至 Android Bitmap 上。
 *
 * @param env          JNI 環境指標
 * @param thiz         呼叫此函式的 Java/Kotlin 物件
 * @param y_plane      Y 通道數據 (亮度通道)
 * @param u_plane      U 通道數據
 * @param v_plane      V 通道數據
 * @param y_row_stride Y 平面的 Row Stride
 * @param uv_row_stride UV 平面的 Row Stride
 * @param uv_pixel_stride UV 平面的 Pixel Stride (通常 NV21/YV12 格式下為 1 或 2)
 * @param width        影像寬度
 * @param height       影像高度
 * @param out_bitmap   用於顯示的 Android Bitmap 物件 (應為 ARGB_8888 格式)
 */
JNIEXPORT void JNICALL
Java_com_example_opencvndk_OpenCVBridge_processFrameToGray(
        JNIEnv *env,
        jobject thiz,
        jobject y_plane,
        jobject u_plane,
        jobject v_plane,
        jint y_row_stride,
        jint uv_row_stride,
        jint uv_pixel_stride,
        jint width,
        jint height,
        jobject out_bitmap) {

    // 1. 取得 Direct ByteBuffer 的指標
    uint8_t *y_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(y_plane));
    uint8_t *u_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(u_plane));
    uint8_t *v_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(v_plane));

    if (!y_data || !u_data || !v_data) {
        LOGE("無法獲取 Direct ByteBuffer 的記憶體指標！");
        return;
    }

    // 2. 鎖定 Android Bitmap 以便直接寫入灰階渲染結果
    AndroidBitmapInfo bitmap_info;
    void *bitmap_pixels = nullptr;

    if (AndroidBitmap_getInfo(env, out_bitmap, &bitmap_info) < 0) {
        LOGE("無法獲取 Bitmap 資訊！");
        return;
    }

    if (bitmap_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("輸出 Bitmap 格式必須為 RGBA_8888！");
        return;
    }

    if (AndroidBitmap_lockPixels(env, out_bitmap, &bitmap_pixels) < 0) {
        LOGE("鎖定 Bitmap 記憶體失敗！");
        return;
    }

    try {
        // 3. 建立 OpenCV 矩陣結構
        // 為了效能，我們只讀取 Y 通道 (亮度)。因為「灰階影像」只需要 Y 通道即可！
        // 這能夠省去複雜的 YUV420 完整顏色轉換開銷，達到極致的處理速度！
        cv::Mat mat_y(height, width, CV_8UC1);

        // 將 Y 平面的數據逐行拷貝至 cv::Mat (考慮到 rowStride 不一定等於 width 的情況)
        for (int i = 0; i < height; ++i) {
            memcpy(mat_y.ptr<uint8_t>(i), y_data + (i * y_row_stride), width);
        }

        // 4. 為了輸出到 RGBA_8888 格式的 Bitmap，我們需要將單通道的灰階 mat_y 轉為 4 通道的 RGBA
        cv::Mat mat_rgba(height, width, CV_8UC4, bitmap_pixels);
        cv::cvtColor(mat_y, mat_rgba, cv::COLOR_GRAY2RGBA);

    } catch (const cv::Exception &e) {
        LOGE("OpenCV 異常: %s", e.what());
    } catch (...) {
        LOGE("處理影像幀時發生未知異常！");
    }

    // 5. 釋放並解鎖 Bitmap 指標
    AndroidBitmap_unlockPixels(env, out_bitmap);
}

}

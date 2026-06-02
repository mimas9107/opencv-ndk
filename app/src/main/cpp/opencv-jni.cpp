#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>

#include <cstring>
#include <fstream>
#include <memory>
#include <mutex>
#include <sstream>
#include <string>
#include <vector>

#include <opencv2/core.hpp>
#include <opencv2/dnn.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "OpenCV-NDK-JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

constexpr int kMinOcrWidth = 64;
constexpr int kMinOcrHeight = 64;
constexpr int kMaxOcrWidth = 256;
constexpr int kMaxOcrHeight = 256;

struct OcrRuntime {
    std::mutex mutex;
    bool initialized = false;
    std::string loadedModelDir;
    std::string lastError;
    std::unique_ptr<cv::dnn::TextDetectionModel_DB> detector;
    std::unique_ptr<cv::dnn::TextRecognitionModel> recognizer;
};

OcrRuntime& ocrRuntime() {
    static OcrRuntime runtime;
    return runtime;
}

bool fileExists(const std::string& path) {
    std::ifstream file(path);
    return file.good();
}

std::vector<std::string> loadVocabulary(const std::string& path) {
    std::ifstream file(path);
    std::vector<std::string> vocabulary;
    std::string line;

    while (std::getline(file, line)) {
        if (!line.empty() && line.back() == '\r') {
            line.pop_back();
        }
        if (!line.empty()) {
            vocabulary.push_back(line);
        }
    }

    return vocabulary;
}

std::string joinPath(const std::string& base, const std::string& leaf) {
    if (!base.empty() && base.back() == '/') {
        return base + leaf;
    }
    return base + "/" + leaf;
}

std::string jsonEscape(const std::string& input) {
    std::ostringstream out;
    for (char c : input) {
        switch (c) {
            case '\\': out << "\\\\"; break;
            case '"': out << "\\\""; break;
            case '\b': out << "\\b"; break;
            case '\f': out << "\\f"; break;
            case '\n': out << "\\n"; break;
            case '\r': out << "\\r"; break;
            case '\t': out << "\\t"; break;
            default:
                if (static_cast<unsigned char>(c) < 0x20) {
                    out << "\\u";
                    out.width(4);
                    out.fill('0');
                    out << std::hex << static_cast<int>(static_cast<unsigned char>(c));
                    out << std::dec;
                } else {
                    out << c;
                }
        }
    }
    return out.str();
}

std::string makeJsonStatus(const std::string& status,
                           const std::string& message,
                           int candidateCount = 0,
                           int acceptedCount = 0,
                           const std::string& resultsJson = "[]",
                           long latencyMs = 0) {
    std::ostringstream out;
    out << "{";
    out << "\"status\":\"" << jsonEscape(status) << "\",";
    out << "\"message\":\"" << jsonEscape(message) << "\",";
    out << "\"candidateCount\":" << candidateCount << ",";
    out << "\"acceptedCount\":" << acceptedCount << ",";
    out << "\"latencyMs\":" << latencyMs << ",";
    out << "\"results\":" << resultsJson;
    out << "}";
    return out.str();
}

cv::Mat buildGrayFrameFromYPlane(const uint8_t* yData,
                                 int yRowStride,
                                 int width,
                                 int height,
                                 int rotationDegrees) {
    cv::Mat matY(height, width, CV_8UC1);
    for (int row = 0; row < height; ++row) {
        std::memcpy(matY.ptr<uint8_t>(row), yData + (row * yRowStride), width);
    }

    if (rotationDegrees == 90) {
        cv::rotate(matY, matY, cv::ROTATE_90_CLOCKWISE);
    } else if (rotationDegrees == 180) {
        cv::rotate(matY, matY, cv::ROTATE_180);
    } else if (rotationDegrees == 270) {
        cv::rotate(matY, matY, cv::ROTATE_90_COUNTERCLOCKWISE);
    }

    return matY;
}

cv::Rect clampRect(const cv::Rect& rect, const cv::Size& bounds) {
    cv::Rect imageRect(0, 0, bounds.width, bounds.height);
    return rect & imageRect;
}

bool initOcrRuntime(const std::string& modelDir, std::string& errorMessage) {
    auto& runtime = ocrRuntime();
    std::lock_guard<std::mutex> lock(runtime.mutex);

    if (runtime.initialized && runtime.loadedModelDir == modelDir) {
        return true;
    }

    runtime.initialized = false;
    runtime.loadedModelDir = modelDir;
    runtime.detector.reset();
    runtime.recognizer.reset();
    runtime.lastError.clear();

    const std::string detectorPath = joinPath(modelDir, "text_detection.onnx");
    const std::string recognizerPath = joinPath(modelDir, "text_recognition.onnx");
    const std::string charsetPath = joinPath(modelDir, "charset.txt");

    if (!fileExists(detectorPath)) {
        errorMessage = "缺少文字偵測模型: " + detectorPath;
        runtime.lastError = errorMessage;
        return false;
    }
    if (!fileExists(recognizerPath)) {
        errorMessage = "缺少文字辨識模型: " + recognizerPath;
        runtime.lastError = errorMessage;
        return false;
    }
    if (!fileExists(charsetPath)) {
        errorMessage = "缺少字庫檔: " + charsetPath;
        runtime.lastError = errorMessage;
        return false;
    }

    try {
        auto detector = std::make_unique<cv::dnn::TextDetectionModel_DB>(detectorPath);
        detector->setPreferableBackend(cv::dnn::DNN_BACKEND_OPENCV);
        detector->setPreferableTarget(cv::dnn::DNN_TARGET_CPU);
        detector->setBinaryThreshold(0.3f);
        detector->setPolygonThreshold(0.5f);
        detector->setUnclipRatio(1.5);
        detector->setMaxCandidates(1000);
        detector->setInputSize(cv::Size(736, 736));
        detector->setInputMean(cv::Scalar(123.675, 116.28, 103.53));
        detector->setInputScale(cv::Scalar(
            1.0 / (255.0 * 0.229),
            1.0 / (255.0 * 0.224),
            1.0 / (255.0 * 0.225)
        ));
        detector->setInputSwapRB(false);

        const auto vocabulary = loadVocabulary(charsetPath);
        if (vocabulary.empty()) {
            errorMessage = "字庫檔內容為空: " + charsetPath;
            runtime.lastError = errorMessage;
            return false;
        }

        auto recognizer = std::make_unique<cv::dnn::TextRecognitionModel>(recognizerPath);
        recognizer->setPreferableBackend(cv::dnn::DNN_BACKEND_OPENCV);
        recognizer->setPreferableTarget(cv::dnn::DNN_TARGET_CPU);
        recognizer->setDecodeType("CTC-greedy");
        recognizer->setVocabulary(vocabulary);
        recognizer->setInputSize(cv::Size(100, 32));
        recognizer->setInputMean(cv::Scalar(127.5, 127.5, 127.5));
        recognizer->setInputScale(cv::Scalar(1.0 / 127.5, 1.0 / 127.5, 1.0 / 127.5));
        recognizer->setInputSwapRB(false);
        recognizer->setInputCrop(false);

        runtime.detector = std::move(detector);
        runtime.recognizer = std::move(recognizer);
        runtime.initialized = true;

        LOGI("OCR 模型已載入: %s", modelDir.c_str());
        return true;
    } catch (const cv::Exception& e) {
        errorMessage = std::string("OpenCV OCR 初始化失敗: ") + e.what();
    } catch (const std::exception& e) {
        errorMessage = std::string("OCR 初始化失敗: ") + e.what();
    } catch (...) {
        errorMessage = "OCR 初始化失敗: unknown error";
    }

    runtime.lastError = errorMessage;
    runtime.initialized = false;
    runtime.detector.reset();
    runtime.recognizer.reset();
    return false;
}

std::string runOcrPipeline(const cv::Mat& grayFrame) {
    auto& runtime = ocrRuntime();

    cv::Mat bgrFrame;
    cv::cvtColor(grayFrame, bgrFrame, cv::COLOR_GRAY2BGR);

    std::vector<std::vector<cv::Point>> detections;
    std::vector<float> confidences;

    runtime.detector->detect(bgrFrame, detections, confidences);

    LOGI("OCR 偵測候選框數量: %zu", detections.size());

    std::vector<cv::Rect> acceptedRois;
    std::vector<std::size_t> acceptedIndices;
    acceptedRois.reserve(detections.size());
    acceptedIndices.reserve(detections.size());

    for (std::size_t i = 0; i < detections.size(); ++i) {
        const cv::Rect rawRect = cv::boundingRect(detections[i]);
        const cv::Rect rect = clampRect(rawRect, grayFrame.size());
        const float confidence = i < confidences.size() ? confidences[i] : -1.0f;

        const bool tooSmall = rect.width <= kMinOcrWidth || rect.height <= kMinOcrHeight;
        const bool tooLarge = rect.width > kMaxOcrWidth || rect.height > kMaxOcrHeight;

        if (tooSmall) {
            LOGD("OCR 候選框 #%zu 被略過: 太小 rect=(%d,%d,%d,%d) conf=%.3f",
                 i, rect.x, rect.y, rect.width, rect.height, confidence);
            continue;
        }
        if (tooLarge) {
            LOGD("OCR 候選框 #%zu 被略過: 太大 rect=(%d,%d,%d,%d) conf=%.3f",
                 i, rect.x, rect.y, rect.width, rect.height, confidence);
            continue;
        }

        LOGD("OCR 候選框 #%zu 放行: rect=(%d,%d,%d,%d) conf=%.3f",
             i, rect.x, rect.y, rect.width, rect.height, confidence);
        acceptedRois.push_back(rect);
        acceptedIndices.push_back(i);
    }

    if (acceptedRois.empty()) {
        return makeJsonStatus(
            "empty",
            "沒有通過尺寸門檻的文字區塊",
            static_cast<int>(detections.size()),
            0,
            "[]"
        );
    }

    std::vector<std::string> recognizedTexts;
    runtime.recognizer->recognize(bgrFrame, acceptedRois, recognizedTexts);

    std::ostringstream resultsJson;
    resultsJson << "[";
    std::size_t usableCount = 0;
    for (std::size_t i = 0; i < acceptedRois.size(); ++i) {
        const cv::Rect& rect = acceptedRois[i];
        const std::string text = i < recognizedTexts.size() ? recognizedTexts[i] : "";
        const float detectionConfidence = acceptedIndices[i] < confidences.size()
                                               ? confidences[acceptedIndices[i]]
                                               : -1.0f;
        const bool usable = !text.empty();

        if (usable) {
            ++usableCount;
        }

        LOGI("OCR 辨識結果 #%zu: text=\"%s\" conf=%.3f rect=(%d,%d,%d,%d) usable=%s",
             acceptedIndices[i],
             text.c_str(),
             detectionConfidence,
             rect.x,
             rect.y,
             rect.width,
             rect.height,
             usable ? "true" : "false");

        if (i > 0) {
            resultsJson << ",";
        }
        resultsJson << "{";
        resultsJson << "\"index\":" << acceptedIndices[i] << ",";
        resultsJson << "\"text\":\"" << jsonEscape(text) << "\",";
        resultsJson << "\"detectionConfidence\":" << detectionConfidence << ",";
        resultsJson << "\"usable\":" << (usable ? "true" : "false") << ",";
        resultsJson << "\"x\":" << rect.x << ",";
        resultsJson << "\"y\":" << rect.y << ",";
        resultsJson << "\"w\":" << rect.width << ",";
        resultsJson << "\"h\":" << rect.height;
        resultsJson << "}";
    }
    resultsJson << "]";

    LOGI("OCR 辨識摘要: candidate=%zu accepted=%zu usable=%zu",
         detections.size(),
         acceptedRois.size(),
         usableCount);

    return makeJsonStatus(
        "ok",
        recognizedTexts.empty() ? "已完成偵測，但未產生辨識文字" : "OCR 辨識完成",
        static_cast<int>(detections.size()),
        static_cast<int>(acceptedRois.size()),
        resultsJson.str()
    );
}

}  // namespace

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_opencvndk_OpenCVBridge_processFrameToGray(
        JNIEnv *env,
        jobject,
        jobject y_plane,
        jobject u_plane,
        jobject v_plane,
        jint y_row_stride,
        jint,
        jint,
        jint width,
        jint height,
        jint rotation_degrees,
        jobject out_bitmap) {

    uint8_t *y_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(y_plane));
    uint8_t *u_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(u_plane));
    uint8_t *v_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(v_plane));

    if (!y_data || !u_data || !v_data) {
        LOGE("無法獲取 Direct ByteBuffer 的記憶體指標！");
        return;
    }

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
        const cv::Mat matY = buildGrayFrameFromYPlane(y_data, y_row_stride, width, height, rotation_degrees);
        cv::Mat matRGBA(matY.rows, matY.cols, CV_8UC4, bitmap_pixels);
        cv::cvtColor(matY, matRGBA, cv::COLOR_GRAY2RGBA);
    } catch (const cv::Exception &e) {
        LOGE("OpenCV 異常: %s", e.what());
    } catch (...) {
        LOGE("處理影像幀時發生未知異常！");
    }

    AndroidBitmap_unlockPixels(env, out_bitmap);
}

JNIEXPORT jstring JNICALL
Java_com_example_opencvndk_OpenCVBridge_runOcrOnGrayFrame(
        JNIEnv *env,
        jobject,
        jobject y_plane,
        jint y_row_stride,
        jint width,
        jint height,
        jint rotation_degrees,
        jstring model_dir) {

    const char *modelDirChars = env->GetStringUTFChars(model_dir, nullptr);
    if (!modelDirChars) {
        return env->NewStringUTF(makeJsonStatus("error", "無法讀取模型路徑").c_str());
    }

    std::string modelDir(modelDirChars);
    env->ReleaseStringUTFChars(model_dir, modelDirChars);

    std::string initError;
    if (!initOcrRuntime(modelDir, initError)) {
        LOGW("OCR 模型初始化失敗: %s", initError.c_str());
        return env->NewStringUTF(
            makeJsonStatus("model_missing", initError, 0, 0, "[]").c_str()
        );
    }

    uint8_t *y_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(y_plane));
    if (!y_data) {
        return env->NewStringUTF(makeJsonStatus("error", "無法獲取 Y plane 記憶體指標").c_str());
    }

    try {
        const cv::Mat matY = buildGrayFrameFromYPlane(y_data, y_row_stride, width, height, rotation_degrees);
        const std::string resultJson = runOcrPipeline(matY);
        return env->NewStringUTF(resultJson.c_str());
    } catch (const cv::Exception& e) {
        LOGE("OCR pipeline OpenCV 異常: %s", e.what());
        return env->NewStringUTF(
            makeJsonStatus("error", std::string("OpenCV OCR 執行失敗: ") + e.what(), 0, 0, "[]").c_str()
        );
    } catch (const std::exception& e) {
        LOGE("OCR pipeline std::exception: %s", e.what());
        return env->NewStringUTF(
            makeJsonStatus("error", std::string("OCR 執行失敗: ") + e.what(), 0, 0, "[]").c_str()
        );
    } catch (...) {
        LOGE("OCR pipeline 發生未知異常");
        return env->NewStringUTF(
            makeJsonStatus("error", "OCR 執行失敗: unknown error", 0, 0, "[]").c_str()
        );
    }
}

}  // extern "C"

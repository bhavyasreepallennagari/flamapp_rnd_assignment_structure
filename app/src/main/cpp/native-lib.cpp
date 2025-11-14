#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "native-lib", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "native-lib", __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_flamapp_NativeProcessor_getNativeInfo(JNIEnv *env, jobject /* this */) {
    std::string info = "Native OK (no OpenCV)";
    return env->NewStringUTF(info.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_flamapp_NativeProcessor_processBitmap(JNIEnv *env, jobject /* this */, jobject bitmap) {
    if (bitmap == nullptr) return;

    AndroidBitmapInfo info;
    void* pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("AndroidBitmap_getInfo failed");
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format not RGBA_8888");
        return;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("AndroidBitmap_lockPixels failed");
        return;
    }

    uint32_t* line = (uint32_t*)pixels;
    for (int y = 0; y < info.height; y++) {
        uint32_t* px = line + y * info.width;
        for (int x = 0; x < info.width; x++) {
            uint32_t color = px[x];
            uint8_t a = (color >> 24) & 0xFF;
            uint8_t r = (color >> 16) & 0xFF;
            uint8_t g = (color >> 8) & 0xFF;
            uint8_t b = (color) & 0xFF;
            // simple grayscale
            uint8_t gray = (uint8_t)((0.2989f * r) + (0.5870f * g) + (0.1140f * b));
            uint32_t newColor = (a << 24) | (gray << 16) | (gray << 8) | gray;
            px[x] = newColor;
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

} // extern "C"

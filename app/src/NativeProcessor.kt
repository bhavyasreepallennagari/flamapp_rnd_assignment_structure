object NativeProcessor {
    init {
        System.loadLibrary("native-lib")
    }

    external fun getOpenCVVersion(): String
    external fun processBitmap(bitmap: Bitmap)
}

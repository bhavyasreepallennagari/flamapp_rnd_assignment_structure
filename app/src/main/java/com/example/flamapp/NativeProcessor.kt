package com.example.flamapp

import android.graphics.Bitmap

object NativeProcessor {
    init {
        System.loadLibrary("native-lib")
    }

    external fun processBitmap(bitmap: Bitmap) // in-place modify
    external fun getNativeInfo(): String
}

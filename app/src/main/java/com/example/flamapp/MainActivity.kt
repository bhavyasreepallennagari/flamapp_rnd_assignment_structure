package com.example.flamapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.TextureView
import android.widget.ImageView
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView
    private lateinit var processedImage: ImageView
    private lateinit var glSurface: GLSurfaceView
    private lateinit var glRenderer: GLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.textureView)
        processedImage = findViewById(R.id.processedImage)
        glSurface = findViewById(R.id.glSurface)

        // OpenGL setup
        glSurface.setEGLContextClientVersion(2)
        glRenderer = GLRenderer(this)
        glSurface.setRenderer(glRenderer)
        glSurface.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startCamera()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startCamera() {
        Camera2Helper.startCamera(
            activity = this,
            textureView = textureView
        ) { frameBitmap ->

            val copy: Bitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)

            // --- CALL C++ NATIVE CODE ---
            try {
                NativeProcessor.processBitmap(copy)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            runOnUiThread {
                // Show processed frame
                processedImage.setImageBitmap(copy)

                // Send to OpenGL renderer
                glRenderer.updateBitmap(copy)
                glSurface.requestRender()
            }
        }
    }
}

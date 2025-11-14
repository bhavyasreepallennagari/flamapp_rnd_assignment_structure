package com.example.flamapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat

object Camera2Helper {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    @SuppressLint("MissingPermission")
    fun startCamera(activity: Activity, textureView: TextureView, onFrame: (Bitmap) -> Unit) {
        startBackgroundThread()

        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.firstOrNull() ?: return

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("Camera2", "Camera permission not granted")
            return
        }

        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createPreviewSession(textureView, onFrame)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.e("Camera2", "Camera error: $error")
                camera.close()
            }
        }, backgroundHandler)
    }

    private fun createPreviewSession(textureView: TextureView, onFrame: (Bitmap) -> Unit) {
        val texture = textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(textureView.width.takeIf { it > 0 } ?: 1080,
            textureView.height.takeIf { it > 0 } ?: 1920)
        val surface = Surface(texture)

        val requestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        requestBuilder?.addTarget(surface)

        cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                session.setRepeatingRequest(requestBuilder!!.build(), null, backgroundHandler)

                // Poll the TextureView periodically on background thread
                textureView.post(object : Runnable {
                    override fun run() {
                        val bmp = textureView.bitmap
                        if (bmp != null) {
                            onFrame(bmp)
                        }
                        // queue next frame read
                        textureView.postDelayed(this, 50) // ~20 FPS read attempts
                    }
                })
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("Camera2", "Preview configure failed")
            }
        }, backgroundHandler)
    }

    private fun startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThread = HandlerThread("CameraBG").also { it.start() }
            backgroundHandler = Handler(backgroundThread!!.looper)
        }
    }
}

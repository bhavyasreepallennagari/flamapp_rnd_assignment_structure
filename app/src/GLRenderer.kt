package com.example.flamapp

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer : GLSurfaceView.Renderer {

    private var textureId = -1
    private var bitmapToRender: Bitmap? = null
    private var updateRequired = false

    private val vertexData = floatArrayOf(
        -1f, -1f, 0f, 1f,
         1f, -1f, 1f, 1f,
        -1f,  1f, 0f, 0f,
         1f,  1f, 1f, 0f
    )

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertexData).position(0) }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        textureId = createTexture()
        GLES20.glClearColor(0f,0f,0f,1f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0,width,height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (updateRequired) {
            bitmapToRender?.let {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,it,0)
            }
            updateRequired = false
        }

        drawQuad()
    }

    fun updateBitmap(bmp: Bitmap) {
        bitmapToRender = bmp
        updateRequired = true
    }

    private fun createTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val tex = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        return tex
    }

    private fun drawQuad() {
        GLES20.glEnableVertexAttribArray(0)
        GLES20.glEnableVertexAttribArray(1)

        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        vertexBuffer.position(2)
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        vertexBuffer.position(0)
    }
}

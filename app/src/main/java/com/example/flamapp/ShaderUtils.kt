package com.example.flamapp

import android.opengl.GLES20

object ShaderUtils {

    fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun createProgram(vertex: String, fragment: String): Int {
        val v = loadShader(GLES20.GL_VERTEX_SHADER, vertex)
        val f = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, v)
        GLES20.glAttachShader(program, f)
        GLES20.glLinkProgram(program)
        return program
    }
}

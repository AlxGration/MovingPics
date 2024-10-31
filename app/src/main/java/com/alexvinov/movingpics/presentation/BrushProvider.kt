package com.alexvinov.movingpics.presentation

import android.graphics.Color
import android.graphics.Paint

class BrushProvider {
    private val pen = Paint()

    init {
        pen.style = Paint.Style.STROKE
        pen.isAntiAlias = true
        setUpPen(color = Color.BLACK)
    }

    fun setUpPen(color: Int, width: Float = 3f) {
        pen.color = color
        pen.strokeWidth = width
    }

    fun pen(): Paint {
        return pen
    }
}
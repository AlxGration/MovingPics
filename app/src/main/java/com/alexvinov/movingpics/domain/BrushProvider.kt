package com.alexvinov.movingpics.domain

import android.graphics.Color
import android.graphics.Paint
import javax.inject.Inject

class BrushProvider @Inject constructor() {
    private val pen = Paint()

    init {
        pen.style = Paint.Style.STROKE
        pen.isAntiAlias = true
        setUpPen(color = Color.BLACK)
    }

    fun setUpPen(
        color: Int,
        width: Float = 3f,
    ) {
        pen.color = color
        pen.strokeWidth = width
    }

    fun pen(): Paint = pen
}

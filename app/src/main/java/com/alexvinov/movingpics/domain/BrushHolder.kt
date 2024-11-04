package com.alexvinov.movingpics.domain

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import javax.inject.Inject

class BrushHolder @Inject constructor() {
    private val pen = Paint()
    private val eraser = Paint()

    init {
        pen.style = Paint.Style.STROKE
        pen.isAntiAlias = true
        pen.strokeJoin = Paint.Join.ROUND
        setUpBrushColor(DEFAULT_BRUSH_COLOR)
        setUpBrushWidth(DEFAULT_BRUSH_WIDTH)

        eraser.style = Paint.Style.STROKE
        eraser.isAntiAlias = true
        eraser.strokeWidth = 10f
        eraser.color = Color.TRANSPARENT
        eraser.maskFilter = null
        eraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        eraser.strokeJoin = Paint.Join.ROUND
    }

    fun setUpBrushColor(color: Int) {
        pen.color = color
    }

    fun setUpBrushWidth(width: Float = DEFAULT_BRUSH_WIDTH) {
        pen.strokeWidth = width
        eraser.strokeWidth = width
    }

    fun pen(): Paint = pen

    fun eraser(): Paint = eraser

    companion object {
        const val DEFAULT_BRUSH_COLOR = Color.BLACK
        const val DEFAULT_BRUSH_WIDTH = 10f
    }
}

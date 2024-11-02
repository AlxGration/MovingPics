package com.alexvinov.movingpics.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

object BitmapUtils {
    private val paint = Paint()

    fun Bitmap.mergeWith(bitmap: Bitmap, alpha: Int): Bitmap {
        val canvas = Canvas(this)
        paint.alpha = alpha
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return this
    }
}
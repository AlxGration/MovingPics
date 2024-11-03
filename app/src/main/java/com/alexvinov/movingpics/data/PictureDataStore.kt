package com.alexvinov.movingpics.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.alexvinov.movingpics.R

class PictureDataStore(
    private val context: Context,
) {
    private var width = 1
    private var height = 1
    private var last: Bitmap? = null
    private var lastRemovePicture: Bitmap? = null

    fun initialBackgroundPicture(): Bitmap {
        val bgDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_background, null)
        val bgBitmap =
            Bitmap.createBitmap(
                bgDrawable?.intrinsicWidth ?: width,
                bgDrawable?.intrinsicHeight ?: height,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bgBitmap)

        bgDrawable?.setBounds(0, 0, canvas.width, canvas.height)
        bgDrawable?.draw(canvas)
        return bgBitmap
    }

    fun save(picture: Bitmap) {
        last = picture
        lastRemovePicture = null
    }

    fun empty(): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    fun last(): Bitmap? = last?.copy(Bitmap.Config.ARGB_8888, true)

    fun removeLast() {
        lastRemovePicture = last()
        last = null
    }

    fun lastRemovedPicture(): Bitmap? = lastRemovePicture?.copy(Bitmap.Config.ARGB_8888, true)

    fun initPictureSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }
}

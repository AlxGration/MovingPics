package com.alexvinov.movingpics.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.alexvinov.movingpics.R

class PictureStorage(
    private val context: Context,
) {

    fun lastPicture(): Bitmap {
        return initialPicture()
    }

    private fun initialPicture(): Bitmap {
        val bgDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_background, null)
        val bgBitmap =
            Bitmap.createBitmap(
                bgDrawable?.intrinsicWidth ?: 1,
                bgDrawable?.intrinsicHeight ?: 1,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bgBitmap)

        bgDrawable?.setBounds(0, 0, canvas.width, canvas.height)
        bgDrawable?.draw(canvas)
        return bgBitmap
    }
}

package com.alexvinov.movingpics.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.alexvinov.movingpics.data.PictureDataStore
import javax.inject.Inject

class PictureRepository @Inject constructor(
    private val picturesStore: PictureDataStore,
) {
    fun background(): Bitmap = picturesStore.background()

    fun emptyPicture(): Bitmap {
        return picturesStore.empty()
    }

    fun lastOrEmptyPicture(): Bitmap {
        return picturesStore.last() ?: picturesStore.empty()
    }

    fun removeLastPicture(): Bitmap {
        return picturesStore.background()
    }

    fun backgroundWithLastPicture(): Bitmap {
        val bitmap = background()
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.alpha = 60
        canvas.drawBitmap(lastOrEmptyPicture(), 0f, 0f, paint)
        return bitmap
    }

    fun savePicture(picture: Bitmap) {
        picturesStore.save(picture)
    }
}

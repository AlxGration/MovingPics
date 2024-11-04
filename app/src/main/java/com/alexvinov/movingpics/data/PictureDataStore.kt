package com.alexvinov.movingpics.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.alexvinov.movingpics.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

class PictureDataStore(
    private val context: Context,
) {
    private var width: Int? = null
    private var height: Int? = null
    private val currentSize = AtomicLong(0)
    private var lastRemovePicture: Bitmap? = null

    fun picturesSize() = currentSize.get()

    suspend fun initialBackgroundPicture(): Bitmap {
        val bgDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_background, null)
        val bgBitmap =
            Bitmap.createBitmap(
                width ?: bgDrawable?.intrinsicWidth ?: 1,
                height ?: bgDrawable?.intrinsicHeight ?: 1,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bgBitmap)

        bgDrawable?.setBounds(0, 0, canvas.width, canvas.height)
        bgDrawable?.draw(canvas)
        return bgBitmap
    }

    suspend fun save(picture: Bitmap) {
        lastRemovePicture = null
        saveToInternalStorage(picture)
    }

    suspend fun empty(): Bitmap = Bitmap.createBitmap(width ?: 1, height ?: 1, Bitmap.Config.ARGB_8888)

    suspend fun last(): Bitmap? = getPictureFromInternalStorage(currentSize.get()-1)?.copy(Bitmap.Config.ARGB_8888, true)

    suspend fun removeLast() {
        lastRemovePicture = null
        val picture = removePictureFromInternalStorage(currentSize.get()-1)
        if (picture != null){
            currentSize.decrementAndGet()
            lastRemovePicture = picture
        }
    }

    suspend fun lastRemovedPicture(): Bitmap? = lastRemovePicture?.copy(Bitmap.Config.ARGB_8888, true)

    fun initPictureSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    suspend fun removePictureFromInternalStorage(index: Long): Bitmap? {
        val filename = fileName(index)
        val file = File(context.filesDir, filename)
        val bitmap = getPictureFromInternalStorage(index)
        if (file.exists()) {
            file.delete()
        }

        return bitmap
    }

    suspend fun getPictureFromInternalStorage(index: Long): Bitmap? {
        val filename = fileName(index)
        val file = File(context.filesDir, filename)
        if (!file.exists()) return null
        return try {
            FileInputStream(file).use { ios ->
                val picture = BitmapFactory.decodeStream(ios)
                Bitmap.createScaledBitmap(
                    picture,
                    width ?: picture.width,
                    height ?: picture.height,
                    false
                ).copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (err: Throwable) {
            err.printStackTrace()
            null
        }
    }

    private suspend fun saveToInternalStorage(bitmap: Bitmap) {
        val filename = fileName(currentSize.getAndIncrement())
        val file = File(context.filesDir, filename)
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun fileName(index: Long) = "draft-$index.png"
}

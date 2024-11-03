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
    private var width = 1
    private var height = 1
    private val currentSize = AtomicLong(0)
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
        lastRemovePicture = null
        saveToInternalStorage(picture)
    }

    fun empty(): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    fun last(): Bitmap? = getPictureFromInternalStorage(currentSize.get()-1)?.copy(Bitmap.Config.ARGB_8888, true)

    fun removeLast() {
        lastRemovePicture = null
        val picture = removePictureFromInternalStorage(currentSize.get()-1)
        if (picture != null){
            currentSize.decrementAndGet()
            lastRemovePicture = picture
        }
    }

    fun lastRemovedPicture(): Bitmap? = lastRemovePicture?.copy(Bitmap.Config.ARGB_8888, true)

    fun initPictureSize(
        width: Int,
        height: Int,
    ) {
        this.width = width
        this.height = height
    }

    private fun fileName(index: Long) = "draft-$index.png"

    private fun saveToInternalStorage(bitmap: Bitmap) {
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

    private fun removePictureFromInternalStorage(index: Long): Bitmap? {
        val filename = fileName(index)
        val file = File(context.filesDir, filename)
        val bitmap = getPictureFromInternalStorage(index)
        if (file.exists()) {
            file.delete()
        }

        return bitmap
    }

    private fun getPictureFromInternalStorage(index: Long): Bitmap? {
        val filename = fileName(index)
        val file = File(context.filesDir, filename)
        if (!file.exists()) return null
        return try {
            FileInputStream(file).use { ios -> BitmapFactory.decodeStream(ios) }
        } catch (err: Throwable) {
            err.printStackTrace()
            null
        }
    }
}

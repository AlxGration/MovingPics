package com.alexvinov.movingpics.domain

import android.graphics.Bitmap
import com.alexvinov.movingpics.data.PictureDataStore
import com.alexvinov.movingpics.utils.BitmapUtils.mergeWith
import javax.inject.Inject

class PictureRepository @Inject constructor(
    private val historyHolder: HistoryHolder,
    private val picturesStore: PictureDataStore,
) {
    suspend fun addLayer(bitmap: Bitmap) = historyHolder.addLayer(bitmap)

    suspend fun nextLayer() = historyHolder.redoLastAction()

    suspend fun previousLayer(): Bitmap {
        return historyHolder.undoLastAction()
            ?: picturesStore.lastRemovedPicture()
            ?: picturesStore.empty()
    }

    suspend fun savePicture(): Boolean {
        val picture = historyHolder.lastPictureState()
        picture?.let { picture ->
            picturesStore.save(picture)
            historyHolder.clear()
        }
        return picture != null
    }

    suspend fun background(): Bitmap = picturesStore.initialBackgroundPicture()

    suspend fun emptyPicture() = picturesStore.empty()

    suspend fun removePicture() {
        historyHolder.clear()
        picturesStore.removeLast()
    }

    suspend fun lastOrEmptyPicture(): Bitmap {
        return picturesStore.last() ?: picturesStore.empty()
    }

    fun isHistoryEmpty(): Boolean = !historyHolder.hasUndoActions()

    fun hasRedoActions() = historyHolder.hasRedoActions()

    suspend fun backgroundWithLastPicture(): Bitmap {
        val background = background()
        return picturesStore.last()?.let {
            background.mergeWith(bitmap = lastOrEmptyPicture(), alpha = 60)
        } ?: background
    }

    fun initPictureSize(width: Int, height: Int) {
        picturesStore.initPictureSize(width, height)
    }
}

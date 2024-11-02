package com.alexvinov.movingpics.domain

import android.graphics.Bitmap
import com.alexvinov.movingpics.data.PictureDataStore
import com.alexvinov.movingpics.utils.BitmapUtils.mergeWith
import javax.inject.Inject

class PictureRepository @Inject constructor(
    private val historyHolder: HistoryHolder,
    private val picturesStore: PictureDataStore,
) {
    fun addLayer(bitmap: Bitmap) = historyHolder.addLayer(bitmap)

    fun nextLayer() = historyHolder.redoLastAction()

    fun previousLayer(): Bitmap {
        return historyHolder.undoLastAction()
            ?: picturesStore.lastRemovedPicture()
            ?: picturesStore.empty()
    }

    fun savePicture(): Boolean {
        val picture = historyHolder.lastPictureState()
        picture?.let { picture ->
            picturesStore.save(picture)
            historyHolder.clear()
        }
        return picture != null
    }

    fun background(): Bitmap = picturesStore.initialBackgroundPicture()

    fun emptyPicture() = picturesStore.empty()

    fun removePicture() {
        historyHolder.clear()
        picturesStore.removeLast()
    }

    fun lastOrEmptyPicture(): Bitmap {
        return picturesStore.last() ?: picturesStore.empty()
    }

    fun isHistoryEmpty(): Boolean = !historyHolder.hasUndoActions()

    fun hasRedoActions() = historyHolder.hasRedoActions()

    fun backgroundWithLastPicture(): Bitmap {
        val background = background()
        return picturesStore.last()?.let {
            background.mergeWith(bitmap = lastOrEmptyPicture(), alpha = 60)
        } ?: background
    }

    fun initPictureSize(width: Int, height: Int) {
        picturesStore.initPictureSize(width, height)
    }
}

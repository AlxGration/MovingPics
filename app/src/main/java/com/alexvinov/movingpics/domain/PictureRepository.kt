package com.alexvinov.movingpics.domain

import android.graphics.Bitmap
import com.alexvinov.movingpics.data.PictureDataStore
import com.alexvinov.movingpics.utils.BitmapUtils.mergeWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean
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
        val picture = historyHolder.lastPictureState() ?: picturesStore.lastRemovedPicture()
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

    suspend fun lastRemovedOrEmptyPicture(): Bitmap {
        return picturesStore.lastRemovedPicture() ?: picturesStore.empty()
    }

    suspend fun startAnimationFlow(): Flow<Bitmap?> {
        if (picturesStore.picturesSize() < 1) return emptyFlow()

        IS_ANIMATION_PLAYING.set(true)
        return flow {
            while (IS_ANIMATION_PLAYING.get()) {
                for (index in 0 until picturesStore.picturesSize()) {
                    emit(picturesStore.getPictureFromInternalStorage(index))
                    delay(ANIMATION_DELAY_MILLIS)
                }
            }
        }
    }

    fun stopAnimationFlow() = IS_ANIMATION_PLAYING.set(false)

    fun isAnimationPlaying() = IS_ANIMATION_PLAYING.get()

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

    suspend fun saveCurrentStateBeforeAnimation() {
        // сохраняем нарисованное, но не сохраненное
        savePicture()
    }

    suspend fun restoreStateAfterAnimation(): Bitmap {
        // delay для того, чтобы flow анимации  наверняка завершился и не запушил кадр поверх
        delay(600)
        // тк перед началом анимации сохраняли текущий кадр в стор, то востановим его для рисования
        removePicture()

        return previousLayer()
    }

    companion object {
        private const val ANIMATION_DELAY_MILLIS = 400L
        private val IS_ANIMATION_PLAYING = AtomicBoolean(false)
    }
}

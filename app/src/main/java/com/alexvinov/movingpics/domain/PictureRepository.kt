package com.alexvinov.movingpics.domain

import android.graphics.Bitmap
import com.alexvinov.movingpics.data.HistoryDataStore
import com.alexvinov.movingpics.data.PictureDataStore
import com.alexvinov.movingpics.utils.BitmapUtils.mergeWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class PictureRepository @Inject constructor(
    private val historyDataStore: HistoryDataStore,
    private val picturesStore: PictureDataStore,
) {
    suspend fun pushToHistory(bitmap: Bitmap) = historyDataStore.push(bitmap)

    suspend fun redoLastAction() = historyDataStore.redoLastAction()

    suspend fun popFromHistory() = historyDataStore.undoLastAction()

    suspend fun savePicture(): Boolean {
        // последнее состояние из истории действий
        val picture = historyDataStore.last() ?: return false
        picturesStore.save(picture)
        historyDataStore.clear()
        return true
    }

    suspend fun background(): Bitmap = picturesStore.initialBackgroundPicture()

    suspend fun emptyPicture() = picturesStore.empty()

    suspend fun removePicture() {
        historyDataStore.clear()
        // добавляем в историю операций
        picturesStore.last()?.let { picture -> historyDataStore.push(picture) }
        picturesStore.removeLast()
    }

    suspend fun lastOrEmptyPicture(): Bitmap {
        return picturesStore.last() ?: picturesStore.empty()
    }

    suspend fun startAnimationFlow(): Flow<Bitmap?> {
        if (picturesStore.picturesSize() < 1) return emptyFlow()

        IS_ANIMATION_PLAYING.set(true)
        return flow {
            while (IS_ANIMATION_PLAYING.get()) {
                for (index in 0 until picturesStore.picturesSize()) {
                    if (!IS_ANIMATION_PLAYING.get()) break
                    emit(picturesStore.getPictureFromInternalStorage(index))
                    delay(ANIMATION_DELAY_MILLIS)
                }
            }
        }
    }

    fun stopAnimationFlow() = IS_ANIMATION_PLAYING.set(false)

    fun isAnimationPlaying() = IS_ANIMATION_PLAYING.get()

    fun isHistoryEmpty(): Boolean = !historyDataStore.hasUndoActions()

    fun hasRedoActions() = historyDataStore.hasRedoActions()

    suspend fun backgroundWithLastPicture(): Bitmap {
        val background = picturesStore.initialBackgroundPicture()
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
        val picture = picturesStore.lastRemovedPicture() ?: picturesStore.empty()

        // добавляем в историю операций
        pushToHistory(picture)
        return picture
    }

    companion object {
        private const val ANIMATION_DELAY_MILLIS = 400L
        private val IS_ANIMATION_PLAYING = AtomicBoolean(false)
    }
}

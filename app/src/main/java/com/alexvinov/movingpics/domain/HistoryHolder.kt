package com.alexvinov.movingpics.domain

import android.graphics.Bitmap
import com.alexvinov.movingpics.utils.LimitedDeque
import javax.inject.Inject

class HistoryHolder @Inject constructor() {
    // единица зарезервирована для хранения последнего кадра (в случае переполнения лимита)
    private var history = LimitedDeque<Bitmap>(11)
    private var historyBackup = LimitedDeque<Bitmap>(10)

    fun addLayer(bitmap: Bitmap) {
        val bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        history.push(bitmap)
        historyBackup.clear()
    }

    fun undoLastAction(): Bitmap? {
        if (hasUndoActions()) {
            val bitmap = history.pop()
            bitmap?.let { lastLayer ->
                historyBackup.push(lastLayer)
            }
        }
        // нужно отдавать копию, а иначе вью может изменить объект, который тут хранится
        return history.last()?.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun redoLastAction(): Bitmap? {
        val bitmap = historyBackup.pop()
        bitmap?.let { nextLayer ->
            history.push(nextLayer)
        }
        // нужно отдавать копию, а иначе вью может изменить объект, который тут хранится
        return bitmap?.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun hasRedoActions() = !historyBackup.isEmpty()

    fun hasUndoActions() = !history.isEmpty()

    fun lastPictureState() = history.last()

    fun clear() {
        history.clear()
        historyBackup.clear()
    }
}

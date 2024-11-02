package com.alexvinov.movingpics.domain

import android.graphics.Bitmap
import com.alexvinov.movingpics.utils.LimitedDeque

class HistoryHolder {
    // единица зарезервирована для хранения первоначальной битмапы (бэкграунда)
    private var history = LimitedDeque<Bitmap>(11)
    private var historyBackup = LimitedDeque<Bitmap>(10)

    fun addLayer(bitmap: Bitmap) {
        val bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        history.push(bitmap)
        historyBackup.clear()
    }

    fun undoLastAction(): Bitmap? {
        if (history.size() > 1) {
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
}

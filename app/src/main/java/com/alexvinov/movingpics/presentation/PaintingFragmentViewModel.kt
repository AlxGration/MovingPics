package com.alexvinov.movingpics.presentation

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import com.alexvinov.movingpics.domain.BrushProvider
import com.alexvinov.movingpics.domain.HistoryHolder
import com.alexvinov.movingpics.domain.PictureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaintingFragmentViewModel @Inject constructor(
    private val brushHolder: BrushProvider,
    private val historyHolder: HistoryHolder,
    private val pictureRepository: PictureRepository,
) : ViewModel() {

    private val _backgroundState = MutableStateFlow(pictureRepository.background())
    val backgroundState: StateFlow<Bitmap> = _backgroundState.asStateFlow()

    private val _pictureState = MutableStateFlow(pictureRepository.emptyPicture())
    val pictureState: StateFlow<Bitmap> = _pictureState.asStateFlow()

    private val _brushState = MutableStateFlow(brushHolder.pen())
    val brushState: StateFlow<Paint> = _brushState.asStateFlow()

    private val _hasUndoActionsState = MutableStateFlow(false)
    val hasUndoActionsState: StateFlow<Boolean> = _hasUndoActionsState.asStateFlow()

    private val _hasRedoActionsState = MutableStateFlow(false)
    val hasRedoActionsState: StateFlow<Boolean> = _hasRedoActionsState.asStateFlow()

    fun addLayer(bitmap: Bitmap) {
        historyHolder.addLayer(bitmap)
        setEnablingUndoRedoActions()
    }

    fun redoLastAction() {
        historyHolder.redoLastAction()?.let { bitmap ->
            _pictureState.value = bitmap
        }
        setEnablingUndoRedoActions()
    }

    fun undoLastAction() {
        historyHolder.undoLastAction()?.let { bitmap ->
            _pictureState.value = bitmap
        }
        setEnablingUndoRedoActions()
    }

    fun setUpPen(color: Int, width: Float) {
        brushHolder.setUpPen(color, width)
        _brushState.value = brushHolder.pen()
    }

    fun pickPen(){
        _brushState.value = brushHolder.pen()
    }

    fun pickEraser(){
        _brushState.value = brushHolder.eraser()
    }

    fun addNewPicture() {
        historyHolder.lastPictureState()?.let { picture ->
            pictureRepository.savePicture(picture)
            historyHolder.clear()
            _backgroundState.value = pictureRepository.backgroundWithLastPicture()
            _pictureState.value = pictureRepository.emptyPicture()
        }
    }

    fun removePicture() {
        historyHolder.clear()
        _pictureState.value = pictureRepository.lastOrEmptyPicture()
        pictureRepository.removeLastPicture()
        _backgroundState.value = pictureRepository.backgroundWithLastPicture()
    }

    private fun setEnablingUndoRedoActions() {
        _hasRedoActionsState.value = historyHolder.hasRedoActions()
        _hasUndoActionsState.value = historyHolder.hasUndoActions()
    }
}
package com.alexvinov.movingpics.presentation.fragments

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import com.alexvinov.movingpics.domain.BrushProvider
import com.alexvinov.movingpics.domain.PictureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaintingFragmentViewModel @Inject constructor(
    private val brushHolder: BrushProvider,
    private val pictureRepository: PictureRepository,
) : ViewModel() {

    private val _backgroundState = MutableStateFlow(pictureRepository.background())
    val backgroundState: StateFlow<Bitmap> = _backgroundState.asStateFlow()

    private val _pictureState = MutableStateFlow(pictureRepository.emptyPicture())
    val pictureState: StateFlow<Bitmap> = _pictureState.asStateFlow()

    private val _brushState = MutableStateFlow(brushHolder.pen())
    val brushState: StateFlow<Paint> = _brushState.asStateFlow()

    private val _actionButtonState = MutableStateFlow(ControlsButtonsEnableState())
    val actionButtonState: StateFlow<ControlsButtonsEnableState> = _actionButtonState.asStateFlow()

    fun addLayer(bitmap: Bitmap) {
        pictureRepository.addLayer(bitmap)
        calcEnablingActionButtons()
    }

    fun initPictureSize(width: Int, height: Int) {
        pictureRepository.initPictureSize(width, height)
    }

    fun redoLastAction() {
        pictureRepository.nextLayer()?.let { picture ->
            _pictureState.value = picture
        }
        calcEnablingActionButtons()
    }

    fun undoLastAction() {
        _pictureState.value = pictureRepository.previousLayer()
        calcEnablingActionButtons()
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
        if(!pictureRepository.isHistoryEmpty()) {
            if (pictureRepository.savePicture()) {
                _backgroundState.value = pictureRepository.backgroundWithLastPicture()
                _pictureState.value = pictureRepository.emptyPicture()
                calcEnablingActionButtons()
            }
        }
    }

    fun removePicture() {
        _pictureState.value = pictureRepository.lastOrEmptyPicture()
        pictureRepository.removePicture()
        _backgroundState.value = pictureRepository.backgroundWithLastPicture()
        calcEnablingActionButtons()
    }

    private fun calcEnablingActionButtons() {
        val hasActionsHistory = !pictureRepository.isHistoryEmpty()
        _actionButtonState.value = ControlsButtonsEnableState(
            isUndoEnabled = hasActionsHistory,
            isRedoEnabled = pictureRepository.hasRedoActions(),
            isNewPictureEnabled = hasActionsHistory
        )
    }
}
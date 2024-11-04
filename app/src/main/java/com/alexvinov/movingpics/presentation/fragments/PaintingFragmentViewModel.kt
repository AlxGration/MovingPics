package com.alexvinov.movingpics.presentation.fragments

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexvinov.movingpics.domain.BrushProvider
import com.alexvinov.movingpics.domain.PictureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class PaintingFragmentViewModel @Inject constructor(
    private val brushHolder: BrushProvider,
    private val pictureRepository: PictureRepository,
) : ViewModel() {

    private val _backgroundState = MutableStateFlow(
        runBlocking { pictureRepository.background() }
    )
    val backgroundState: StateFlow<Bitmap> = _backgroundState.asStateFlow()

    private val _pictureState = MutableStateFlow(
        runBlocking { pictureRepository.emptyPicture() }
    )
    val pictureState: StateFlow<Bitmap> = _pictureState.asStateFlow()

    private val _brushState = MutableStateFlow(brushHolder.pen())
    val brushState: StateFlow<Paint> = _brushState.asStateFlow()

    private val _actionButtonState = MutableStateFlow(ControlsButtonsEnableState())
    val actionButtonState: StateFlow<ControlsButtonsEnableState> = _actionButtonState.asStateFlow()

    fun addLayer(bitmap: Bitmap) = viewModelScope.launch(Dispatchers.Default) {
        pictureRepository.addLayer(bitmap)
        calcEnablingActionButtons()
    }

    fun initPictureSize(width: Int, height: Int) {
        pictureRepository.initPictureSize(width, height)
    }

    fun redoLastAction() = viewModelScope.launch(Dispatchers.Default) {
        pictureRepository.nextLayer()?.let { picture ->
            _pictureState.value = picture
        }
        calcEnablingActionButtons()
    }

    fun undoLastAction() = viewModelScope.launch(Dispatchers.Default) {
        _pictureState.value = pictureRepository.previousLayer()
        calcEnablingActionButtons()
    }

    fun setUpPen(color: Int, width: Float) {
        brushHolder.setUpPen(color, width)
        _brushState.value = brushHolder.pen()
    }

    fun setUpBrushWidth(width: Float) {
        brushHolder.setUpBrushWidth(width)
    }

    fun pickPen() {
        _brushState.value = brushHolder.pen()
    }

    fun pickEraser() {
        _brushState.value = brushHolder.eraser()
    }

    fun addNewPicture() = viewModelScope.launch(Dispatchers.Default) {
        if (pictureRepository.savePicture()) {
            _backgroundState.value = pictureRepository.backgroundWithLastPicture()
            _pictureState.value = pictureRepository.emptyPicture()
        }
    }

    fun removePicture() = viewModelScope.launch(Dispatchers.Default) {
        _pictureState.value = pictureRepository.lastOrEmptyPicture()
        pictureRepository.removePicture()
        _backgroundState.value = pictureRepository.backgroundWithLastPicture()
        calcEnablingActionButtons()
    }

    fun copyPicture() = viewModelScope.launch(Dispatchers.Default) {
        if (pictureRepository.savePicture()) {
            _backgroundState.value = pictureRepository.backgroundWithLastPicture()
            addLayer(pictureRepository.lastOrEmptyPicture())
            calcEnablingActionButtons()
        }
    }

    fun playAnimation() = viewModelScope.launch(Dispatchers.Default) {
        pictureRepository.saveCurrentStateBeforeAnimation()
        _backgroundState.value = pictureRepository.background()
        calcEnablingActionButtons(isInAnimation = true)
        pictureRepository.startAnimationFlow().collect { picture ->
            picture?.let { picture ->
                _pictureState.value = picture
            }
        }
    }

    fun stopAnimation() = viewModelScope.launch(Dispatchers.Default) {
        pictureRepository.stopAnimationFlow()
        _pictureState.value = pictureRepository.restoreStateAfterAnimation()
        _backgroundState.value = pictureRepository.backgroundWithLastPicture()
        calcEnablingActionButtons(isInAnimation = false)
    }

    private fun calcEnablingActionButtons(isInAnimation: Boolean = false) {
        val hasActionsHistory = !pictureRepository.isHistoryEmpty()
        _actionButtonState.value = ControlsButtonsEnableState(
            isUndoEnabled = hasActionsHistory && !isInAnimation,
            isRedoEnabled = pictureRepository.hasRedoActions() && !isInAnimation,
            isNewPictureEnabled = !isInAnimation,
            isAnimationPlaying = isInAnimation,
        )
    }
}
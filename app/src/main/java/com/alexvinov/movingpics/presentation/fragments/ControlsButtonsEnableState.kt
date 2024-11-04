package com.alexvinov.movingpics.presentation.fragments

data class ControlsButtonsEnableState(
    val isUndoEnabled: Boolean = false,
    val isRedoEnabled: Boolean = false,
    val isNewPictureEnabled: Boolean = false,
    val isAnimationPlaying: Boolean = false,
)
package com.alexvinov.movingpics.presentation.fragments

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alexvinov.movingpics.databinding.FragmentFirstBinding
import com.alexvinov.movingpics.databinding.ViewControlsTopBinding
import com.alexvinov.movingpics.presentation.views.ControlColorView
import com.alexvinov.movingpics.presentation.views.PaintView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaintingFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = requireNotNull(_binding) { "binding should not be bull" }
    private var topControlsBinding: ViewControlsTopBinding? = null
        get() {
            if (field == null) field = ViewControlsTopBinding.bind(binding.root)
            return field
        }

    private val viewModel: PaintingFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        setUpControls()
        setUpViewModelListeners()
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPaint.setDrawingListener(object : PaintView.DrawingListener {
            override fun onDrawingFinished(bitmap: Bitmap) {
                viewModel.addLayer(bitmap)
            }

            override fun initViewSize(width: Int, height: Int) {
                viewModel.initPictureSize(width, height)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPaint.setDrawingListener(null)
        binding.viewControlColor.setColorSelectedListener(null)
        _binding = null
        topControlsBinding = null
    }

    private fun setUpViewModelListeners() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pictureState.collect { picture ->
                    binding.viewPaint.setPictureBitmap(picture)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.backgroundState.collect { picture ->
                    binding.viewPaint.setBackgroundBitmap(picture)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.brushState.collect { brush ->
                    binding.viewPaint.setPen(brush)
                    binding.bottomControlsContainer.colorPicker.imageTintList = ColorStateList.valueOf(brush.color)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionButtonState.collect { buttonsState ->
                    topControlsBinding?.redo?.isEnabled = buttonsState.isRedoEnabled
                    topControlsBinding?.undo?.isEnabled = buttonsState.isUndoEnabled
                    topControlsBinding?.newPicture?.isEnabled = buttonsState.isNewPictureEnabled
                    topControlsBinding?.removePicture?.isEnabled = !buttonsState.isAnimationPlaying
                    topControlsBinding?.play?.isEnabled = !buttonsState.isAnimationPlaying
                    topControlsBinding?.stop?.isEnabled = buttonsState.isAnimationPlaying
                    binding.viewPaint.setIsDrawingAllowed(!buttonsState.isAnimationPlaying)
                    binding.bottomControlsContainer.root.isVisible = !buttonsState.isAnimationPlaying
                    if (binding.viewControlColor.isVisible && buttonsState.isAnimationPlaying){
                        binding.viewControlColor.isVisible = false
                    }
                }
            }
        }
    }

    private fun setUpControls() {
        setUpTopControls()
        setUpBottomControls()
        setUpColorControls()
    }

    private fun setUpTopControls() {
        with(requireNotNull(topControlsBinding)) {
            undo.setOnClickListener {
                viewModel.undoLastAction()
            }
            redo.setOnClickListener {
                viewModel.redoLastAction()
            }
            newPicture.setOnClickListener {
                viewModel.addNewPicture()
            }
            removePicture.setOnClickListener {
                viewModel.removePicture()
            }
            play.setOnClickListener {
                viewModel.playAnimation()
            }
            stop.setOnClickListener {
                viewModel.stopAnimation()
            }
        }
    }

    private fun setUpBottomControls() {
        with(binding.bottomControlsContainer) {
            eraser.setOnClickListener {
                viewModel.pickEraser()
            }
            pen.setOnClickListener {
                viewModel.pickPen()
            }
            colorPicker.setOnClickListener {
                binding.viewControlColor.isVisible = !binding.viewControlColor.isVisible
            }
        }
    }

    private fun setUpColorControls() {
        with(binding) {
            viewControlColor.setColorSelectedListener(object : ControlColorView.OnColorSelectedListener {
                override fun onColorSelected(color: Int) {
                    viewModel.setUpPen(color, 10f)
                    bottomControlsContainer.colorPicker.imageTintList = ColorStateList.valueOf(color)
                }
            })
        }
    }
}

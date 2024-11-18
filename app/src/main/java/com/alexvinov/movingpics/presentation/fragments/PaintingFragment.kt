package com.alexvinov.movingpics.presentation.fragments

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alexvinov.movingpics.R
import com.alexvinov.movingpics.databinding.FragmentFirstBinding
import com.alexvinov.movingpics.databinding.ViewControlsTopBinding
import com.alexvinov.movingpics.presentation.views.ControlBrushSizeView
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
        binding.viewControlBrushSize.setBrushWidthChangedListener(null)
        _binding = null
        topControlsBinding = null
    }

    private fun setUpViewModelListeners() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.pictureState.collect { picture ->
                    binding.viewPaint.setPictureBitmap(picture)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.backgroundState.collect { picture ->
                    binding.viewPaint.setBackgroundBitmap(picture)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.brushState.collect { brush ->
                    binding.viewPaint.setBrush(brush)
                    binding.bottomControlsContainer.colorPicker.imageTintList = ColorStateList.valueOf(brush.color)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.actionButtonState.collect { buttonsState ->
                    topControlsBinding?.redo?.isEnabled = buttonsState.isRedoEnabled
                    topControlsBinding?.undo?.isEnabled = buttonsState.isUndoEnabled
                    topControlsBinding?.newPicture?.isEnabled = buttonsState.isNewPictureEnabled
                    topControlsBinding?.removePicture?.isEnabled = !buttonsState.isAnimationPlaying
                    topControlsBinding?.copyPicture?.isEnabled = !buttonsState.isAnimationPlaying
                    topControlsBinding?.play?.isEnabled = !buttonsState.isAnimationPlaying
                    topControlsBinding?.stop?.isEnabled = buttonsState.isAnimationPlaying
                    binding.viewPaint.setIsDrawingAllowed(!buttonsState.isAnimationPlaying)
                    binding.bottomControlsContainer.root.isVisible = !buttonsState.isAnimationPlaying
                    if (binding.viewControlColor.isVisible && buttonsState.isAnimationPlaying){
                        binding.viewControlColor.isVisible = false
                    }
                    if (binding.viewControlBrushSize.isVisible && buttonsState.isAnimationPlaying){
                        binding.viewControlBrushSize.isVisible = false
                    }
                }
            }
        }
    }

    private fun setUpControls() {
        setUpTopControls()
        setUpBottomControls()
        setUpViewListeners()
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
                Toast.makeText(requireContext(), "Layer removed", Toast.LENGTH_SHORT).show()
            }
            copyPicture.setOnClickListener {
                viewModel.copyPicture()
                Toast.makeText(requireContext(), "Layer copied", Toast.LENGTH_SHORT).show()
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
            brushSize.setOnClickListener {
                binding.viewControlColor.isVisible = false
                binding.viewControlBrushSize.isVisible = !binding.viewControlBrushSize.isVisible
                highlightControl(brushSize.id)
            }
            eraser.setOnClickListener {
                viewModel.pickEraser()
                highlightControl(eraser.id)
            }
            pen.setOnClickListener {
                viewModel.pickPen()
                highlightControl(pen.id)
            }
            colorPicker.setOnClickListener {
                binding.viewControlBrushSize.isVisible = false
                binding.viewControlColor.isVisible = !binding.viewControlColor.isVisible
                highlightControl(colorPicker.id)
            }
        }
    }

    private fun setUpViewListeners() {
        with(binding) {
            viewControlColor.setColorSelectedListener(object : ControlColorView.OnColorSelectedListener {
                override fun onColorSelected(color: Int) {
                    viewModel.setUpBrush(color = color)
                    bottomControlsContainer.colorPicker.imageTintList = ColorStateList.valueOf(color)
                }
            })
            viewControlBrushSize.setBrushWidthChangedListener(object : ControlBrushSizeView.OnBrushWidthChangedListener {
                override fun onBrushWidthChanged(width: Float) {
                    viewModel.setUpBrush(width = width)
                }
            })
        }
    }

    private fun highlightControl(id: Int) {
        with(binding.bottomControlsContainer) {
            val highlightedTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
            val plainTint = ColorStateList.valueOf(getPlainThemedColor())
            brushSize.imageTintList = if (brushSize.id == id) highlightedTint else plainTint
            eraser.imageTintList = if (eraser.id == id) highlightedTint else plainTint
            pen.imageTintList = if (pen.id == id) highlightedTint else plainTint
            colorPicker.backgroundTintList = if (colorPicker.id == id) highlightedTint else plainTint
        }
    }

    private fun getPlainThemedColor(): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }
}

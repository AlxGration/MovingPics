package com.alexvinov.movingpics.presentation

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alexvinov.movingpics.databinding.FragmentFirstBinding
import com.alexvinov.movingpics.databinding.TopControlsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaintingFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private var topControlsBinding: TopControlsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaintingFragmentViewModel by viewModels()

    private var paintingView: PaintView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        topControlsBinding = TopControlsBinding.bind(binding.root)
        setUpControls()
        setUpViewModelListeners()
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        paintingView = PaintView(requireContext())
        paintingView?.setDrawingListener(object : PaintView.DrawingListener{
            override fun onDrawingFinished(bitmap: Bitmap) {
                viewModel.addLayer(bitmap)
            }
        })

        binding.paintingContainer.addView(
            paintingView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpViewModelListeners() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pictureState.collect { picture ->
                    paintingView?.setBackgroundBitmap(picture)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.brushState.collect { brush ->
                    paintingView?.setPen(brush)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasRedoActionsState.collect { hasActions ->
                    topControlsBinding?.redo?.isEnabled = hasActions
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasUndoActionsState.collect { hasActions ->
                    topControlsBinding?.undo?.isEnabled = hasActions
                }
            }
        }
    }

    private fun setUpControls() {
        setUpTopControls()
        setUpBottomControls()
    }

    private fun setUpTopControls() {
        with(requireNotNull(topControlsBinding)) {
            undo.setOnClickListener {
                viewModel.undoLastAction()
            }
            redo.setOnClickListener {
                viewModel.redoLastAction()
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
        }
    }
}

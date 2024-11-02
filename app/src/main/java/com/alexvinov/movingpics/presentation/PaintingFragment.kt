package com.alexvinov.movingpics.presentation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.alexvinov.movingpics.R
import com.alexvinov.movingpics.databinding.FragmentFirstBinding
import com.alexvinov.movingpics.databinding.TopControlsBinding
import com.alexvinov.movingpics.domain.HistoryHolder

class PaintingFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private var topControlsBinding: TopControlsBinding? = null
    private val binding get() = _binding!!

    private var paintingView: PaintView? = null
    private var brushProvider: BrushProvider = BrushProvider()
    private var historyHolder: HistoryHolder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        topControlsBinding = TopControlsBinding.bind(binding.root)
        historyHolder = HistoryHolder()
        setUpControls()
        return binding.root
    }

    private fun setUpControls() {
        with(requireNotNull(topControlsBinding)) {
            undo.setOnClickListener {
                previousPicture()?.let { bitmap ->
                    paintingView?.setBackgroundBitmap(bitmap)
                }
            }
            redo.setOnClickListener {
                historyHolder?.redoLastAction()?.let { bitmap ->
                    paintingView?.setBackgroundBitmap(bitmap)
                }
            }
        }
    }

    private fun previousPicture(): Bitmap? {
        return historyHolder?.undoLastAction()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        paintingView = PaintView(requireContext())
        paintingView?.setPen(brushProvider.pen())
        paintingView?.setDrawingListener(object : PaintView.DrawingListener{
            override fun onDrawingFinished(bitmap: Bitmap) {
                historyHolder?.addLayer(bitmap)
            }
        })
        paintingView?.setBackgroundBitmap(getBitmap())

        binding.paintingContainer.addView(
            paintingView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    private fun getBitmap(): Bitmap {
        val bgDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_background, null)
        val bgBitmap = Bitmap.createBitmap(
            bgDrawable?.intrinsicWidth ?: 1,
            bgDrawable?.intrinsicHeight ?: 1,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bgBitmap)

        bgDrawable?.setBounds(0, 0, canvas.width, canvas.height)
        bgDrawable?.draw(canvas)
        return bgBitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

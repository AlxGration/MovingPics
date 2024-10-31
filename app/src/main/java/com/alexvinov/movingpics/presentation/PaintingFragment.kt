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

class PaintingFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var paintingView: PaintView? = null
    private var brushProvider: BrushProvider = BrushProvider()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        paintingView = PaintView(requireContext(), brushProvider)
        binding.paintingContainer.addView(
            paintingView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        paintingView?.setBackground(getBitmap())
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

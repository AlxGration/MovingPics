package com.alexvinov.movingpics.presentation.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.alexvinov.movingpics.R
import com.alexvinov.movingpics.databinding.ViewControlBrushSizeBinding
import com.alexvinov.movingpics.databinding.ViewControlColorsBinding
import com.alexvinov.movingpics.utils.toDp

class ControlBrushSizeView(
    private val context: Context,
    private val attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val binding = ViewControlBrushSizeBinding.inflate(LayoutInflater.from(context), this)
    private var brushWidthListener: OnBrushWidthChangedListener? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        setPadding(16.toDp(context))
        background = ContextCompat.getDrawable(context, R.drawable.controls_floating_background)

        initBrushWidthListeners()
    }

    fun setBrushWidthChangedListener(listener: OnBrushWidthChangedListener?) { brushWidthListener = listener }

    private fun initBrushWidthListeners() {
        with(binding) {
            seekBar.max = DEFAULT_MAX_BRUSH_WIDTH
            seekBar.progress = DEFAULT_BRUSH_WIDTH
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, value: Int, p2: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    brushWidthListener?.onBrushWidthChanged((seekBar?.progress ?: DEFAULT_BRUSH_WIDTH).toFloat())
                }
            })
        }
    }

    interface OnBrushWidthChangedListener {
        fun onBrushWidthChanged(width: Float)
    }

    companion object {
        const val DEFAULT_MAX_BRUSH_WIDTH = 140
        const val DEFAULT_BRUSH_WIDTH = 10
    }
}
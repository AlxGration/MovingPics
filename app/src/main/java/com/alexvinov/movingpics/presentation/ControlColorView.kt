package com.alexvinov.movingpics.presentation

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.alexvinov.movingpics.R
import com.alexvinov.movingpics.databinding.ViewControlColorsBinding
import com.alexvinov.movingpics.utils.toDp

class ControlColorView(
    private val context: Context,
    private val attrs: AttributeSet?,
) : LinearLayout(context, attrs) {

    private val binding = ViewControlColorsBinding.inflate(LayoutInflater.from(context), this)
    private var colorSelectedListener: OnColorSelectedListener? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        setPadding(16.toDp(context))
        background = ContextCompat.getDrawable(context, R.drawable.controls_floating_background)

        initColorsListeners()
    }
    fun setColorSelectedListener(listener: OnColorSelectedListener?) { colorSelectedListener = listener }

    private fun initColorsListeners() {
        with(binding) {
            colorWhite.setOnClickListener { colorSelectedListener?.onColorSelected(Color.WHITE) }
            colorBlack.setOnClickListener { colorSelectedListener?.onColorSelected(Color.BLACK) }
            colorRed.setOnClickListener {
                colorSelectedListener?.onColorSelected(ContextCompat.getColor(context, R.color.red))
            }
            colorBlue.setOnClickListener {
                colorSelectedListener?.onColorSelected(ContextCompat.getColor(context, R.color.blue))
            }
        }
    }

    interface OnColorSelectedListener {
        fun onColorSelected(color: Int)
    }
}
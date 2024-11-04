package com.alexvinov.movingpics.presentation.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

class PaintView(
    private val context: Context,
    private val attrs: AttributeSet? = null,
) : SurfaceView(context, attrs),
    SurfaceHolder.Callback,
    View.OnTouchListener,
    Runnable {
    private var holder: SurfaceHolder? = null
    private var drawThread: Thread? = null

    private var isSurfaceReady = AtomicBoolean(false)
    private var isDrawingActive = AtomicBoolean(false)
    private var isUserDrawingAllowed = AtomicBoolean(false)

    private val path = Path()
    private var pen = Paint()
    private var canvas: Canvas? = null
    private var picture: Bitmap? = null
    private var background: Bitmap? = null

    private var drawingListener: DrawingListener? = null

    init {
        this.holder = getHolder()
        this.holder?.addCallback(this)
        setOnTouchListener(this)
    }

    fun setBrush(pen: Paint) {
        this.pen = pen
    }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        background = bitmap
    }

    fun setPictureBitmap(bitmap: Bitmap) {
        picture = bitmap
        canvas = Canvas(bitmap)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.holder = holder
        drawThread?.let { thread ->
            isDrawingActive.set(false)
            try {
                thread.join()
            } catch (err: Throwable) {
                err.printStackTrace()
            }
        }

        isSurfaceReady.set(true)
        startDrawing()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int,
    ) {
        if (width == 0 || height == 0) {
            return
        }

        drawingListener?.initViewSize(width, height)
        picture
            ?.let { bitmap ->
                Bitmap.createScaledBitmap(bitmap, width, height, false)
            }?.also { bitmap ->
                setPictureBitmap(bitmap)
            }
        background
            ?.let { bitmap ->
                Bitmap.createScaledBitmap(bitmap, width, height, false)
            }?.also { bitmap ->
                setBackgroundBitmap(bitmap)
            }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopDrawing()
        this.holder?.surface?.release()
        this.holder = null
        isSurfaceReady.set(false)
    }

    override fun onTouch(
        view: View?,
        event: MotionEvent?,
    ): Boolean {
        if (!isUserDrawingAllowed.get()) return false
        event ?: return false
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDown(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
            }

            MotionEvent.ACTION_UP -> {
                touchUp()
            }

            else -> {}
        }
        return true
    }

    private fun touchDown(
        x: Float,
        y: Float,
    ) {
        path.moveTo(x, y)
    }

    private fun touchMove(
        x: Float,
        y: Float,
    ) {
        path.lineTo(x, y)
        canvas?.drawPath(path, pen)
    }

    private fun touchUp() {
        path.reset()
        picture?.let { bitmap -> drawingListener?.onDrawingFinished(bitmap) }
    }

    override fun run() {
        while (isDrawingActive.get()) {
            holder ?: return

            val startFrameTime = System.nanoTime()
            holder?.lockCanvas()?.let { canvas ->
                picture?.let { bitmap ->
                    try {
                        background?.let { background ->
                            canvas.drawBitmap(background, 0f, 0f, null)
                        }
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                    } catch (err: Throwable) {
                        err.printStackTrace()
                    }
                }
                holder?.unlockCanvasAndPost(canvas)
            }

            val frameTime = (System.nanoTime() - startFrameTime) / 1000000

            if (frameTime < MAX_FRAME_TIME) {
                try {
                    Thread.sleep(MAX_FRAME_TIME - frameTime)
                } catch (err: Throwable) {
                    err.printStackTrace()
                }
            }
        }
    }

    fun setIsDrawingAllowed(isDrawingAllowed: Boolean) {
        isUserDrawingAllowed.set(isDrawingAllowed)
    }

    private fun stopDrawing() {
        drawThread ?: return
        drawThread?.let { thread ->
            isDrawingActive.set(false)
            while (true) {
                try {
                    thread.join(WAITING_THREAD_MILLIS)
                    break
                } catch (err: Throwable) {
                    err.printStackTrace()
                }
            }
            drawThread = null
        }
    }

    private fun startDrawing() {
        if (isSurfaceReady.get() && drawThread == null) {
            drawThread = Thread(this)
            isDrawingActive.set(true)
            drawThread?.start()
        }
    }

    interface DrawingListener {
        fun onDrawingFinished(bitmap: Bitmap)
        fun initViewSize(width: Int, height: Int)
    }

    fun setDrawingListener(listener: DrawingListener?) {
        drawingListener = listener
    }

    companion object {
        private const val MAX_FRAME_TIME = 16
        private const val WAITING_THREAD_MILLIS = 3000L
    }
}

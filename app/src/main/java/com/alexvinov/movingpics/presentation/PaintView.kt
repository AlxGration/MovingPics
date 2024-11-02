package com.alexvinov.movingpics.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

class PaintView(
    context: Context,
) : SurfaceView(context),
    SurfaceHolder.Callback,
    View.OnTouchListener,
    Runnable {
    private var holder: SurfaceHolder? = null
    private var drawThread: Thread? = null

    private var surfaceReady = AtomicBoolean(false)
    private var drawingActive = AtomicBoolean(false)

    private val path = Path()
    private var pen = Paint()
    private var canvas: Canvas? = null
    private var curBitmap: Bitmap? = null

    private var drawingListener: DrawingListener? = null

    init {
        this.holder = getHolder()
        this.holder?.addCallback(this)
        setOnTouchListener(this)
    }

    fun setPen(pen: Paint) {
        this.pen = pen
    }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        curBitmap = bitmap
        canvas = Canvas(bitmap)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.holder = holder
        drawThread?.let { thread ->
            drawingActive.set(false)
            try {
                thread.join()
            } catch (err: Throwable) {
                err.printStackTrace()
            }
        }

        surfaceReady.set(true)
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

        curBitmap
            ?.let { bitmap ->
                Bitmap.createScaledBitmap(bitmap, width, height, false)
            }?.also { bitmap ->
                setBackgroundBitmap(bitmap)
                drawingListener?.onDrawingFinished(bitmap)
            }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopDrawing()
        this.holder?.surface?.release()
        this.holder = null
        surfaceReady.set(false)
    }

    override fun onTouch(
        view: View?,
        event: MotionEvent?,
    ): Boolean {
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
        curBitmap?.let { bitmap -> drawingListener?.onDrawingFinished(bitmap) }
    }

    override fun run() {
        while (drawingActive.get()) {
            holder ?: return

            val startFrameTime = System.nanoTime()
            holder?.lockCanvas()?.let { canvas ->
                curBitmap?.let { bitmap ->
                    try {
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

    private fun stopDrawing() {
        drawThread ?: return
        drawThread?.let { thread ->
            drawingActive.set(false)
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
        if (surfaceReady.get() && drawThread == null) {
            drawThread = Thread(this)
            drawingActive.set(true)
            drawThread?.start()
        }
    }

    interface DrawingListener {
        fun onDrawingFinished(bitmap: Bitmap)
    }

    fun setDrawingListener(listener: DrawingListener?) {
        drawingListener = listener
    }

    companion object {
        private const val MAX_FRAME_TIME = 16
        private const val WAITING_THREAD_MILLIS = 3000L
    }
}

package com.hydration.tracker.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin

class WaterWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#A4D7E1")
    }

    private val wavePath = Path()
    private var waveAnimator: ValueAnimator? = null
    private var waveShift = 0f
    private var progress = 0f

    private val waveLength = 400f
    private val waveHeight = 20f

    init {
        startWaveAnimation()
    }

    fun setProgress(newProgress: Float) {
        val animator = ValueAnimator.ofFloat(progress, newProgress)
        animator.duration = 600
        animator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun startWaveAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, waveLength).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                waveShift = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val waterLevel = height - (height * progress)

        wavePath.reset()
        wavePath.moveTo(0f, waterLevel)

        var x = 0f
        while (x < width) {
            val y = waterLevel + waveHeight * sin((x + waveShift) * 2 * Math.PI / waveLength).toFloat()
            wavePath.lineTo(x, y)
            x += 10f
        }

        wavePath.lineTo(width, waterLevel)
        wavePath.lineTo(width, height)
        wavePath.lineTo(0f, height)
        wavePath.close()

        canvas.drawPath(wavePath, wavePaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
    }
}
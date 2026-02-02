package com.hydration.tracker.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class AnimatedProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#22A4D7E1")
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var progress = 0f
    private var targetProgress = 0f
    private val cornerRadius = 50f

    init {
        val gradientColors = intArrayOf(
            Color.parseColor("#A4D7E1"),
            Color.parseColor("#A496E2")
        )

        progressPaint.shader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            gradientColors,
            null,
            Shader.TileMode.CLAMP
        )
    }

    fun setProgress(newProgress: Float) {
        targetProgress = newProgress.coerceIn(0f, 1f)

        ValueAnimator.ofFloat(progress, targetProgress).apply {
            duration = 600
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val gradientColors = intArrayOf(
            Color.parseColor("#A4D7E1"),
            Color.parseColor("#A496E2")
        )

        progressPaint.shader = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            gradientColors,
            null,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            cornerRadius, cornerRadius,
            backgroundPaint
        )

        // Draw progress
        val progressWidth = width * progress
        canvas.drawRoundRect(
            0f, 0f, progressWidth, height.toFloat(),
            cornerRadius, cornerRadius,
            progressPaint
        )
    }
}
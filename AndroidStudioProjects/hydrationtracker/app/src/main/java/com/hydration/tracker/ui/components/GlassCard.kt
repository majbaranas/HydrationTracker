package com.hydration.tracker.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.graphics.Outline
import android.widget.FrameLayout
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF

/**
 * Transparent GlassCard with a subtle rounded gray border (no blur, no extra deps).
 */
class GlassCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cornerRadius = 24f

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        color = Color.parseColor("#99BDBDBD")
    }

    private val borderRect = RectF()

    init {
        // allow drawing the border
        setWillNotDraw(false)
        // no elevation/shadow
        elevation = 0f

        // Keep rounded outline so clipping still works for child content
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                val w = view?.width ?: 0
                val h = view?.height ?: 0
                outline?.setRoundRect(0, 0, w, h, cornerRadius)
            }
        }
        clipToOutline = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val halfStroke = borderPaint.strokeWidth / 2f
        borderRect.set(halfStroke, halfStroke, w.toFloat() - halfStroke, h.toFloat() - halfStroke)
    }

    override fun onDraw(canvas: Canvas) {
        // Draw only the rounded border; interior remains transparent
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
        super.onDraw(canvas)
    }
}

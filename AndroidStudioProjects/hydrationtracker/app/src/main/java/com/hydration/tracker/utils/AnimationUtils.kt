package com.hydration.tracker.utils

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object AnimationUtils {

    /**
     * Pulse animation (continuous)
     */
    fun createPulseAnimation(view: View, scale: Float = 1.1f): ValueAnimator {
        return ValueAnimator.ofFloat(1f, scale, 1f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float
                view.scaleX = value
                view.scaleY = value
            }
        }
    }

    /**
     * Smooth progress animation
     */
    fun animateProgress(
        from: Int,
        to: Int,
        duration: Long = 600,
        onUpdate: (Int) -> Unit,
        onEnd: (() -> Unit)? = null
    ) {
        ValueAnimator.ofInt(from, to).apply {
            this.duration = duration
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                onUpdate(it.animatedValue as Int)
            }
            doOnEnd {
                onEnd?.invoke()
            }
            start()
        }
    }

    /**
     * Shake animation for errors
     */
    fun createShakeAnimation(view: View) {
        val animator = ValueAnimator.ofFloat(0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = 500
        animator.addUpdateListener {
            view.translationX = it.animatedValue as Float
        }
        animator.start()
    }

    /**
     * Wave animation (like a ripple effect)
     */
    fun createWaveAnimation(view: View, delay: Long = 0) {
        view.postDelayed({
            view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }, delay)
    }
}
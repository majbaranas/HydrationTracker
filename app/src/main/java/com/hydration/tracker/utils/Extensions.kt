package com.hydration.tracker.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import java.text.SimpleDateFormat
import java.util.*

// Date Extensions
fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

// View Extensions
fun View.fadeIn(duration: Long = 300) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(FastOutSlowInInterpolator())
        .start()
}

fun View.fadeOut(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .setInterpolator(FastOutSlowInInterpolator())
        .withEndAction {
            visibility = View.GONE
            onEnd?.invoke()
        }
        .start()
}

fun View.scaleIn(duration: Long = 400) {
    scaleX = 0f
    scaleY = 0f
    visibility = View.VISIBLE
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(duration)
        .setInterpolator(OvershootInterpolator(1.5f))
        .start()
}

fun View.slideInFromBottom(duration: Long = 400) {
    translationY = 300f
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .translationY(0f)
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(FastOutSlowInInterpolator())
        .start()
}

fun View.bounce() {
    animate()
        .scaleX(0.95f)
        .scaleY(0.95f)
        .setDuration(100)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start()
        }
        .start()
}

// Context Extensions
fun Context.dpToPx(dp: Float): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

fun Context.pxToDp(px: Float): Int {
    return (px / resources.displayMetrics.density).toInt()
}

// Number Extensions
fun Int.formatAsWater(): String {
    return if (this >= 1000) {
        "${this / 1000}.${(this % 1000) / 100}L"
    } else {
        "${this}ml"
    }
}
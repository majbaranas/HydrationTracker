package com.hydration.tracker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import jp.wasabeef.blurry.Blurry
import android.graphics.BlurMaskFilter
import android.graphics.RenderNode

object BlurUtils {

    fun applyBlurToView(context: Context, view: View, radius: Int = 25) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applyRenderEffectBlur(view, radius)
        } else {
            applyBlurryEffect(context, view, radius)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Suppress("NewApi")
    private fun applyRenderEffectBlur(view: View, radius: Int) {
        val blurEffect = RenderEffect.createBlurEffect(
            radius.toFloat(),
            radius.toFloat(),
            Shader.TileMode.CLAMP
        )
        view.setRenderEffect(blurEffect)
    }

    private fun applyBlurryEffect(context: Context, view: View, radius: Int) {
        try {
            Blurry.with(context)
                .radius(radius)
                .sampling(2)
                .async()
                .animate(300)
                .onto(view as? android.view.ViewGroup)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeBlur(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            removeRenderEffectBlur(view)
        } else {
            Blurry.delete(view as? android.view.ViewGroup)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Suppress("NewApi")
    private fun removeRenderEffectBlur(view: View) {
        view.setRenderEffect(null)
    }

    fun createBlurredBitmap(context: Context, bitmap: Bitmap, radius: Float = 25f): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return createBlurredBitmapWithRenderEffect(bitmap, radius)
        } else {
            return createBlurredBitmapLegacy(bitmap, radius)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createBlurredBitmapWithRenderEffect(bitmap: Bitmap, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val renderNode = RenderNode("blur").apply {
            setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
            setPosition(0, 0, bitmap.width, bitmap.height)
        }
        
        val canvas = renderNode.beginRecording()
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        renderNode.endRecording()
        
        val outputCanvas = Canvas(output)
        outputCanvas.drawRenderNode(renderNode)
        
        return output
    }

    private fun createBlurredBitmapLegacy(bitmap: Bitmap, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }
}
package com.hydration.tracker.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hydration.tracker.databinding.ActivitySplashBinding
import com.hydration.tracker.ui.MainActivity
import com.hydration.tracker.data.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    private val splashDuration = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()
        navigateAfterDelay()
    }

    private fun startAnimations() {
        // Glass circle animation - scale and fade in
        binding.glassCircle.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
        }

        val glassAlpha = ObjectAnimator.ofFloat(binding.glassCircle, View.ALPHA, 0f, 1f)
        val glassScaleX = ObjectAnimator.ofFloat(binding.glassCircle, View.SCALE_X, 0.5f, 1f)
        val glassScaleY = ObjectAnimator.ofFloat(binding.glassCircle, View.SCALE_Y, 0.5f, 1f)

        AnimatorSet().apply {
            playTogether(glassAlpha, glassScaleX, glassScaleY)
            duration = 800
            interpolator = OvershootInterpolator(1.2f)
            start()
        }

        // Water drop icon animation - bounce effect
        binding.splashIcon.apply {
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
            postDelayed({
                val iconAlpha = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
                val iconScaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, 1f)
                val iconScaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f, 1f)

                AnimatorSet().apply {
                    playTogether(iconAlpha, iconScaleX, iconScaleY)
                    duration = 600
                    interpolator = OvershootInterpolator(2f)
                    start()
                }

                // Add a subtle continuous bounce
                startContinuousBounce()
            }, 300)
        }

        // App name text animation - slide up and fade in
        binding.appNameText.apply {
            alpha = 0f
            translationY = 50f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }, 600)
        }

        // Loading progress animation
        binding.loadingProgress.apply {
            alpha = 0f
            postDelayed({
                animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start()
            }, 1000)
        }
    }

    private fun startContinuousBounce() {
        val bounce = ObjectAnimator.ofFloat(binding.splashIcon, View.TRANSLATION_Y, 0f, -20f, 0f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
        }
        bounce.start()
    }

    private fun navigateAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashDuration)
    }

    private fun navigateToNextScreen() {
        lifecycleScope.launch {
            val isLoggedIn = userPreferences.isLoggedIn.first()
            
            val intent = if (isLoggedIn) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java)
            }
            
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
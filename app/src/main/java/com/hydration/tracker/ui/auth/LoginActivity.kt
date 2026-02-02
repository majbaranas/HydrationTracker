package com.hydration.tracker.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import com.hydration.tracker.R
import com.hydration.tracker.databinding.ActivityLoginBinding
import com.hydration.tracker.ui.MainActivity
import com.hydration.tracker.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    // Secret guest login
    private var loginClickCount = 0
    private var lastClickTime = 0L
    private val clickTimeWindow = 3000L // 3 seconds window to complete 3 clicks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        animateEntrance()
    }

    private fun setupUI() {
        // Email field animations
        binding.emailEditText.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                binding.emailInputLayout.error = null
            }
        }

        // Password field animations
        binding.passwordEditText.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                binding.passwordInputLayout.error = null
            }
        }

        // Login button with secret guest mode
        binding.loginButton.setOnClickListener {
            it.bounce()

            // Secret guest login logic
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > clickTimeWindow) {
                // Reset counter if too much time has passed
                loginClickCount = 1
            } else {
                loginClickCount++
            }
            lastClickTime = currentTime

            // Check if user clicked 3 times
            if (loginClickCount >= 3) {
                loginClickCount = 0
                enterAsGuest()
                return@setOnClickListener
            }

            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInput(email, password)) {
                hideKeyboard()
                viewModel.login(email, password)
            }
        }

        // Sign up redirect
        binding.signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> showLoading()
                is AuthViewModel.AuthState.Success -> {
                    hideLoading()
                    navigateToHome()
                }
                is AuthViewModel.AuthState.Error -> {
                    hideLoading()
                    showError(state.message)
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            binding.emailInputLayout.error = getString(R.string.error_email_required)
            AnimationUtils.createShakeAnimation(binding.emailInputLayout)
            isValid = false
        }

        if (password.isBlank()) {
            binding.passwordInputLayout.error = getString(R.string.error_password_required)
            AnimationUtils.createShakeAnimation(binding.passwordInputLayout)
            isValid = false
        }

        return isValid
    }

    private fun animateEntrance() {
        // Logo animation
        binding.logoImage.alpha = 0f
        binding.logoImage.scaleX = 0f
        binding.logoImage.scaleY = 0f

        ObjectAnimator.ofFloat(binding.logoImage, View.ALPHA, 0f, 1f).apply {
            duration = 600
            start()
        }

        val scaleX = ObjectAnimator.ofFloat(binding.logoImage, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.logoImage, View.SCALE_Y, 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 600
            interpolator = OvershootInterpolator(1.5f)
            start()
        }

        // Staggered card animations
        binding.loginCard.slideInFromBottom(400)
        binding.loginButton.postDelayed({
            binding.loginButton.scaleIn(400)
        }, 200)

        binding.signUpContainer.postDelayed({
            binding.signUpContainer.fadeIn(400)
        }, 400)
    }

    private fun showLoading() {
        binding.loginButton.isEnabled = false
        binding.progressBar.fadeIn()
        binding.buttonText.fadeOut()
    }

    private fun hideLoading() {
        binding.loginButton.isEnabled = true
        binding.progressBar.fadeOut()
        binding.buttonText.fadeIn()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun hideKeyboard() {
        currentFocus?.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun enterAsGuest() {
        Snackbar.make(binding.root, "Entering as Guest...", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.accent))
            .show()

        // Navigate to home without authentication
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("GUEST_MODE", true)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
package com.hydration.tracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import com.hydration.tracker.R
import com.hydration.tracker.databinding.ActivitySignUpBinding
import com.hydration.tracker.ui.MainActivity
import com.hydration.tracker.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        animateEntrance()
    }

    private fun setupUI() {
        // Back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // Input validations
        binding.usernameEditText.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                binding.usernameInputLayout.error = null
            }
        }

        binding.emailEditText.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                binding.emailInputLayout.error = null
            }
        }

        binding.passwordEditText.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                binding.passwordInputLayout.error = null
                updatePasswordStrength(it.toString())
            }
        }

        binding.confirmPasswordEditText.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                binding.confirmPasswordInputLayout.error = null
            }
        }

        // Sign up button
        binding.signUpButton.setOnClickListener {
            it.bounce()
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInput(username, email, password, confirmPassword)) {
                hideKeyboard()
                viewModel.signUp(username, email, password)
            }
        }

        // Login redirect
        binding.loginText.setOnClickListener {
            onBackPressed()
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

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (username.isBlank()) {
            binding.usernameInputLayout.error = getString(R.string.error_username_required)
            AnimationUtils.createShakeAnimation(binding.usernameInputLayout)
            isValid = false
        }

        if (email.isBlank()) {
            binding.emailInputLayout.error = getString(R.string.error_email_required)
            AnimationUtils.createShakeAnimation(binding.emailInputLayout)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = getString(R.string.error_email_invalid)
            AnimationUtils.createShakeAnimation(binding.emailInputLayout)
            isValid = false
        }

        if (password.isBlank()) {
            binding.passwordInputLayout.error = getString(R.string.error_password_required)
            AnimationUtils.createShakeAnimation(binding.passwordInputLayout)
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = getString(R.string.error_password_short)
            AnimationUtils.createShakeAnimation(binding.passwordInputLayout)
            isValid = false
        }

        if (confirmPassword != password) {
            binding.confirmPasswordInputLayout.error = getString(R.string.error_password_mismatch)
            AnimationUtils.createShakeAnimation(binding.confirmPasswordInputLayout)
            isValid = false
        }

        return isValid
    }

    private fun updatePasswordStrength(password: String) {
        val strength = when {
            password.length < 6 -> 0.33f
            password.length < 10 -> 0.66f
            else -> 1f
        }

        binding.passwordStrengthBar.setProgress(strength)

        binding.passwordStrengthText.text = when {
            strength < 0.5f -> getString(R.string.password_weak)
            strength < 1f -> getString(R.string.password_medium)
            else -> getString(R.string.password_strong)
        }
    }

    private fun animateEntrance() {
        binding.signUpCard.slideInFromBottom(400)

        binding.signUpButton.postDelayed({
            binding.signUpButton.scaleIn(400)
        }, 200)

        binding.loginContainer.postDelayed({
            binding.loginContainer.fadeIn(400)
        }, 400)
    }

    private fun showLoading() {
        binding.signUpButton.isEnabled = false
        binding.progressBar.fadeIn()
        binding.buttonText.fadeOut()
    }

    private fun hideLoading() {
        binding.signUpButton.isEnabled = true
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
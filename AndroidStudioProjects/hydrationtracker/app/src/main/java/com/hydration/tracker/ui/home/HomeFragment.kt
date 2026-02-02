package com.hydration.tracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hydration.tracker.R
import com.hydration.tracker.databinding.DialogAddWaterBinding
import com.hydration.tracker.databinding.FragmentHomeBinding
import com.hydration.tracker.data.local.entities.WaterIntakeEntity
import com.hydration.tracker.utils.bounce
import com.hydration.tracker.utils.fadeIn
import com.hydration.tracker.utils.fadeOut
import com.hydration.tracker.utils.formatAsWater
import com.hydration.tracker.utils.scaleIn
import com.hydration.tracker.utils.slideInFromBottom
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var intakeAdapter: WaterIntakeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupUI()
        setupObservers()
        animateEntrance()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }
    }

    private fun setupUI() {
        // Recycler view for intake history
        intakeAdapter = WaterIntakeAdapter(
            onDeleteClick = { intake ->
                showDeleteConfirmation(intake)
            }
        )

        binding.intakeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = intakeAdapter
            setHasFixedSize(false)
        }

        // Reset button
        binding.resetButton.setOnClickListener {
            showResetConfirmation()
        }

        // Quick add buttons
        binding.add250mlButton.setOnClickListener {
            it.bounce()
            addWaterWithAnimation(250)
        }

        binding.add500mlButton.setOnClickListener {
            it.bounce()
            addWaterWithAnimation(500)
        }

        binding.add750mlButton.setOnClickListener {
            it.bounce()
            addWaterWithAnimation(750)
        }

        // FAB for custom amount
        binding.addWaterFab.setOnClickListener {
            showAddWaterDialog()
        }
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.welcomeText.text = getString(R.string.welcome_user, it.username)
                binding.goalText.text = getString(R.string.daily_goal, it.dailyGoal.formatAsWater())
            }
        }

        viewModel.todayTotal.observe(viewLifecycleOwner) { total ->
            viewModel.currentUser.value?.let { user ->
                val progress = total.toFloat() / user.dailyGoal
                updateProgress(total, user.dailyGoal, progress)
            }
        }

        viewModel.todayIntakes.observe(viewLifecycleOwner) { intakes ->
            intakeAdapter.submitList(intakes)

            if (intakes.isEmpty()) {
                binding.emptyStateText.fadeIn()
                binding.intakeRecyclerView.fadeOut()
                binding.resetButton.isEnabled = false
            } else {
                binding.emptyStateText.fadeOut()
                binding.intakeRecyclerView.fadeIn()
                binding.resetButton.isEnabled = true
            }
        }

        viewModel.addIntakeState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewModel.AddIntakeState.Success -> {
                    celebrateAddition()
                }
                is HomeViewModel.AddIntakeState.Error -> {
                    showError(state.message)
                }
            }
        }

        viewModel.waterLimitWarning.observe(viewLifecycleOwner) { warning ->
            when (warning) {
                is HomeViewModel.WaterLimitWarning.WarningLevel -> {
                    showWarningDialog()
                }
                is HomeViewModel.WaterLimitWarning.MaxLimitReached -> {
                    showMaxLimitDialog()
                }
                null -> { /* No warning */ }
            }
        }
    }

    private fun updateProgress(current: Int, goal: Int, progress: Float) {
        // ...existing code...
        binding.waterWaveView.setProgress(progress)

        // ...existing code...
        binding.progressBar.setProgress(progress)

        // Smooth number animation
        animateProgress(
            from = binding.currentAmountText.text.toString().filter { it.isDigit() }.toIntOrNull() ?: 0,
            to = current,
            onUpdate = { value ->
                binding.currentAmountText.text = value.formatAsWater()
            }
        )

        // Check if goal reached
        if (progress >= 1f && progress < 1.1f) {
            showGoalReachedCelebration()
        }
    }

    private fun animateProgress(from: Int, to: Int, onUpdate: (Int) -> Unit) {
        val animator = android.animation.ValueAnimator.ofInt(from, to)
        animator.duration = 500
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            onUpdate(value)
        }
        animator.start()
    }

    private fun addWaterWithAnimation(amount: Int) {
        viewModel.addWaterIntake(amount)
    }

    private fun showAddWaterDialog() {
        val dialogBinding = DialogAddWaterBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Set initial state for animation
        dialogBinding.root.translationY = 100f
        dialogBinding.root.alpha = 0f

        // Quick add button handlers
        val buttons250ml = ArrayList<View>()
        dialogBinding.root.findViewsWithText(buttons250ml, "250ml", View.FIND_VIEWS_WITH_TEXT)
        buttons250ml.forEach { it.setOnClickListener { _ -> dialogBinding.amountEditText.setText("250") } }

        val buttons500ml = ArrayList<View>()
        dialogBinding.root.findViewsWithText(buttons500ml, "500ml", View.FIND_VIEWS_WITH_TEXT)
        buttons500ml.forEach { it.setOnClickListener { _ -> dialogBinding.amountEditText.setText("500") } }

        val buttons750ml = ArrayList<View>()
        dialogBinding.root.findViewsWithText(buttons750ml, "750ml", View.FIND_VIEWS_WITH_TEXT)
        buttons750ml.forEach { it.setOnClickListener { _ -> dialogBinding.amountEditText.setText("750") } }

        dialogBinding.confirmButton.setOnClickListener {
            val amount = dialogBinding.amountEditText.text.toString().toIntOrNull()
            if (amount != null && amount > 0) {
                viewModel.addWaterIntake(amount)
                dialog.dismiss()
            } else {
                dialogBinding.amountInputLayout.error = getString(R.string.error_invalid_amount)
                // Shake animation
                dialogBinding.amountInputLayout.animate()
                    .translationX(10f)
                    .setDuration(50)
                    .withEndAction {
                        dialogBinding.amountInputLayout.animate()
                            .translationX(-10f)
                            .setDuration(50)
                            .withEndAction {
                                dialogBinding.amountInputLayout.animate()
                                    .translationX(0f)
                                    .setDuration(50)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
        }

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        // Slide in animation
        dialogBinding.root.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(250)
            .start()
    }

    private fun showDeleteConfirmation(intake: com.hydration.tracker.data.local.entities.WaterIntakeEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_intake_title)
            .setMessage(R.string.delete_intake_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteIntake(intake)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_history_title)
            .setMessage(R.string.reset_history_message)
            .setPositiveButton(R.string.reset) { _, _ ->
                viewModel.deleteAllTodayIntakes()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showWarningDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.warning_title)
            .setMessage(R.string.warning_too_much_water)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                viewModel.clearWarning()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showMaxLimitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.limit_reached_title)
            .setMessage(R.string.limit_reached_message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                viewModel.clearWarning()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun celebrateAddition() {
        binding.waterWaveView.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(300)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                binding.waterWaveView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()

        binding.successIcon.apply {
            visibility = View.VISIBLE
            scaleX = 0.8f
            scaleY = 0.8f
            animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            postDelayed({
                animate().alpha(0f).setDuration(300).start()
            }, 1500)
        }
    }

    private fun showGoalReachedCelebration() {
        // Confetti animation
        binding.celebrationAnimation.apply {
            visibility = View.VISIBLE
            playAnimation()
            postDelayed({
                fadeOut()
            }, 3000)
        }

        // Show congratulations dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.goal_reached_title)
            .setMessage(R.string.goal_reached_message)
            .setPositiveButton(R.string.awesome) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun animateEntrance() {
        // Staggered animations
        binding.welcomeCard.slideInFromBottom(300)

        binding.waterProgressCard.postDelayed({
            binding.waterProgressCard.slideInFromBottom(350)
        }, 100)

        binding.quickActionsCard.postDelayed({
            binding.quickActionsCard.slideInFromBottom(400)
        }, 200)

        binding.addWaterFab.postDelayed({
            binding.addWaterFab.scaleIn(400)
        }, 300)
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar
            .make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
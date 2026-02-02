package com.hydration.tracker.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hydration.tracker.R
import com.hydration.tracker.databinding.FragmentProfileBinding
import com.hydration.tracker.databinding.DialogSetGoalBinding
import com.hydration.tracker.ui.auth.LoginActivity
import com.hydration.tracker.utils.*
import com.hydration.tracker.worker.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        animateEntrance()
    }

    private fun setupUI() {
        binding.setGoalButton.setOnClickListener {
            it.bounce()
            showSetGoalDialog()
        }

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
            if (isChecked) {
                scheduleReminders()
            } else {
                cancelReminders()
            }
        }
        binding.languageContainer.setOnClickListener {
            it.bounce()
            showLanguageDialog()
        }

        binding.intervalContainer.setOnClickListener {
            it.bounce()
            showReminderIntervalDialog()
        }

        binding.logoutButton.setOnClickListener {
            it.bounce()
            showLogoutDialog()
        }
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.usernameText.text = it.username
                binding.emailText.text = it.email
                binding.goalValueText.text = it.dailyGoal.formatAsWater()
                binding.memberSinceText.text = getString(
                    R.string.member_since,
                    it.createdAt.toDateString()
                )
            }
        }

        viewModel.notificationsEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.notificationSwitch.isChecked = enabled
        }

        viewModel.reminderInterval.observe(viewLifecycleOwner) { interval ->
            binding.intervalValueText.text = getString(R.string.minutes_format, interval)
        }

        viewModel.currentLanguage.observe(viewLifecycleOwner) { languageCode ->
            binding.languageValueText.text = LocaleHelper.getLanguageName(languageCode)
        }

    }

    // REMOVED THE DUPLICATE EMPTY FUNCTION THAT WAS HERE

    private fun showReminderIntervalDialog() {
        val intervals = arrayOf("15 min", "30 min", "60 min", "90 min", "Custom")
        val intervalValues = arrayOf(15, 30, 60, 90, 0)
        val currentInterval = viewModel.reminderInterval.value ?: 60
        val currentIndex = intervalValues.indexOf(currentInterval).let { if (it == -1) 4 else it }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reminder_interval)
            .setSingleChoiceItems(intervals, currentIndex) { dialog, which ->
                if (which == 4) {
                    dialog.dismiss()
                    showCustomIntervalDialog()
                } else {
                    val selectedInterval = intervalValues[which]
                    viewModel.setReminderInterval(selectedInterval)
                    if (viewModel.notificationsEnabled.value == true) {
                        scheduleReminders()
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCustomIntervalDialog() {
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter minutes (10-1440)"
        input.setText((viewModel.reminderInterval.value ?: 60).toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Custom Interval")
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val interval = input.text.toString().toIntOrNull()
                if (interval != null && interval in 10..1440) {
                    viewModel.setReminderInterval(interval)
                    if (viewModel.notificationsEnabled.value == true) {
                        scheduleReminders()
                    }
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Invalid Input")
                        .setMessage("Enter a value between 10-1440 minutes")
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showLanguageDialog() {
        // ...existing code...
        val languages = arrayOf("English", "العربية", "Français")
        val languageCodes = arrayOf("en", "ar", "fr")

        val currentLanguage = viewModel.currentLanguage.value ?: "en"
        val currentIndex = languageCodes.indexOf(currentLanguage)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguageCode = languageCodes[which]
                viewModel.setLanguage(selectedLanguageCode)

                // Save to SharedPreferences for MainActivity.attachBaseContext()
                val prefs = requireContext().getSharedPreferences("user_preferences", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("language", selectedLanguageCode).apply()

                // Apply locale and recreate activity
                LocaleHelper.setLocale(requireContext(), selectedLanguageCode)
                requireActivity().recreate()

                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showSetGoalDialog() {
        val dialogBinding = DialogSetGoalBinding.inflate(layoutInflater)

        val currentGoal = viewModel.currentUser.value?.dailyGoal ?: 2000
        dialogBinding.goalEditText.setText(currentGoal.toString())

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.confirmButton.setOnClickListener {
            val goal = dialogBinding.goalEditText.text.toString().toIntOrNull()
            if (goal != null && goal > 0) {
                viewModel.updateDailyGoal(goal)
                dialog.dismiss()
            } else {
                dialogBinding.goalInputLayout.error = getString(R.string.error_invalid_goal)
                AnimationUtils.createShakeAnimation(dialogBinding.goalInputLayout)
            }
        }

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialogBinding.root.scaleIn()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.logout) { _, _ ->
                viewModel.logout()
                navigateToLogin()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun scheduleReminders() {
        val interval = viewModel.reminderInterval.value ?: 60

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            interval.toLong(),
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun cancelReminders() {
        WorkManager.getInstance(requireContext())
            .cancelUniqueWork(ReminderWorker.WORK_NAME)
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun animateEntrance() {
        binding.profileCard.slideInFromBottom(300)

        binding.settingsCard.postDelayed({
            binding.settingsCard.slideInFromBottom(350)
        }, 100)

        binding.logoutButton.postDelayed({
            binding.logoutButton.scaleIn(400)
        }, 200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
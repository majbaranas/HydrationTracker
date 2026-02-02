package com.hydration.tracker.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.hydration.tracker.data.local.dao.UserDao
import com.hydration.tracker.data.local.entities.UserEntity
import com.hydration.tracker.data.preferences.UserPreferences
import com.hydration.tracker.data.repository.AuthRepository
import com.hydration.tracker.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val goalRepository: GoalRepository,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val userId = userPreferences.userId.asLiveData()

    val currentUser: LiveData<UserEntity?> = userId.switchMap { id ->
        if (id != null) {
            userDao.getUserById(id).asLiveData()
        } else {
            MutableLiveData(null)
        }
    }

    val currentLanguage = userPreferences.language.asLiveData()


    val notificationsEnabled = userPreferences.notificationsEnabled.asLiveData()
    val reminderInterval = userPreferences.reminderInterval.asLiveData()

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            val id = userPreferences.userId.first()
            if (id != null) {
                userDao.updateDailyGoal(id, goal)
                goalRepository.createGoal(id, goal)
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }

    fun setReminderInterval(minutes: Int) {
        viewModelScope.launch {
            userPreferences.setReminderInterval(minutes)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferences.setLanguage(languageCode)
        }
    }


    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
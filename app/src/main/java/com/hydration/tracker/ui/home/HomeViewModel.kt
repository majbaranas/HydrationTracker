package com.hydration.tracker.ui.home

import androidx.lifecycle.*
import com.hydration.tracker.data.local.entities.UserEntity
import com.hydration.tracker.data.local.entities.WaterIntakeEntity
import com.hydration.tracker.data.preferences.UserPreferences
import com.hydration.tracker.data.repository.WaterIntakeRepository
import com.hydration.tracker.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val waterIntakeRepository: WaterIntakeRepository,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        const val WARNING_LIMIT_ML = 5000 // 5L warning
        const val MAX_LIMIT_ML = 7000 // 7L maximum
    }

    private val userId = userPreferences.userId.asLiveData()

    val currentUser: LiveData<UserEntity?> = userId.switchMap { id ->
        if (id != null) {
            userDao.getUserById(id).asLiveData()
        } else {
            MutableLiveData(null)
        }
    }

    val todayIntakes: LiveData<List<WaterIntakeEntity>> = userId.switchMap { id ->
        if (id != null) {
            waterIntakeRepository.getTodayIntakes(id).asLiveData()
        } else {
            MutableLiveData(emptyList())
        }
    }

    val todayTotal: LiveData<Int> = userId.switchMap { id ->
        if (id != null) {
            waterIntakeRepository.getTodayTotal(id).asLiveData().map { it ?: 0 }
        } else {
            liveData { emit(0) }
        }
    }

    private val _addIntakeState = MutableLiveData<AddIntakeState>()
    val addIntakeState: LiveData<AddIntakeState> = _addIntakeState

    private val _waterLimitWarning = MutableLiveData<WaterLimitWarning>()
    val waterLimitWarning: LiveData<WaterLimitWarning> = _waterLimitWarning

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            try {
                val id = userPreferences.userId.first()
                if (id != null) {
                    val currentTotal = todayTotal.value ?: 0
                    val newTotal = currentTotal + amount

                    // Check if adding would exceed maximum limit
                    if (newTotal > MAX_LIMIT_ML) {
                        _waterLimitWarning.value = WaterLimitWarning.MaxLimitReached
                        return@launch
                    }

                    // Add the water intake
                    waterIntakeRepository.addWaterIntake(id, amount)
                    _addIntakeState.value = AddIntakeState.Success

                    // Check for warning
                    if (currentTotal < WARNING_LIMIT_ML && newTotal >= WARNING_LIMIT_ML) {
                        _waterLimitWarning.value = WaterLimitWarning.WarningLevel
                    }
                }
            } catch (e: Exception) {
                _addIntakeState.value = AddIntakeState.Error(e.message ?: "Failed to add water")
            }
        }
    }

    fun deleteIntake(intake: WaterIntakeEntity) {
        viewModelScope.launch {
            waterIntakeRepository.deleteIntake(intake)
        }
    }

    fun deleteAllTodayIntakes() {
        viewModelScope.launch {
            try {
                val id = userPreferences.userId.first()
                if (id != null) {
                    waterIntakeRepository.deleteAllTodayIntakes(id)
                }
            } catch (e: Exception) {
                _addIntakeState.value = AddIntakeState.Error("Failed to reset intake history")
            }
        }
    }

    fun clearWarning() {
        _waterLimitWarning.value = null
    }

    sealed class AddIntakeState {
        object Success : AddIntakeState()
        data class Error(val message: String) : AddIntakeState()
    }

    sealed class WaterLimitWarning {
        object WarningLevel : WaterLimitWarning()
        object MaxLimitReached : WaterLimitWarning()
    }
}

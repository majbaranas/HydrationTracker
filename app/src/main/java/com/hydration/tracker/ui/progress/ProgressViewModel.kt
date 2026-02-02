package com.hydration.tracker.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.hydration.tracker.data.preferences.UserPreferences
import com.hydration.tracker.data.repository.WaterIntakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val waterIntakeRepository: WaterIntakeRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val userId = userPreferences.userId.asLiveData()

    val weeklyData: LiveData<Map<String, Int>> = userId.switchMap { id ->
        if (id != null) {
            waterIntakeRepository.getWeeklyData(id).asLiveData()
        } else {
            MutableLiveData(emptyMap())
        }
    }
}

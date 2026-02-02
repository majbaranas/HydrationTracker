package com.hydration.tracker.ui.tips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TipsViewModel @Inject constructor(
    // Add any repositories or use cases you need here
) : ViewModel() {

    private val _tips = MutableLiveData<List<HydrationTip>>()
    val tips: LiveData<List<HydrationTip>> = _tips

    private val _shareEvent = MutableLiveData<Event<HydrationTip>>()
    val shareEvent: LiveData<Event<HydrationTip>> = _shareEvent

    init {
        loadTips()
    }

    private fun loadTips() {
        // In a real app, you might load tips from a repository
        // For now, we'll just signal that tips should be loaded by the fragment
    }

    fun onShareTip(tip: HydrationTip) {
        _shareEvent.value = Event(tip)
    }

    fun onTipClicked(tip: HydrationTip) {
        // Handle tip click if needed
        // Could expand the tip, show more details, etc.
    }

    // Event wrapper to handle single-time events
    class Event<out T>(private val content: T) {
        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }
}
package com.hydration.tracker.ui.tips

import androidx.annotation.DrawableRes

data class HydrationTip(
    val id: Int,
    val title: String,
    val description: String,
    @DrawableRes val icon: Int
)
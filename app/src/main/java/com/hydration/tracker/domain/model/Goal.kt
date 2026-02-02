package com.hydration.tracker.domain.model

data class Goal(
    val id: Long,
    val targetAmount: Int,
    val startDate: String,
    val endDate: String?,
    val achieved: Boolean
)
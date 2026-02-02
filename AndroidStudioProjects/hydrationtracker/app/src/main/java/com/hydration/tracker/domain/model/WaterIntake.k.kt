package com.hydration.tracker.domain.model

data class WaterIntake(
    val id: Long,
    val amount: Int,
    val timestamp: Long,
    val date: String
)
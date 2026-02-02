package com.hydration.tracker.domain.model

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val dailyGoal: Int,
    val createdAt: Long
)
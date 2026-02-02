package com.hydration.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val targetAmount: Int, // ml
    val startDate: String,
    val endDate: String?,
    val achieved: Boolean = false
)
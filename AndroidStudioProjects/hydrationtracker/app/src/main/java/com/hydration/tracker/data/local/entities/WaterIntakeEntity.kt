package com.hydration.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intake")
data class WaterIntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val amount: Int, // ml
    val timestamp: Long = System.currentTimeMillis(),
    val date: String // yyyy-MM-dd format
)
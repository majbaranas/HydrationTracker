package com.hydration.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hydration.tracker.data.local.dao.GoalDao
import com.hydration.tracker.data.local.dao.UserDao
import com.hydration.tracker.data.local.dao.WaterIntakeDao
import com.hydration.tracker.data.local.entities.GoalEntity
import com.hydration.tracker.data.local.entities.UserEntity
import com.hydration.tracker.data.local.entities.WaterIntakeEntity

@Database(
    entities = [UserEntity::class, WaterIntakeEntity::class, GoalEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HydrationDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun goalDao(): GoalDao
}
package com.hydration.tracker.data.local.dao

import androidx.room.*
import com.hydration.tracker.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET dailyGoal = :goal WHERE id = :userId")
    suspend fun updateDailyGoal(userId: Long, goal: Int)
}
package com.hydration.tracker.data.local.dao

import androidx.room.*
import com.hydration.tracker.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY startDate DESC")
    fun getUserGoals(userId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND achieved = 0 ORDER BY startDate DESC LIMIT 1")
    fun getCurrentGoal(userId: Long): Flow<GoalEntity?>

    @Update
    suspend fun update(goal: GoalEntity)
}
package com.hydration.tracker.data.local.dao

import androidx.room.*
import com.hydration.tracker.data.local.entities.WaterIntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(intake: WaterIntakeEntity): Long

    @Delete
    suspend fun delete(intake: WaterIntakeEntity)

    @Query("DELETE FROM water_intake WHERE userId = :userId AND date = :date")
    suspend fun deleteAllTodayIntakes(userId: Long, date: String)

    @Query("SELECT * FROM water_intake WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getTodayIntakes(userId: Long, date: String): Flow<List<WaterIntakeEntity>>

    @Query("SELECT SUM(amount) FROM water_intake WHERE userId = :userId AND date = :date")
    fun getTodayTotal(userId: Long, date: String): Flow<Int?>

    @Query("""
        SELECT date, SUM(amount) as total 
        FROM water_intake 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate 
        GROUP BY date
    """)
    fun getWeeklyData(userId: Long, startDate: String, endDate: String): Flow<List<WeeklyIntake>>
}

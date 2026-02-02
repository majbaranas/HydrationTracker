package com.hydration.tracker.data.repository

import com.hydration.tracker.data.local.dao.WaterIntakeDao
import com.hydration.tracker.data.local.entities.WaterIntakeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterIntakeRepository @Inject constructor(
    private val waterIntakeDao: WaterIntakeDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun addWaterIntake(userId: Long, amount: Int): Long {
        val intake = WaterIntakeEntity(
            userId = userId,
            amount = amount,
            date = getTodayDate()
        )
        return waterIntakeDao.insert(intake)
    }

    fun getTodayIntakes(userId: Long): Flow<List<WaterIntakeEntity>> {
        return waterIntakeDao.getTodayIntakes(userId, getTodayDate())
    }

    fun getTodayTotal(userId: Long): Flow<Int?> {
        return waterIntakeDao.getTodayTotal(userId, getTodayDate())
    }

    fun getWeeklyData(userId: Long): Flow<Map<String, Int>> {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = dateFormat.format(calendar.time)

        return waterIntakeDao.getWeeklyData(userId, startDate, endDate).map { weeklyIntakes ->
            weeklyIntakes.associate { it.date to it.total }
        }
    }

    suspend fun deleteIntake(intake: WaterIntakeEntity) {
        waterIntakeDao.delete(intake)
    }

    suspend fun deleteAllTodayIntakes(userId: Long) {
        waterIntakeDao.deleteAllTodayIntakes(userId, getTodayDate())
    }

    private fun getTodayDate(): String {
        return dateFormat.format(Date())
    }
}
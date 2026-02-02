package com.hydration.tracker.data.repository

import com.hydration.tracker.data.local.dao.GoalDao
import com.hydration.tracker.data.local.dao.UserDao
import com.hydration.tracker.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val userDao: UserDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun createGoal(userId: Long, targetAmount: Int): Long {
        val goal = GoalEntity(
            userId = userId,
            targetAmount = targetAmount,
            startDate = getTodayDate(),
            endDate = null
        )
        val goalId = goalDao.insert(goal)

        // Update user's daily goal
        userDao.updateDailyGoal(userId, targetAmount)

        return goalId
    }

    fun getUserGoals(userId: Long): Flow<List<GoalEntity>> {
        return goalDao.getUserGoals(userId)
    }

    fun getCurrentGoal(userId: Long): Flow<GoalEntity?> {
        return goalDao.getCurrentGoal(userId)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.update(goal)
    }

    suspend fun markGoalAchieved(goalId: Long, userId: Long) {
        val goals = goalDao.getUserGoals(userId)
        // Update in coroutine scope
    }

    private fun getTodayDate(): String {
        return dateFormat.format(Date())
    }
}
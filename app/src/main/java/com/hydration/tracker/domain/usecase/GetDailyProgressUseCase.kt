package com.hydration.tracker.domain.usecase

import com.hydration.tracker.data.local.dao.UserDao
import com.hydration.tracker.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class DailyProgress(
    val currentAmount: Int,
    val goalAmount: Int,
    val percentage: Float,
    val remainingAmount: Int,
    val isGoalReached: Boolean
)

class GetDailyProgressUseCase @Inject constructor(
    private val waterIntakeRepository: WaterIntakeRepository,
    private val userDao: UserDao
) {
    operator fun invoke(userId: Long): Flow<DailyProgress> {
        return combine(
            waterIntakeRepository.getTodayTotal(userId),
            userDao.getUserById(userId)
        ) { todayTotal, user ->
            val currentAmount = todayTotal ?: 0
            val goalAmount = user?.dailyGoal ?: 2000
            val percentage = if (goalAmount > 0) {
                (currentAmount.toFloat() / goalAmount.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            val remainingAmount = (goalAmount - currentAmount).coerceAtLeast(0)
            val isGoalReached = currentAmount >= goalAmount

            DailyProgress(
                currentAmount = currentAmount,
                goalAmount = goalAmount,
                percentage = percentage,
                remainingAmount = remainingAmount,
                isGoalReached = isGoalReached
            )
        }
    }
}
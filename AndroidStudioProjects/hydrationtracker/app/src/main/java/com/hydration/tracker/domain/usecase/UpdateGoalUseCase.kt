package com.hydration.tracker.domain.usecase

import com.hydration.tracker.data.repository.GoalRepository
import javax.inject.Inject

class UpdateGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(userId: Long, newGoal: Int): Result<Long> {
        return try {
            if (newGoal <= 0) {
                return Result.failure(IllegalArgumentException("Goal must be greater than 0"))
            }

            if (newGoal < 500) {
                return Result.failure(IllegalArgumentException("Goal seems too low. Minimum is 500ml"))
            }

            if (newGoal > 10000) {
                return Result.failure(IllegalArgumentException("Goal seems too high. Maximum is 10L"))
            }

            val goalId = goalRepository.createGoal(userId, newGoal)
            Result.success(goalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
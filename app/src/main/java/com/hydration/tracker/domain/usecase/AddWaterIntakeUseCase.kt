package com.hydration.tracker.domain.usecase

import com.hydration.tracker.data.repository.WaterIntakeRepository
import javax.inject.Inject

class AddWaterIntakeUseCase @Inject constructor(
    private val waterIntakeRepository: WaterIntakeRepository
) {
    suspend operator fun invoke(userId: Long, amount: Int): Result<Long> {
        return try {
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
            }

            if (amount > 5000) {
                return Result.failure(IllegalArgumentException("Amount seems too large"))
            }

            val id = waterIntakeRepository.addWaterIntake(userId, amount)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
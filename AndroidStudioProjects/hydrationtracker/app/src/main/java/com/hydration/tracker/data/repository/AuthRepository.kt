package com.hydration.tracker.data.repository

import com.hydration.tracker.data.local.dao.UserDao
import com.hydration.tracker.data.local.entities.UserEntity
import com.hydration.tracker.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {

    suspend fun signUp(username: String, email: String, password: String): Result<Long> {
        return try {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("Email already registered"))
            }

            val passwordHash = hashPassword(password)
            val user = UserEntity(
                username = username,
                email = email,
                passwordHash = passwordHash
            )

            val userId = userDao.insert(user)
            userPreferences.saveUserId(userId)
            userPreferences.setLoggedIn(true)

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Long> {
        return try {
            val user = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("User not found"))

            val passwordHash = hashPassword(password)
            if (user.passwordHash != passwordHash) {
                return Result.failure(Exception("Invalid password"))
            }

            userPreferences.saveUserId(user.id)
            userPreferences.setLoggedIn(true)

            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPreferences.clearAll()
    }

    fun getCurrentUserId(): Flow<Long?> = userPreferences.userId

    fun isLoggedIn(): Flow<Boolean> = userPreferences.isLoggedIn

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
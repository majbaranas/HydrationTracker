package com.hydration.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_INTERVAL = intPreferencesKey("reminder_interval")
        val LANGUAGE = stringPreferencesKey("language")

    }

    val userId: Flow<Long?> = context.dataStore.data.map { it[Keys.USER_ID] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val reminderInterval: Flow<Int> = context.dataStore.data.map { it[Keys.REMINDER_INTERVAL] ?: 60 }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }

    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { it[Keys.USER_ID] = userId }
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { it[Keys.IS_LOGGED_IN] = isLoggedIn }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setReminderInterval(minutes: Int) {
        context.dataStore.edit { it[Keys.REMINDER_INTERVAL] = minutes }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = languageCode }
        // Also save to SharedPreferences for MainActivity.attachBaseContext()
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            .edit()
            .putString("language", languageCode)
            .apply()
    }


    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
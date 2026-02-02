package com.hydration.tracker.di

import android.content.Context
import androidx.room.Room
import com.hydration.tracker.data.local.HydrationDatabase
import com.hydration.tracker.data.local.dao.GoalDao
import com.hydration.tracker.data.local.dao.UserDao
import com.hydration.tracker.data.local.dao.WaterIntakeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHydrationDatabase(
        @ApplicationContext context: Context
    ): HydrationDatabase {
        return Room.databaseBuilder(
            context,
            HydrationDatabase::class.java,
            "hydration_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: HydrationDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideWaterIntakeDao(database: HydrationDatabase): WaterIntakeDao {
        return database.waterIntakeDao()
    }

    @Provides
    fun provideGoalDao(database: HydrationDatabase): GoalDao {
        return database.goalDao()
    }
}
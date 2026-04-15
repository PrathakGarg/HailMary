package com.arise.habitquest.di

import android.content.Context
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.local.database.dao.*
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideMissionDao(db: AppDatabase): MissionDao = db.missionDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideDailyLogDao(db: AppDatabase): DailyLogDao = db.dailyLogDao()
    @Provides fun provideShadowDao(db: AppDatabase): ShadowDao = db.shadowDao()
}

package com.arise.habitquest.di

import com.arise.habitquest.data.repository.AchievementRepositoryImpl
import com.arise.habitquest.data.repository.MissionRepositoryImpl
import com.arise.habitquest.data.repository.UserRepositoryImpl
import com.arise.habitquest.domain.repository.AchievementRepository
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindMissionRepository(impl: MissionRepositoryImpl): MissionRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository
}

package com.arise.habitquest.domain.repository

import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.AchievementTrigger
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun observeAllAchievements(): Flow<List<Achievement>>
    suspend fun getUnlockedAchievements(): List<Achievement>
    suspend fun getAchievementsByTrigger(trigger: AchievementTrigger): List<Achievement>
    suspend fun insertAchievements(achievements: List<Achievement>)
    suspend fun unlockAchievement(id: String)
    suspend fun relockAchievement(id: String)
    suspend fun updateProgress(id: String, progress: Int)
    suspend fun countUnlocked(): Int
}

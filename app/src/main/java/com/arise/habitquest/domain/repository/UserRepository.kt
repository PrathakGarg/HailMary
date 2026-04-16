package com.arise.habitquest.domain.repository

import com.arise.habitquest.domain.model.HunterStats
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.Rank
import com.arise.habitquest.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun getUserProfile(): UserProfile?
    suspend fun upsertProfile(profile: UserProfile)
    suspend fun updateXpAndLevel(xp: Long, level: Int, rank: Rank, xpToNext: Long)
    suspend fun updateHp(hp: Int)
    suspend fun updateStreak(current: Int, best: Int)
    suspend fun updateStats(stats: HunterStats)
    suspend fun incrementMissionStats(xpGained: Long)
    suspend fun decrementMissionStats(completedDelta: Int, xpDelta: Long)
    suspend fun updateShields(shields: Int)
    suspend fun updateAdaptiveDifficulty(difficulty: Float)
    suspend fun incrementDayCount()
    suspend fun updateMissState(consecutiveMissDays: Int, pendingWarning: Boolean)
    suspend fun updateProgressionState(state: String, transitionRecommendation: MissionCategory?)
    suspend fun updateRestDay(restDay: Int)
    suspend fun updateNotificationHour(hour: Int)
    /** Returns map of templateId → total completions from shadow records */
    suspend fun getShadowCompletions(templateIds: List<String>): Map<String, Int>
}

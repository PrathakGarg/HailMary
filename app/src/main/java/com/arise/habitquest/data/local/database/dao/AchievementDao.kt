package com.arise.habitquest.data.local.database.dao

import androidx.room.*
import com.arise.habitquest.data.local.database.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY unlocked_at DESC, rarity DESC")
    fun observeAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE unlocked_at IS NOT NULL ORDER BY unlocked_at DESC")
    suspend fun getUnlockedAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE trigger_type = :type")
    suspend fun getAchievementsByTrigger(type: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET unlocked_at = :unlockedAt WHERE id = :id")
    suspend fun unlockAchievement(id: String, unlockedAt: Long)

    @Query("UPDATE achievements SET progress_current = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked_at IS NOT NULL")
    suspend fun countUnlocked(): Int
}

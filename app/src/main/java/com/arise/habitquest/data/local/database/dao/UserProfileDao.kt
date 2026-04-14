package com.arise.habitquest.data.local.database.dao

import androidx.room.*
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET xp = :xp, level = :level, rank = :rank, xp_to_next_level = :xpToNext WHERE id = 1")
    suspend fun updateXpAndLevel(xp: Long, level: Int, rank: String, xpToNext: Long)

    @Query("UPDATE user_profile SET hp = :hp WHERE id = 1")
    suspend fun updateHp(hp: Int)

    @Query("UPDATE user_profile SET streak_current = :current, streak_best = :best WHERE id = 1")
    suspend fun updateStreak(current: Int, best: Int)

    @Query("UPDATE user_profile SET stat_str = :str, stat_agi = :agi, stat_int = :intel, stat_vit = :vit, stat_end = :end, stat_sense = :sense WHERE id = 1")
    suspend fun updateStats(str: Int, agi: Int, intel: Int, vit: Int, end: Int, sense: Int)

    @Query("UPDATE user_profile SET total_missions_completed = total_missions_completed + 1, total_xp_earned = total_xp_earned + :xpGained WHERE id = 1")
    suspend fun incrementMissionStats(xpGained: Long)

    @Query("UPDATE user_profile SET total_missions_completed = MAX(0, total_missions_completed - :completedDelta), total_xp_earned = MAX(0, total_xp_earned - :xpDelta) WHERE id = 1")
    suspend fun decrementMissionStats(completedDelta: Int, xpDelta: Long)

    @Query("UPDATE user_profile SET streak_shields = :shields WHERE id = 1")
    suspend fun updateShields(shields: Int)

    @Query("UPDATE user_profile SET adaptive_difficulty = :difficulty WHERE id = 1")
    suspend fun updateAdaptiveDifficulty(difficulty: Float)

    @Query("UPDATE user_profile SET days_since_join = days_since_join + 1 WHERE id = 1")
    suspend fun incrementDayCount()

    @Query("UPDATE user_profile SET consecutive_miss_days = :count, pending_warning = :warning WHERE id = 1")
    suspend fun updateMissState(count: Int, warning: Boolean)
}

package com.arise.habitquest.data.local.database.dao

import androidx.room.*
import com.arise.habitquest.data.local.database.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    @Query("SELECT * FROM missions WHERE due_date = :date ORDER BY type ASC, is_completed ASC")
    fun observeMissionsForDate(date: String): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE due_date = :date ORDER BY type ASC, is_completed ASC")
    suspend fun getMissionsForDate(date: String): List<MissionEntity>

    @Query("SELECT * FROM missions WHERE type = 'WEEKLY' AND due_date >= :weekStart AND due_date <= :weekEnd")
    fun observeWeeklyMissions(weekStart: String, weekEnd: String): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE type = 'BOSS_RAID' ORDER BY due_date DESC LIMIT 3")
    fun observeBossRaids(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE type = 'PENALTY_ZONE' AND is_completed = 0 AND is_failed = 0")
    fun observePenaltyZone(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getMissionById(id: String): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE due_date = :date AND type = 'DAILY'")
    suspend fun deleteDailyMissionsForDate(date: String)

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Query("UPDATE missions SET is_completed = 1, completed_at = :completedAt, streak_count = :streak, accepted_mini_version = :usedMini WHERE id = :id")
    suspend fun markCompleted(id: String, completedAt: Long, streak: Int, usedMini: Boolean)

    @Query("UPDATE missions SET is_failed = 1, streak_count = 0 WHERE id = :id")
    suspend fun markFailed(id: String)

    @Query("UPDATE missions SET is_skipped = 1 WHERE id = :id")
    suspend fun markSkipped(id: String)

    @Query("DELETE FROM missions WHERE due_date < :cutoffDate AND type = 'DAILY'")
    suspend fun pruneOldDailyMissions(cutoffDate: String)

    @Query("SELECT COUNT(*) FROM missions WHERE due_date = :date AND is_completed = 1")
    suspend fun countCompletedForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM missions WHERE due_date = :date AND type = 'DAILY'")
    suspend fun countDailyMissionsForDate(date: String): Int

    @Query("SELECT * FROM missions WHERE due_date >= :from AND due_date <= :to ORDER BY due_date ASC")
    suspend fun getMissionsInRange(from: String, to: String): List<MissionEntity>
}

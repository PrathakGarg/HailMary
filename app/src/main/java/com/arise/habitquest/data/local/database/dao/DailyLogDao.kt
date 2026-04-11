package com.arise.habitquest.data.local.database.dao

import androidx.room.*
import com.arise.habitquest.data.local.database.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {

    @Query("SELECT * FROM daily_logs ORDER BY date DESC LIMIT 30")
    fun observeRecentLogs(): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): DailyLogEntity?

    @Query("SELECT * FROM daily_logs ORDER BY date DESC LIMIT :count")
    suspend fun getRecentLogs(count: Int): List<DailyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyLogEntity)

    @Query("SELECT AVG(completion_rate) FROM daily_logs WHERE date >= :fromDate")
    suspend fun getAverageCompletionRate(fromDate: String): Float?

    @Query("SELECT SUM(xp_gained) - SUM(xp_lost) FROM daily_logs WHERE date >= :fromDate")
    suspend fun getNetXpSince(fromDate: String): Long?

    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    suspend fun getLogsInRange(from: String, to: String): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC LIMIT :count")
    fun observeRecentLogsLive(count: Int): kotlinx.coroutines.flow.Flow<List<DailyLogEntity>>
}

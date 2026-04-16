package com.arise.habitquest.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arise.habitquest.data.local.database.entity.MissionTrackingLogEntity

@Dao
interface MissionTrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MissionTrackingLogEntity)

    @Query(
        """
        SELECT *
        FROM mission_tracking_logs
        WHERE mission_id = :missionId
        ORDER BY created_at DESC
        LIMIT :limit
        """
    )
    suspend fun getLogsForMission(missionId: String, limit: Int = 10): List<MissionTrackingLogEntity>

    @Query(
        """
        SELECT *
        FROM mission_tracking_logs
        ORDER BY created_at DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentLogs(limit: Int = 20): List<MissionTrackingLogEntity>
}

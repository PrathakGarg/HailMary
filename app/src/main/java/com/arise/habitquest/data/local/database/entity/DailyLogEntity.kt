package com.arise.habitquest.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val date: String, // ISO date: 2026-04-11

    @ColumnInfo(name = "completed_ids_json") val completedIdsJson: String = "[]",
    @ColumnInfo(name = "failed_ids_json") val failedIdsJson: String = "[]",
    @ColumnInfo(name = "skipped_ids_json") val skippedIdsJson: String = "[]",

    @ColumnInfo(name = "xp_gained") val xpGained: Int = 0,
    @ColumnInfo(name = "xp_lost") val xpLost: Int = 0,
    @ColumnInfo(name = "hp_lost") val hpLost: Int = 0,
    @ColumnInfo(name = "hp_gained") val hpGained: Int = 0,

    @ColumnInfo(name = "rank_snapshot") val rankSnapshot: String = "E",
    @ColumnInfo(name = "level_snapshot") val levelSnapshot: Int = 1,

    @ColumnInfo(name = "completion_rate") val completionRate: Float = 0f,
    @ColumnInfo(name = "total_missions") val totalMissions: Int = 0,

    @ColumnInfo(name = "system_message") val systemMessage: String = "",

    @ColumnInfo(name = "was_rest_day") val wasRestDay: Boolean = false
)

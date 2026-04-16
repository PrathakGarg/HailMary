package com.arise.habitquest.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mission_tracking_logs",
    indices = [
        Index(value = ["mission_id"]),
        Index(value = ["created_at"])
    ]
)
data class MissionTrackingLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "mission_id")
    val missionId: String,

    @ColumnInfo(name = "mission_title")
    val missionTitle: String,

    @ColumnInfo(name = "mission_due_date")
    val missionDueDate: String,

    @ColumnInfo(name = "primary_label")
    val primaryLabel: String,

    @ColumnInfo(name = "primary_value")
    val primaryValue: String,

    @ColumnInfo(name = "secondary_label")
    val secondaryLabel: String = "",

    @ColumnInfo(name = "secondary_value")
    val secondaryValue: String = "",

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

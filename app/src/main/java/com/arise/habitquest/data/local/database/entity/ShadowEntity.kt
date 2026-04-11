package com.arise.habitquest.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shadows")
data class ShadowEntity(
    @PrimaryKey @ColumnInfo(name = "template_id") val templateId: String,

    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "description") val description: String,

    @ColumnInfo(name = "total_completions") val totalCompletions: Int = 0,
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "best_streak") val bestStreak: Int = 0,

    @ColumnInfo(name = "is_shadow") val isShadow: Boolean = false, // true = 21+ day streak achieved
    @ColumnInfo(name = "last_completed_date") val lastCompletedDate: String = "",

    @ColumnInfo(name = "icon_name") val iconName: String = "auto_awesome",
    @ColumnInfo(name = "xp_per_completion") val xpPerCompletion: Int = 30
)

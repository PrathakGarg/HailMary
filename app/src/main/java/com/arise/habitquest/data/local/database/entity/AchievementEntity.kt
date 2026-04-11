package com.arise.habitquest.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "flavor_text") val flavorText: String,
    @ColumnInfo(name = "icon_name") val iconName: String,

    @ColumnInfo(name = "rarity") val rarity: String, // COMMON / RARE / EPIC / LEGENDARY / MYTHIC

    @ColumnInfo(name = "unlocked_at") val unlockedAt: Long? = null, // null = locked

    @ColumnInfo(name = "progress_current") val progressCurrent: Int = 0,
    @ColumnInfo(name = "progress_target") val progressTarget: Int = 1,

    @ColumnInfo(name = "xp_bonus") val xpBonus: Int = 0,

    // Achievement trigger type and threshold for auto-unlock checks
    @ColumnInfo(name = "trigger_type") val triggerType: String = "MANUAL",
    // MISSIONS_COMPLETED / STREAK_DAYS / LEVEL_REACHED / RANK_REACHED / HP_ZERO / etc.
    @ColumnInfo(name = "trigger_threshold") val triggerThreshold: Int = 1
)

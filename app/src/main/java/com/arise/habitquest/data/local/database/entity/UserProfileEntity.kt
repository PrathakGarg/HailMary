package com.arise.habitquest.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,

    @ColumnInfo(name = "hunter_name") val hunterName: String = "",
    @ColumnInfo(name = "epithet") val epithet: String = "",
    @ColumnInfo(name = "title") val title: String = "The Unawakened",

    @ColumnInfo(name = "rank") val rank: String = "E",
    @ColumnInfo(name = "level") val level: Int = 1,
    @ColumnInfo(name = "xp") val xp: Long = 0L,
    @ColumnInfo(name = "xp_to_next_level") val xpToNextLevel: Long = 100L,

    @ColumnInfo(name = "hp") val hp: Int = 100,
    @ColumnInfo(name = "max_hp") val maxHp: Int = 100,

    @ColumnInfo(name = "stat_str") val statStr: Int = 5,
    @ColumnInfo(name = "stat_agi") val statAgi: Int = 5,
    @ColumnInfo(name = "stat_int") val statInt: Int = 5,
    @ColumnInfo(name = "stat_vit") val statVit: Int = 5,
    @ColumnInfo(name = "stat_end") val statEnd: Int = 5,
    @ColumnInfo(name = "stat_sense") val statSense: Int = 5,

    @ColumnInfo(name = "streak_current") val streakCurrent: Int = 0,
    @ColumnInfo(name = "streak_best") val streakBest: Int = 0,
    @ColumnInfo(name = "days_since_join") val daysSinceJoin: Int = 0,
    @ColumnInfo(name = "total_missions_completed") val totalMissionsCompleted: Int = 0,
    @ColumnInfo(name = "total_xp_earned") val totalXpEarned: Long = 0L,

    @ColumnInfo(name = "streak_shields") val streakShields: Int = 0,
    @ColumnInfo(name = "grace_uses_remaining") val graceUsesRemaining: Int = 3,
    @ColumnInfo(name = "adaptive_difficulty") val adaptiveDifficulty: Float = 1.0f,

    @ColumnInfo(name = "rest_day") val restDay: Int = 6, // ISO DayOfWeek ordinal: MONDAY=0 … SUNDAY=6
    @ColumnInfo(name = "notification_hour") val notificationHour: Int = 8,
    @ColumnInfo(name = "notification_evening_hour") val notificationEveningHour: Int = 20,

    @ColumnInfo(name = "onboarding_complete") val onboardingComplete: Boolean = false,
    @ColumnInfo(name = "onboarding_answers_json") val onboardingAnswersJson: String = "",

    @ColumnInfo(name = "join_date") val joinDate: String = "", // ISO date string

    // Warning flag: set when first daily miss occurs (grace period)
    @ColumnInfo(name = "pending_warning") val pendingWarning: Boolean = false,
    // Count of consecutive days with 0 completions
    @ColumnInfo(name = "consecutive_miss_days") val consecutiveMissDays: Int = 0
)

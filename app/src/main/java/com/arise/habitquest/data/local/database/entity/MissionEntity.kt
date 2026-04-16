package com.arise.habitquest.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String,

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "system_lore") val systemLore: String,
    @ColumnInfo(name = "mini_mission_description") val miniMissionDescription: String = "",

    @ColumnInfo(name = "type") val type: String,           // DAILY / WEEKLY / BOSS_RAID / SPECIAL / PENALTY_ZONE
    @ColumnInfo(name = "category") val category: String,   // PHYSICAL / MENTAL / PRODUCTIVITY / SOCIAL / WELLNESS / CREATIVITY
    @ColumnInfo(name = "difficulty") val difficulty: String, // F / E / D / C / B / A / S

    @ColumnInfo(name = "xp_reward") val xpReward: Int,
    @ColumnInfo(name = "penalty_xp") val penaltyXp: Int,
    @ColumnInfo(name = "penalty_hp") val penaltyHp: Int,
    @ColumnInfo(name = "stat_rewards_json") val statRewardsJson: String = "{}", // JSON: {"STR":1,"VIT":1}

    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "is_failed") val isFailed: Boolean = false,
    @ColumnInfo(name = "is_skipped") val isSkipped: Boolean = false,
    @ColumnInfo(name = "accepted_mini_version") val acceptedMiniVersion: Boolean = false,

    @ColumnInfo(name = "due_date") val dueDate: String,    // ISO date: 2026-04-11
    @ColumnInfo(name = "scheduled_time_hint") val scheduledTimeHint: String? = null, // MORNING/AFTERNOON/EVENING

    @ColumnInfo(name = "streak_count") val streakCount: Int = 0,
    @ColumnInfo(name = "is_recurring") val isRecurring: Boolean = true,
    @ColumnInfo(name = "parent_template_id") val parentTemplateId: String? = null,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,

    // Weekly boss progress fields
    @ColumnInfo(name = "progress_current") val progressCurrent: Int = 0,
    @ColumnInfo(name = "progress_target") val progressTarget: Int = 1,

    // Category icon name (maps to Material icon name string)
    @ColumnInfo(name = "icon_name") val iconName: String = "fitness_center",

    // Weekly progression metadata for PHYSICAL missions
    @ColumnInfo(name = "physical_family") val physicalFamily: String = "UNSPECIFIED",
    @ColumnInfo(name = "muscle_load_json") val muscleLoadJson: String = "{}",

    // True when the System injected this mission for all-round growth (not user's chosen goals)
    @ColumnInfo(name = "is_system_mandate") val isSystemMandate: Boolean = false
)

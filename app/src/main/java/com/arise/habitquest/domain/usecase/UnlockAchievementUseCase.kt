package com.arise.habitquest.domain.usecase

import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.AchievementTrigger
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.AchievementRepository
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.data.time.TimeProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class UnlockAchievementUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val timeProvider: TimeProvider
) {
    private companion object {
        const val BOSS_DEFEATED_COUNT_KEY = "__boss_defeated_count"
        const val SHADOW_UNLOCKED_COUNT_KEY = "__shadow_unlocked_count"
        const val PERFECT_WEEK_VALUE_KEY = "__perfect_week_value"
    }

    private val repairableTriggers = listOf(
        AchievementTrigger.MISSIONS_COMPLETED,
        AchievementTrigger.STREAK_DAYS,
        AchievementTrigger.LEVEL_REACHED,
        AchievementTrigger.RANK_REACHED,
        AchievementTrigger.XP_TOTAL
    )

    suspend operator fun invoke(profile: UserProfile): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        val sessionDay = timeProvider.sessionDay()
        val now = timeProvider.trustedNow().toLocalTime()
        val customProgressById = buildCustomProgressMap(sessionDay)

        // Check missions-completed achievements
        checkTrigger(
            trigger = AchievementTrigger.MISSIONS_COMPLETED,
            value = profile.totalMissionsCompleted,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        // Check streak achievements
        checkTrigger(
            trigger = AchievementTrigger.STREAK_DAYS,
            value = profile.streakCurrent,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        // Check level achievements
        checkTrigger(
            trigger = AchievementTrigger.LEVEL_REACHED,
            value = profile.level,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.RANK_REACHED,
            value = profile.rank.order,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.XP_TOTAL,
            value = profile.totalXpEarned.toInt(),
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.BOSS_DEFEATED,
            value = customProgressById[BOSS_DEFEATED_COUNT_KEY] ?: 0,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.SHADOW_UNLOCKED,
            value = customProgressById[SHADOW_UNLOCKED_COUNT_KEY] ?: 0,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.PERFECT_WEEK,
            value = customProgressById[PERFECT_WEEK_VALUE_KEY] ?: 0,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.EARLY_BIRD,
            value = if (now < LocalTime.of(7, 0)) 1 else 0,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.NIGHT_OWL,
            value = if (now >= LocalTime.of(22, 0) || now < LocalTime.of(1, 0)) 1 else 0,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        checkTrigger(
            trigger = AchievementTrigger.HP_ZERO,
            value = if (profile.hp <= 0) 1 else 0,
            customProgressById = customProgressById,
            newlyUnlocked = newlyUnlocked
        )

        // Award XP bonuses from newly unlocked achievements
        val totalBonusXp = newlyUnlocked.sumOf { it.xpBonus }
        if (totalBonusXp > 0) {
            val updatedProfile = profile.copy(xp = profile.xp + totalBonusXp)
            userRepository.updateXpAndLevel(
                updatedProfile.xp,
                updatedProfile.level,
                updatedProfile.rank,
                updatedProfile.xpToNextLevel
            )
        }

        return newlyUnlocked
    }

    suspend fun reconcileInvalidUnlocks(profile: UserProfile): Int {
        val sessionDay = timeProvider.sessionDay()
        val customProgressById = buildCustomProgressMap(sessionDay)
        var relockedCount = 0

        for (trigger in repairableTriggers) {
            val baseValue = triggerValue(trigger, profile)
            val candidates = achievementRepository.getAchievementsByTrigger(trigger)
            for (achievement in candidates) {
                val progress = customProgressById[achievement.id] ?: baseValue
                achievementRepository.updateProgress(achievement.id, progress)
                if (achievement.isUnlocked && progress < achievement.triggerThreshold) {
                    achievementRepository.relockAchievement(achievement.id)
                    relockedCount += 1
                }
            }
        }

        return relockedCount
    }

    private suspend fun checkTrigger(
        trigger: AchievementTrigger,
        value: Int,
        customProgressById: Map<String, Int>,
        newlyUnlocked: MutableList<Achievement>
    ) {
        val candidates = achievementRepository.getAchievementsByTrigger(trigger)
        for (achievement in candidates) {
            if (achievement.isUnlocked) continue
            val progress = customProgressById[achievement.id] ?: value
            // Update progress
            achievementRepository.updateProgress(achievement.id, progress)
            // Check if threshold met
            if (progress >= achievement.triggerThreshold) {
                achievementRepository.unlockAchievement(achievement.id)
                newlyUnlocked.add(achievement)
            }
        }
    }

    private fun triggerValue(trigger: AchievementTrigger, profile: UserProfile): Int = when (trigger) {
        AchievementTrigger.MISSIONS_COMPLETED -> profile.totalMissionsCompleted
        AchievementTrigger.STREAK_DAYS -> profile.streakCurrent
        AchievementTrigger.LEVEL_REACHED -> profile.level
        AchievementTrigger.RANK_REACHED -> profile.rank.order
        AchievementTrigger.XP_TOTAL -> profile.totalXpEarned.toInt()
        else -> 0
    }

    private suspend fun buildCustomProgressMap(sessionDay: LocalDate): Map<String, Int> {
        val allMissions = missionRepository.getMissionsInRange("0001-01-01", "9999-12-31")
        val completedMissions = allMissions.filter { it.isCompleted }
        val categoryCounts = MissionCategory.entries.associateWith { category ->
            completedMissions.count { it.category == category }
        }

        val weekStart = sessionDay.minusDays(6)
        val completedCategoriesInWeek = completedMissions
            .asSequence()
            .filter { !it.dueDate.isBefore(weekStart) && !it.dueDate.isAfter(sessionDay) }
            .map { it.category }
            .toSet()
        val bossDefeatedCount = completedMissions.count { it.type.name == "BOSS_RAID" }
        val unlockedShadows = completedMissions.count { it.streakCount >= 21 }

        val yesterday = sessionDay.minusDays(1)
        val lastSevenDays = (0..6).map { sessionDay.minusDays(it.toLong()) }
        val yesterdayMissions = missionRepository.getMissionsForDate(yesterday)
        val todayMissions = missionRepository.getMissionsForDate(sessionDay)

        val yesterdayDaily = yesterdayMissions.filter { it.type.name == "DAILY" }
        val todayDaily = todayMissions.filter { it.type.name == "DAILY" }

        val hadZeroCompletionYesterday =
            yesterdayDaily.isNotEmpty() && yesterdayDaily.none { it.isCompleted }
        val isPerfectToday =
            todayDaily.isNotEmpty() && todayDaily.all { it.isCompleted }
        val wasPartialYesterday =
            yesterdayDaily.any { it.isCompleted } && yesterdayDaily.any { !it.isCompleted }

        val hasPerfectWeek = lastSevenDays.all { day ->
            val dailyMissions = allMissions.filter { it.dueDate == day && it.type.name == "DAILY" }
            dailyMissions.isNotEmpty() && dailyMissions.all { it.isCompleted }
        }

        val now = timeProvider.trustedNow().toLocalTime()
        val isMidnightMissionWindow = now >= LocalTime.MIDNIGHT && now < LocalTime.of(1, 0)

        return mapOf(
            "ach_physical_10" to (categoryCounts[MissionCategory.PHYSICAL] ?: 0),
            "ach_physical_25" to (categoryCounts[MissionCategory.PHYSICAL] ?: 0),
            "ach_physical_50" to (categoryCounts[MissionCategory.PHYSICAL] ?: 0),
            "ach_mental_10" to (categoryCounts[MissionCategory.MENTAL] ?: 0),
            "ach_mental_25" to (categoryCounts[MissionCategory.MENTAL] ?: 0),
            "ach_mental_50" to (categoryCounts[MissionCategory.MENTAL] ?: 0),
            "ach_wellness_10" to (categoryCounts[MissionCategory.WELLNESS] ?: 0),
            "ach_wellness_25" to (categoryCounts[MissionCategory.WELLNESS] ?: 0),
            "ach_wellness_50" to (categoryCounts[MissionCategory.WELLNESS] ?: 0),
            "ach_productivity_10" to (categoryCounts[MissionCategory.PRODUCTIVITY] ?: 0),
            "ach_productivity_25" to (categoryCounts[MissionCategory.PRODUCTIVITY] ?: 0),
            "ach_productivity_50" to (categoryCounts[MissionCategory.PRODUCTIVITY] ?: 0),
            "ach_social_10" to (categoryCounts[MissionCategory.SOCIAL] ?: 0),
            "ach_social_25" to (categoryCounts[MissionCategory.SOCIAL] ?: 0),
            "ach_social_50" to (categoryCounts[MissionCategory.SOCIAL] ?: 0),
            "ach_creativity_10" to (categoryCounts[MissionCategory.CREATIVITY] ?: 0),
            "ach_creativity_25" to (categoryCounts[MissionCategory.CREATIVITY] ?: 0),
            "ach_creativity_50" to (categoryCounts[MissionCategory.CREATIVITY] ?: 0),
            "ach_comeback" to if (hadZeroCompletionYesterday) 1 else 0,
            "ach_perfect_after_partial" to if (wasPartialYesterday && isPerfectToday) 1 else 0,
            "ach_sunday_complete" to if (sessionDay.dayOfWeek == DayOfWeek.SUNDAY && isPerfectToday) 1 else 0,
            "ach_six_category_week" to completedCategoriesInWeek.size,
            "ach_before_7am" to if (now < LocalTime.of(7, 0)) 1 else 0,
            "ach_midnight_mission" to if (isMidnightMissionWindow) 1 else 0,
            "ach_perfect_week_7" to if (hasPerfectWeek) 1 else 0,
            BOSS_DEFEATED_COUNT_KEY to bossDefeatedCount,
            SHADOW_UNLOCKED_COUNT_KEY to unlockedShadows,
            PERFECT_WEEK_VALUE_KEY to if (hasPerfectWeek) 1 else 0
        )
    }
}

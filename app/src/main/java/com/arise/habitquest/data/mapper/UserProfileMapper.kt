package com.arise.habitquest.data.mapper

import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.domain.model.*
import java.time.LocalDate
import javax.inject.Inject

class UserProfileMapper @Inject constructor() {

    fun toDomain(entity: UserProfileEntity): UserProfile = UserProfile(
        id = entity.id,
        hunterName = entity.hunterName,
        epithet = entity.epithet,
        title = entity.title,
        rank = Rank.fromString(entity.rank),
        level = entity.level,
        xp = entity.xp,
        xpToNextLevel = entity.xpToNextLevel,
        hp = entity.hp,
        maxHp = entity.maxHp,
        stats = HunterStats(
            str = entity.statStr,
            agi = entity.statAgi,
            int = entity.statInt,
            vit = entity.statVit,
            end = entity.statEnd,
            sense = entity.statSense
        ),
        streakCurrent = entity.streakCurrent,
        streakBest = entity.streakBest,
        daysSinceJoin = entity.daysSinceJoin,
        totalMissionsCompleted = entity.totalMissionsCompleted,
        totalXpEarned = entity.totalXpEarned,
        streakShields = entity.streakShields,
        graceUsesRemaining = entity.graceUsesRemaining,
        adaptiveDifficulty = entity.adaptiveDifficulty,
        restDay = entity.restDay,
        notificationHour = entity.notificationHour,
        onboardingComplete = entity.onboardingComplete,
        onboardingAnswersJson = entity.onboardingAnswersJson,
        joinDate = entity.joinDate.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) },
        pendingWarning = entity.pendingWarning,
        consecutiveMissDays = entity.consecutiveMissDays
    )

    fun toEntity(domain: UserProfile): UserProfileEntity = UserProfileEntity(
        id = domain.id,
        hunterName = domain.hunterName,
        epithet = domain.epithet,
        title = domain.title,
        rank = domain.rank.name,
        level = domain.level,
        xp = domain.xp,
        xpToNextLevel = domain.xpToNextLevel,
        hp = domain.hp,
        maxHp = domain.maxHp,
        statStr = domain.stats.str,
        statAgi = domain.stats.agi,
        statInt = domain.stats.int,
        statVit = domain.stats.vit,
        statEnd = domain.stats.end,
        statSense = domain.stats.sense,
        streakCurrent = domain.streakCurrent,
        streakBest = domain.streakBest,
        daysSinceJoin = domain.daysSinceJoin,
        totalMissionsCompleted = domain.totalMissionsCompleted,
        totalXpEarned = domain.totalXpEarned,
        streakShields = domain.streakShields,
        graceUsesRemaining = domain.graceUsesRemaining,
        adaptiveDifficulty = domain.adaptiveDifficulty,
        restDay = domain.restDay,
        notificationHour = domain.notificationHour,
        onboardingComplete = domain.onboardingComplete,
        onboardingAnswersJson = domain.onboardingAnswersJson,
        joinDate = domain.joinDate?.toString() ?: "",
        pendingWarning = domain.pendingWarning,
        consecutiveMissDays = domain.consecutiveMissDays
    )
}

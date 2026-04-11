package com.arise.habitquest.data.mapper

import com.arise.habitquest.data.local.database.entity.AchievementEntity
import com.arise.habitquest.domain.model.*
import java.time.Instant
import javax.inject.Inject

class AchievementMapper @Inject constructor() {

    fun toDomain(entity: AchievementEntity): Achievement = Achievement(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        flavorText = entity.flavorText,
        iconName = entity.iconName,
        rarity = Rarity.fromString(entity.rarity),
        unlockedAt = entity.unlockedAt?.let { Instant.ofEpochMilli(it) },
        progressCurrent = entity.progressCurrent,
        progressTarget = entity.progressTarget,
        xpBonus = entity.xpBonus,
        triggerType = AchievementTrigger.fromString(entity.triggerType),
        triggerThreshold = entity.triggerThreshold
    )

    fun toEntity(domain: Achievement): AchievementEntity = AchievementEntity(
        id = domain.id,
        title = domain.title,
        description = domain.description,
        flavorText = domain.flavorText,
        iconName = domain.iconName,
        rarity = domain.rarity.name,
        unlockedAt = domain.unlockedAt?.toEpochMilli(),
        progressCurrent = domain.progressCurrent,
        progressTarget = domain.progressTarget,
        xpBonus = domain.xpBonus,
        triggerType = domain.triggerType.name,
        triggerThreshold = domain.triggerThreshold
    )
}

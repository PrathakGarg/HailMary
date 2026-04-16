package com.arise.habitquest.data.mapper

import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.domain.model.*
import kotlinx.serialization.json.*
import java.time.LocalDate
import javax.inject.Inject

class MissionMapper @Inject constructor() {

    fun toDomain(entity: MissionEntity): Mission {
        val statRewards = parseStatRewards(entity.statRewardsJson)
        return Mission(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            systemLore = entity.systemLore,
            miniMissionDescription = entity.miniMissionDescription,
            type = MissionType.entries.find { it.name == entity.type } ?: MissionType.DAILY,
            category = MissionCategory.entries.find { it.name == entity.category } ?: MissionCategory.PHYSICAL,
            difficulty = Difficulty.fromString(entity.difficulty),
            xpReward = entity.xpReward,
            penaltyXp = entity.penaltyXp,
            penaltyHp = entity.penaltyHp,
            statRewards = statRewards,
            isCompleted = entity.isCompleted,
            isFailed = entity.isFailed,
            isSkipped = entity.isSkipped,
            acceptedMiniVersion = entity.acceptedMiniVersion,
            dueDate = LocalDate.parse(entity.dueDate),
            scheduledTimeHint = entity.scheduledTimeHint,
            streakCount = entity.streakCount,
            isRecurring = entity.isRecurring,
            parentTemplateId = entity.parentTemplateId,
            progressCurrent = entity.progressCurrent,
            progressTarget = entity.progressTarget,
            iconName = entity.iconName,
            isSystemMandate = entity.isSystemMandate,
            physicalFamily = runCatching {
                PhysicalMissionFamily.valueOf(entity.physicalFamily)
            }.getOrDefault(PhysicalMissionFamily.UNSPECIFIED),
            muscleLoad = parseMuscleLoad(entity.muscleLoadJson)
        )
    }

    fun toEntity(domain: Mission): MissionEntity = MissionEntity(
        id = domain.id,
        title = domain.title,
        description = domain.description,
        systemLore = domain.systemLore,
        miniMissionDescription = domain.miniMissionDescription,
        type = domain.type.name,
        category = domain.category.name,
        difficulty = domain.difficulty.name,
        xpReward = domain.xpReward,
        penaltyXp = domain.penaltyXp,
        penaltyHp = domain.penaltyHp,
        statRewardsJson = encodeStatRewards(domain.statRewards),
        isCompleted = domain.isCompleted,
        isFailed = domain.isFailed,
        isSkipped = domain.isSkipped,
        acceptedMiniVersion = domain.acceptedMiniVersion,
        dueDate = domain.dueDate.toString(),
        scheduledTimeHint = domain.scheduledTimeHint,
        streakCount = domain.streakCount,
        isRecurring = domain.isRecurring,
        parentTemplateId = domain.parentTemplateId,
        progressCurrent = domain.progressCurrent,
        progressTarget = domain.progressTarget,
        iconName = domain.iconName,
        physicalFamily = domain.physicalFamily.name,
        muscleLoadJson = encodeMuscleLoad(domain.muscleLoad),
        isSystemMandate = domain.isSystemMandate
    )

    private fun parseStatRewards(json: String): Map<Stat, Int> {
        return try {
            val obj = Json.parseToJsonElement(json).jsonObject
            obj.entries.associate { (key, value) ->
                Stat.valueOf(key) to value.jsonPrimitive.int
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun encodeStatRewards(rewards: Map<Stat, Int>): String {
        val obj = buildJsonObject {
            rewards.forEach { (stat, amount) -> put(stat.name, amount) }
        }
        return obj.toString()
    }

    private fun parseMuscleLoad(json: String): Map<MuscleRegion, Float> {
        return try {
            val obj = Json.parseToJsonElement(json).jsonObject
            obj.entries.associate { (key, value) ->
                MuscleRegion.valueOf(key) to value.jsonPrimitive.float
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun encodeMuscleLoad(load: Map<MuscleRegion, Float>): String {
        val obj = buildJsonObject {
            load.forEach { (region, weight) -> put(region.name, weight) }
        }
        return obj.toString()
    }
}

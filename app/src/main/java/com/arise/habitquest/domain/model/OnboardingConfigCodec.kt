package com.arise.habitquest.domain.model

import kotlinx.serialization.json.*
import java.time.DayOfWeek

object OnboardingConfigCodec {

    fun encode(
        templateIds: List<String>,
        restDay: DayOfWeek,
        startingDifficulty: StartingDifficulty,
        goals: Set<Goal>
    ): String = buildJsonObject {
        put("templateIds", buildJsonArray { templateIds.forEach { add(it) } })
        put("restDay", restDay.ordinal)
        put("startingDifficulty", startingDifficulty.name)
        put("goals", buildJsonArray { goals.forEach { add(it.name) } })
    }.toString()

    data class OnboardingConfig(
        val templateIds: List<String>,
        val goalCategories: Set<MissionCategory>
    )

    fun decode(json: String): OnboardingConfig = try {
        val root = Json.parseToJsonElement(json).jsonObject
        val templateIds = (root["templateIds"] as? JsonArray)
            ?.map { it.jsonPrimitive.content }
            .orEmpty()
        val goalCategories = (root["goals"] as? JsonArray)
            ?.mapNotNull { goalName ->
                runCatching { Goal.valueOf(goalName.jsonPrimitive.content).primaryCategory }.getOrNull()
            }
            ?.toSet()
            .orEmpty()
        OnboardingConfig(templateIds = templateIds, goalCategories = goalCategories)
    } catch (_: Exception) {
        OnboardingConfig(emptyList(), emptySet())
    }
}

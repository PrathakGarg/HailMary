package com.arise.habitquest.domain.model

import kotlinx.serialization.json.*
import java.time.DayOfWeek

object OnboardingConfigCodec {

    fun encode(
        templateIds: List<String>,
        restDay: DayOfWeek,
        startingDifficulty: StartingDifficulty,
        goals: Set<Goal>,
        fitnessLevel: FitnessLevel,
        sleepQuality: SleepQuality,
        stressLevel: StressLevel,
        workHoursPerDay: Int,
        availableTime: AvailableTime,
        failureResponse: FailureResponse,
        accountabilityStyle: AccountabilityStyle,
        longestStreak: LongestStreak,
        failureReasons: Set<FailureReason>,
        progressionPreference: ProgressionPreference,
        scheduleStyle: ScheduleStyle,
        equipmentMode: EquipmentMode,
        trackFocus: MissionCategory,
        shoulderRiskFlag: Boolean,
        heatRiskFlag: Boolean
    ): String = buildJsonObject {
        put("templateIds", buildJsonArray { templateIds.forEach { add(it) } })
        put("restDay", restDay.ordinal)
        put("startingDifficulty", startingDifficulty.name)
        put("goals", buildJsonArray { goals.forEach { add(it.name) } })
        put("fitnessLevel", fitnessLevel.name)
        put("sleepQuality", sleepQuality.name)
        put("stressLevel", stressLevel.name)
        put("workHoursPerDay", workHoursPerDay)
        put("availableTime", availableTime.name)
        put("failureResponse", failureResponse.name)
        put("accountabilityStyle", accountabilityStyle.name)
        put("longestStreak", longestStreak.name)
        put("failureReasons", buildJsonArray { failureReasons.forEach { add(it.name) } })
        put("progressionPreference", progressionPreference.name)
        put("scheduleStyle", scheduleStyle.name)
        put("equipmentMode", equipmentMode.name)
        put("trackFocus", trackFocus.name)
        put("shoulderRiskFlag", shoulderRiskFlag)
        put("heatRiskFlag", heatRiskFlag)
    }.toString()

    data class OnboardingConfig(
        val templateIds: List<String>,
        val goalCategories: Set<MissionCategory>,
        val progressionPreference: ProgressionPreference = ProgressionPreference.ASSERTIVE_SAFE,
        val scheduleStyle: ScheduleStyle = ScheduleStyle.FIXED_WINDOW,
        val equipmentMode: EquipmentMode = EquipmentMode.BODYWEIGHT,
        val trackFocus: MissionCategory = MissionCategory.PHYSICAL,
        val shoulderRiskFlag: Boolean = false,
        val heatRiskFlag: Boolean = false,
        val fitnessLevel: FitnessLevel = FitnessLevel.SEDENTARY,
        val sleepQuality: SleepQuality = SleepQuality.OKAY,
        val stressLevel: StressLevel = StressLevel.MEDIUM,
        val workHoursPerDay: Int = 8,
        val availableTime: AvailableTime = AvailableTime.EVENING,
        val failureResponse: FailureResponse = FailureResponse.NEED_TIME,
        val accountabilityStyle: AccountabilityStyle = AccountabilityStyle.BALANCED,
        val longestStreak: LongestStreak = LongestStreak.NEVER,
        val failureReasons: Set<FailureReason> = emptySet()
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
        val progressionPreference = root["progressionPreference"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { ProgressionPreference.valueOf(it) }.getOrNull() }
            ?: ProgressionPreference.ASSERTIVE_SAFE
        val scheduleStyle = root["scheduleStyle"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { ScheduleStyle.valueOf(it) }.getOrNull() }
            ?: ScheduleStyle.FIXED_WINDOW
        val equipmentMode = root["equipmentMode"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { EquipmentMode.valueOf(it) }.getOrNull() }
            ?: EquipmentMode.BODYWEIGHT
        val trackFocus = root["trackFocus"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { MissionCategory.valueOf(it) }.getOrNull() }
            ?: MissionCategory.PHYSICAL
        val shoulderRiskFlag = root["shoulderRiskFlag"]?.jsonPrimitive?.booleanOrNull ?: false
        val heatRiskFlag = root["heatRiskFlag"]?.jsonPrimitive?.booleanOrNull ?: false
        val fitnessLevel = root["fitnessLevel"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { FitnessLevel.valueOf(it) }.getOrNull() }
            ?: FitnessLevel.SEDENTARY
        val sleepQuality = root["sleepQuality"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { SleepQuality.valueOf(it) }.getOrNull() }
            ?: SleepQuality.OKAY
        val stressLevel = root["stressLevel"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { StressLevel.valueOf(it) }.getOrNull() }
            ?: StressLevel.MEDIUM
        val workHoursPerDay = root["workHoursPerDay"]?.jsonPrimitive?.intOrNull ?: 8
        val availableTime = root["availableTime"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { AvailableTime.valueOf(it) }.getOrNull() }
            ?: AvailableTime.EVENING
        val failureResponse = root["failureResponse"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { FailureResponse.valueOf(it) }.getOrNull() }
            ?: FailureResponse.NEED_TIME
        val accountabilityStyle = root["accountabilityStyle"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { AccountabilityStyle.valueOf(it) }.getOrNull() }
            ?: AccountabilityStyle.BALANCED
        val longestStreak = root["longestStreak"]
            ?.jsonPrimitive?.content
            ?.let { runCatching { LongestStreak.valueOf(it) }.getOrNull() }
            ?: LongestStreak.NEVER
        val failureReasons = (root["failureReasons"] as? JsonArray)
            ?.mapNotNull { reason ->
                runCatching { FailureReason.valueOf(reason.jsonPrimitive.content) }.getOrNull()
            }
            ?.toSet()
            .orEmpty()

        OnboardingConfig(
            templateIds = templateIds,
            goalCategories = goalCategories,
            progressionPreference = progressionPreference,
            scheduleStyle = scheduleStyle,
            equipmentMode = equipmentMode,
            trackFocus = trackFocus,
            shoulderRiskFlag = shoulderRiskFlag,
            heatRiskFlag = heatRiskFlag,
            fitnessLevel = fitnessLevel,
            sleepQuality = sleepQuality,
            stressLevel = stressLevel,
            workHoursPerDay = workHoursPerDay,
            availableTime = availableTime,
            failureResponse = failureResponse,
            accountabilityStyle = accountabilityStyle,
            longestStreak = longestStreak,
            failureReasons = failureReasons
        )
    } catch (_: Exception) {
        OnboardingConfig(emptyList(), emptySet())
    }
}

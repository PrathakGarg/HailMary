package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.Goal
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import javax.inject.Inject

class GenerateDailyMissionsUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val generator: MissionGenerator,
    private val dataStore: OnboardingDataStore
) {
    private val inboxMissionTemplateIds = setOf("tpl_inbox_zero", "tpl_two_minute_sweep")

    suspend operator fun invoke(profile: UserProfile, date: LocalDate) {
        dataStore.pruneMissionRollbackLedger()

        val onboardingConfig = parseOnboardingConfig(profile)
        val baseTemplateIds = onboardingConfig.templateIds
        if (baseTemplateIds.isEmpty()) return

        // Guard: don't insert a second set of missions if this date already has some.
        // Prevents duplicates from worker retries, app restarts on the same day, etc.
        if (missionRepository.countDailyMissionsForDate(date) > 0) return

        // Fetch active focus themes from DataStore
        val focusThemes = dataStore.focusThemes.first()
        val deprioritizedTemplateIds = dataStore.deprioritizedTemplateIds.first()
        val excludedTemplateIds = (if (dataStore.excludeInboxMissions.first()) {
            inboxMissionTemplateIds
        } else {
            emptySet()
        }) + deprioritizedTemplateIds

        val recentTemplateIds = missionRepository
            .getMissionsInRange(date.minusDays(3).toString(), date.minusDays(1).toString())
            .mapNotNull { it.parentTemplateId }
            .toSet()

        val selection = generator.selectDailyTemplateIds(
            profile = profile,
            baseTemplateIds = baseTemplateIds,
            goalCategories = onboardingConfig.goalCategories,
            date = date,
            recentTemplateIds = recentTemplateIds,
            focusThemeNames = focusThemes,
            excludedTemplateIds = excludedTemplateIds
        )

        // Fetch per-template completion counts from shadow records
        val shadowCompletions = userRepository.getShadowCompletions(selection.allIds)

        val missions = generator.generateDailyMissions(
            profile = profile,
            templateIds = selection.allIds,
            date = date,
            shadowCompletions = shadowCompletions,
            focusThemeNames = focusThemes,
            excludedTemplateIds = excludedTemplateIds
        )
        missionRepository.insertMissions(missions)
    }

    private data class OnboardingConfig(
        val templateIds: List<String>,
        val goalCategories: Set<MissionCategory>
    )

    private fun parseOnboardingConfig(profile: UserProfile): OnboardingConfig {
        return try {
            val json = Json.parseToJsonElement(profile.onboardingAnswersJson)
            val jsonObject = json.jsonObject
            val templateIds = (jsonObject["templateIds"] as? JsonArray)
                ?.map { it.jsonPrimitive.content }
                .orEmpty()
            val goalCategories = (jsonObject["goals"] as? JsonArray)
                ?.mapNotNull { goalName ->
                    runCatching { Goal.valueOf(goalName.jsonPrimitive.content).primaryCategory }.getOrNull()
                }
                ?.toSet()
                .orEmpty()
            OnboardingConfig(templateIds = templateIds, goalCategories = goalCategories)
        } catch (e: Exception) {
            OnboardingConfig(emptyList(), emptySet())
        }
    }
}

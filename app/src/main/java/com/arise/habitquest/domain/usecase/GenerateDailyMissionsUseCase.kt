package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
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
    suspend operator fun invoke(profile: UserProfile, date: LocalDate) {
        val templateIds = parseTemplateIds(profile)
        if (templateIds.isEmpty()) return

        // Guard: don't insert a second set of missions if this date already has some.
        // Prevents duplicates from worker retries, app restarts on the same day, etc.
        if (missionRepository.countDailyMissionsForDate(date) > 0) return

        // Fetch per-template completion counts from shadow records
        val shadowCompletions = userRepository.getShadowCompletions(templateIds)

        // Fetch active focus themes from DataStore
        val focusThemes = dataStore.focusThemes.first()

        val missions = generator.generateDailyMissions(
            profile = profile,
            templateIds = templateIds,
            date = date,
            shadowCompletions = shadowCompletions,
            focusThemeNames = focusThemes
        )
        missionRepository.insertMissions(missions)
    }

    private fun parseTemplateIds(profile: UserProfile): List<String> {
        return try {
            val json = Json.parseToJsonElement(profile.onboardingAnswersJson)
            val array = json.jsonObject["templateIds"] as? JsonArray ?: return emptyList()
            array.map { it.jsonPrimitive.content }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

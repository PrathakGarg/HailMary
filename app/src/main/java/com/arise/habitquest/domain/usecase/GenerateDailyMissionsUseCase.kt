package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.OnboardingConfigCodec
import com.arise.habitquest.domain.model.PhysicalMissionFamily
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.MissionExclusions
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class GenerateDailyMissionsUseCase @Inject constructor(
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val generator: MissionGenerator,
    private val dataStore: OnboardingDataStore
) {
    suspend operator fun invoke(profile: UserProfile, date: LocalDate) {
        dataStore.pruneMissionRollbackLedger()

        val onboardingConfig = OnboardingConfigCodec.decode(profile.onboardingAnswersJson)
        val baseTemplateIds = onboardingConfig.templateIds
        if (baseTemplateIds.isEmpty()) return

        // Guard: don't insert a second set of missions if this date already has some.
        // Prevents duplicates from worker retries, app restarts on the same day, etc.
        if (missionRepository.countDailyMissionsForDate(date) > 0) return

        // Fetch active focus themes from DataStore
        val focusThemes = dataStore.focusThemes.first()
        val deprioritizedTemplateIds = dataStore.deprioritizedTemplateIds.first()
        val excludedTemplateIds = (if (dataStore.excludeInboxMissions.first()) {
            MissionExclusions.INBOX_TEMPLATE_IDS
        } else {
            emptySet()
        }) + deprioritizedTemplateIds

        val recentTemplateIds = missionRepository
            .getMissionsInRange(date.minusDays(3).toString(), date.minusDays(1).toString())
            .mapNotNull { it.parentTemplateId }
            .toSet()

        val weekStart = date.with(DayOfWeek.MONDAY)
        val weekToDateMissions = if (!date.isEqual(weekStart)) {
            missionRepository.getMissionsInRange(weekStart.toString(), date.minusDays(1).toString())
        } else {
            emptyList()
        }

        val weekToDateFamilyCounts = weekToDateMissions
            .asSequence()
            .filter { it.type.name == "DAILY" }
            .filter { it.category.name == "PHYSICAL" }
            .filter { it.isCompleted || it.isActive }
            .map { it.physicalFamily }
            .filter { it != PhysicalMissionFamily.UNSPECIFIED }
            .groupingBy { it }
            .eachCount()

        val weekToDateFamilyMissCounts = weekToDateMissions
            .asSequence()
            .filter { it.type.name == "DAILY" }
            .filter { it.category.name == "PHYSICAL" }
            .filter { it.isFailed || it.isSkipped }
            .map { it.physicalFamily }
            .filter { it != PhysicalMissionFamily.UNSPECIFIED }
            .groupingBy { it }
            .eachCount()

        val weekToDateCategoryCounts = weekToDateMissions
            .asSequence()
            .filter { it.type.name == "DAILY" }
            .filter { it.isCompleted || it.isActive }
            .groupingBy { it.category }
            .eachCount()

        val recentPhysicalFriction = missionRepository
            .getMissionsInRange(date.minusDays(7).toString(), date.minusDays(1).toString())
            .asSequence()
            .filter { it.type.name == "DAILY" }
            .filter { it.category.name == "PHYSICAL" }
            .sumOf { mission ->
                when {
                    mission.isFailed || mission.isSkipped -> 2L
                    mission.isCompleted && mission.acceptedMiniVersion -> 1L
                    else -> 0L
                }
            }
        val frictionThreshold = when (onboardingConfig.progressionPreference) {
            com.arise.habitquest.domain.model.ProgressionPreference.CONSERVATIVE -> 3L
            com.arise.habitquest.domain.model.ProgressionPreference.AGGRESSIVE -> 5L
            else -> 4L
        }
        val safetyThrottle = recentPhysicalFriction >= frictionThreshold

        val selection = generator.selectDailyTemplateIds(
            profile = profile,
            baseTemplateIds = baseTemplateIds,
            goalCategories = onboardingConfig.goalCategories,
            date = date,
            recentTemplateIds = recentTemplateIds,
            weekToDateFamilyCounts = weekToDateFamilyCounts,
                weekToDateCategoryCounts = weekToDateCategoryCounts,
            weekToDateFamilyMissCounts = weekToDateFamilyMissCounts,
            safetyThrottle = safetyThrottle,
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
}

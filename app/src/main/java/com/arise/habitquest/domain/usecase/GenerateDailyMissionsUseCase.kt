package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.OnboardingConfigCodec
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.policy.MissionExclusions
import kotlinx.coroutines.flow.first
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
}

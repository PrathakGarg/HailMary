package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.OnboardingAnswers
import com.arise.habitquest.domain.model.OnboardingConfigCodec
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val generator: MissionGenerator,
    private val dataStore: OnboardingDataStore,
    private val seedAchievements: SeedAchievementsUseCase,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(answers: OnboardingAnswers) {
        val sessionDate = timeProvider.sessionDay()

        // Generate profile from questionnaire
        val profile = generator.generateInitialProfile(answers)

        // Compute and store template IDs for future daily generation
        val templateIds = generator.getTemplateIds(answers)
        val answersJson = OnboardingConfigCodec.encode(
            templateIds = templateIds,
            restDay = answers.restDay,
            startingDifficulty = answers.startingDifficulty,
            goals = answers.goals
        )

        val fullProfile = profile.copy(
            onboardingComplete = true,
            onboardingAnswersJson = answersJson
        )
        userRepository.upsertProfile(fullProfile)

        // Generate first set of daily missions
        val initialMissions = generator.generateInitialMissions(answers, sessionDate)
        missionRepository.insertMissions(initialMissions)

        // Generate first weekly boss
        val weekStart = sessionDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val boss = generator.generateWeeklyBoss(fullProfile, weekStart)
        missionRepository.insertMission(boss)

        // Seed achievement catalogue
        seedAchievements()

        // Mark onboarding complete in DataStore
        dataStore.setOnboardingComplete(true)
        dataStore.setHunterName(answers.hunterName)
        dataStore.setNotificationHour(answers.notificationHour)
        dataStore.setDayStartMinutes(TimeProvider.DEFAULT_DAY_START_MINUTES)
        dataStore.setLastDailyResetDate(sessionDate.toString())
    }
}

package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.*
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate

class GenerateDailyMissionsUseCaseSafetyThresholdTest {

    private val missionRepository = mockk<MissionRepository>()
    private val userRepository = mockk<UserRepository>()
    private val generator = mockk<MissionGenerator>()
    private val dataStore = mockk<OnboardingDataStore>(relaxed = true)

    private val useCase = GenerateDailyMissionsUseCase(
        missionRepository = missionRepository,
        userRepository = userRepository,
        generator = generator,
        dataStore = dataStore
    )

    @Test
    fun invoke_conservativePreference_triggersSafetyThrottleAtThreeFriction() {
        val date = LocalDate.of(2026, 4, 16)
        val profile = baseProfile(
            progressionPreference = ProgressionPreference.CONSERVATIVE
        )

        stubCommon(date, recentFriction = 3L)

        runBlocking { useCase.invoke(profile, date) }

        coVerify {
            generator.selectDailyTemplateIds(
                profile = profile,
                baseTemplateIds = any(),
                goalCategories = any(),
                date = date,
                recentTemplateIds = any(),
                weekToDateFamilyCounts = any(),
                weekToDateFamilyMissCounts = any(),
                safetyThrottle = true,
                focusThemeNames = any(),
                excludedTemplateIds = any()
            )
        }
    }

    @Test
    fun invoke_aggressivePreference_doesNotTriggerSafetyThrottleAtThreeFriction() {
        val date = LocalDate.of(2026, 4, 16)
        val profile = baseProfile(
            progressionPreference = ProgressionPreference.AGGRESSIVE
        )

        stubCommon(date, recentFriction = 3L)

        runBlocking { useCase.invoke(profile, date) }

        coVerify {
            generator.selectDailyTemplateIds(
                profile = profile,
                baseTemplateIds = any(),
                goalCategories = any(),
                date = date,
                recentTemplateIds = any(),
                weekToDateFamilyCounts = any(),
                weekToDateFamilyMissCounts = any(),
                safetyThrottle = false,
                focusThemeNames = any(),
                excludedTemplateIds = any()
            )
        }
    }

    private fun stubCommon(date: LocalDate, recentFriction: Long) {
        val focusThemesFlow = MutableStateFlow<Set<String>>(emptySet())
        val deprioritizedFlow = MutableStateFlow<Set<String>>(emptySet())
        val excludeInboxFlow = MutableStateFlow(false)

        coJustRun { dataStore.pruneMissionRollbackLedger() }
        coEvery { dataStore.focusThemes } returns focusThemesFlow
        coEvery { dataStore.deprioritizedTemplateIds } returns deprioritizedFlow
        coEvery { dataStore.excludeInboxMissions } returns excludeInboxFlow

        coEvery { missionRepository.countDailyMissionsForDate(date) } returns 0
        coEvery {
            missionRepository.getMissionsInRange(date.minusDays(3).toString(), date.minusDays(1).toString())
        } returns emptyList()
        coEvery {
            missionRepository.getMissionsInRange(date.with(java.time.DayOfWeek.MONDAY).toString(), date.minusDays(1).toString())
        } returns emptyList()
        coEvery {
            missionRepository.getMissionsInRange(date.minusDays(7).toString(), date.minusDays(1).toString())
        } returns frictionMissions(date, recentFriction)

        coEvery {
            generator.selectDailyTemplateIds(
                profile = any(),
                baseTemplateIds = any(),
                goalCategories = any(),
                date = any(),
                recentTemplateIds = any(),
                weekToDateFamilyCounts = any(),
                weekToDateFamilyMissCounts = any(),
                safetyThrottle = any(),
                focusThemeNames = any(),
                excludedTemplateIds = any()
            )
        } returns MissionGenerator.DailyTemplateSelection(
            anchorIds = listOf("push_ups"),
            rotatingIds = listOf("plank")
        )

        coEvery { userRepository.getShadowCompletions(any()) } returns emptyMap()
        coEvery {
            generator.generateDailyMissions(
                profile = any(),
                templateIds = any(),
                date = any(),
                shadowCompletions = any(),
                focusThemeNames = any(),
                excludedTemplateIds = any()
            )
        } returns emptyList()
        coJustRun { missionRepository.insertMissions(any()) }
    }

    private fun baseProfile(progressionPreference: ProgressionPreference): UserProfile {
        return UserProfile(
            hunterName = "tester",
            onboardingComplete = true,
            onboardingAnswersJson = buildAnswersJson(progressionPreference),
            progressionPreference = progressionPreference
        )
    }

    private fun buildAnswersJson(preference: ProgressionPreference): String {
        return OnboardingConfigCodec.encode(
            templateIds = listOf("push_ups", "plank"),
            restDay = java.time.DayOfWeek.SUNDAY,
            startingDifficulty = StartingDifficulty.RECOMMENDED,
            goals = setOf(Goal.FITNESS),
            fitnessLevel = FitnessLevel.MODERATE,
            sleepQuality = SleepQuality.OKAY,
            stressLevel = StressLevel.MEDIUM,
            workHoursPerDay = 8,
            availableTime = AvailableTime.EVENING,
            failureResponse = FailureResponse.NEED_TIME,
            accountabilityStyle = AccountabilityStyle.BALANCED,
            longestStreak = LongestStreak.NEVER,
            failureReasons = emptySet(),
            progressionPreference = preference,
            scheduleStyle = ScheduleStyle.FIXED_WINDOW,
            equipmentMode = EquipmentMode.BODYWEIGHT,
            trackFocus = MissionCategory.PHYSICAL,
            shoulderRiskFlag = false,
            heatRiskFlag = false
        )
    }

    private fun frictionMissions(date: LocalDate, friction: Long): List<Mission> {
        if (friction <= 0L) return emptyList()
        var remaining = friction.toInt()
        val missions = mutableListOf<Mission>()
        var idx = 0

        while (remaining >= 2) {
            missions += Mission(
                id = "f-$idx",
                title = "f",
                description = "f",
                systemLore = "f",
                type = MissionType.DAILY,
                category = MissionCategory.PHYSICAL,
                difficulty = Difficulty.E,
                xpReward = 10,
                penaltyXp = 5,
                penaltyHp = 5,
                isFailed = true,
                dueDate = date.minusDays(1)
            )
            idx++
            remaining -= 2
        }

        if (remaining == 1) {
            missions += Mission(
                id = "f-$idx",
                title = "f",
                description = "f",
                systemLore = "f",
                type = MissionType.DAILY,
                category = MissionCategory.PHYSICAL,
                difficulty = Difficulty.E,
                xpReward = 10,
                penaltyXp = 5,
                penaltyHp = 5,
                isCompleted = true,
                acceptedMiniVersion = true,
                dueDate = date.minusDays(1)
            )
        }

        return missions
    }
}

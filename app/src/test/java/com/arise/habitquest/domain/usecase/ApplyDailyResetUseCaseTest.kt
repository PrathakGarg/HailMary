package com.arise.habitquest.domain.usecase

import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.database.dao.DailyLogDao
import com.arise.habitquest.data.local.database.entity.DailyLogEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.*
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class ApplyDailyResetUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val missionRepository = mockk<MissionRepository>()
    private val dailyLogDao = mockk<DailyLogDao>()
    private val generateDailyMissions = mockk<GenerateDailyMissionsUseCase>()
    private val generator = mockk<MissionGenerator>()
    private val dataStore = mockk<OnboardingDataStore>(relaxed = true)
    private val timeProvider = mockk<TimeProvider>()

    private val useCase = ApplyDailyResetUseCase(
        userRepository = userRepository,
        missionRepository = missionRepository,
        dailyLogDao = dailyLogDao,
        generateDailyMissions = generateDailyMissions,
        generator = generator,
        dataStore = dataStore,
        timeProvider = timeProvider
    )

    @Test
    fun invoke_setsDeloadAndWellnessRecommendation_onLowWeeklyCompletion() {
        val sessionDate = LocalDate.of(2026, 4, 16)
        val expiringDate = sessionDate.minusDays(1)
        val profile = baseProfile(
            onboardingAnswersJson = buildAnswersJson(
                goals = setOf(Goal.FITNESS, Goal.MENTAL_HEALTH)
            ),
            progressionState = ProgressionState.PROGRESSING,
            adaptiveDifficulty = 1.0f,
            consecutiveMissDays = 0,
            pendingWarning = false,
            restDay = 6
        )

        coEvery { userRepository.getUserProfile() } returns profile
        coEvery { timeProvider.sessionDay() } returns sessionDate
        coJustRun { missionRepository.failActiveDailyMissionsForDate(any()) }
        coEvery { missionRepository.getMissionsForDate(expiringDate) } returns listOf(
            mission(id = "f1", date = expiringDate, completed = false, failed = true)
        )
        coEvery { generator.generateSystemMessage(any(), any(), any()) } returns "system-msg"
        coJustRun { dailyLogDao.upsertLog(any()) }
        coEvery { dailyLogDao.getRecentLogs(7) } returns List(7) { index ->
            DailyLogEntity(date = sessionDate.minusDays(index.toLong()).toString(), completionRate = 0.30f)
        }
        coJustRun { userRepository.updateMissState(any(), any()) }
        coJustRun { userRepository.updateXpAndLevel(any(), any(), any(), any()) }
        coJustRun { userRepository.updateHp(any()) }
        coJustRun { userRepository.updateStreak(any(), any()) }
        coJustRun { userRepository.updateAdaptiveDifficulty(any()) }
        coJustRun { userRepository.updateProgressionState(any(), any()) }
        coJustRun { userRepository.incrementDayCount() }
        coJustRun { generateDailyMissions(any(), any()) }
        coJustRun { missionRepository.pruneOldDailyMissions(any()) }
        coJustRun { dataStore.setLastDailyResetDate(any()) }

        runBlocking { useCase.invoke() }

        coVerify { userRepository.updateAdaptiveDifficulty(0.85f) }
        coVerify { userRepository.updateProgressionState("DELOAD", MissionCategory.WELLNESS) }
    }

    @Test
    fun invoke_setsReRampWithoutRecommendation_afterRecoveredWeek() {
        val sessionDate = LocalDate.of(2026, 4, 16)
        val expiringDate = sessionDate.minusDays(1)
        val profile = baseProfile(
            onboardingAnswersJson = buildAnswersJson(goals = setOf(Goal.FITNESS)),
            progressionState = ProgressionState.DELOAD,
            adaptiveDifficulty = 1.0f,
            consecutiveMissDays = 0,
            pendingWarning = false,
            restDay = 6
        )

        coEvery { userRepository.getUserProfile() } returns profile
        coEvery { timeProvider.sessionDay() } returns sessionDate
        coJustRun { missionRepository.failActiveDailyMissionsForDate(any()) }
        coEvery { missionRepository.getMissionsForDate(expiringDate) } returns listOf(
            mission(id = "c1", date = expiringDate, completed = true, failed = false)
        )
        coEvery { generator.generateSystemMessage(any(), any(), any()) } returns "system-msg"
        coJustRun { dailyLogDao.upsertLog(any()) }
        coEvery { dailyLogDao.getRecentLogs(7) } returns List(7) { index ->
            DailyLogEntity(date = sessionDate.minusDays(index.toLong()).toString(), completionRate = 0.90f)
        }
        coJustRun { userRepository.updateMissState(any(), any()) }
        coJustRun { userRepository.updateXpAndLevel(any(), any(), any(), any()) }
        coJustRun { userRepository.updateHp(any()) }
        coJustRun { userRepository.updateStreak(any(), any()) }
        coJustRun { userRepository.updateAdaptiveDifficulty(any()) }
        coJustRun { userRepository.updateProgressionState(any(), any()) }
        coJustRun { userRepository.incrementDayCount() }
        coJustRun { generateDailyMissions(any(), any()) }
        coJustRun { missionRepository.pruneOldDailyMissions(any()) }
        coJustRun { dataStore.setLastDailyResetDate(any()) }

        runBlocking { useCase.invoke() }

        coVerify { userRepository.updateAdaptiveDifficulty(1.1f) }
        coVerify { userRepository.updateProgressionState("RE_RAMP", null) }
    }

    private fun baseProfile(
        onboardingAnswersJson: String,
        progressionState: ProgressionState,
        adaptiveDifficulty: Float,
        consecutiveMissDays: Int,
        pendingWarning: Boolean,
        restDay: Int
    ): UserProfile {
        return UserProfile(
            hunterName = "tester",
            rank = Rank.E,
            level = 1,
            xp = 100L,
            xpToNextLevel = 200L,
            hp = 90,
            maxHp = 100,
            adaptiveDifficulty = adaptiveDifficulty,
            restDay = restDay,
            onboardingAnswersJson = onboardingAnswersJson,
            onboardingComplete = true,
            progressionState = progressionState,
            consecutiveMissDays = consecutiveMissDays,
            pendingWarning = pendingWarning
        )
    }

    private fun buildAnswersJson(goals: Set<Goal>): String {
        return OnboardingConfigCodec.encode(
            templateIds = listOf("push_ups"),
            restDay = java.time.DayOfWeek.SUNDAY,
            startingDifficulty = StartingDifficulty.RECOMMENDED,
            goals = goals,
            fitnessLevel = FitnessLevel.MODERATE,
            sleepQuality = SleepQuality.OKAY,
            stressLevel = StressLevel.MEDIUM,
            workHoursPerDay = 8,
            availableTime = AvailableTime.EVENING,
            failureResponse = FailureResponse.NEED_TIME,
            accountabilityStyle = AccountabilityStyle.BALANCED,
            longestStreak = LongestStreak.NEVER,
            failureReasons = emptySet(),
            progressionPreference = ProgressionPreference.ASSERTIVE_SAFE,
            scheduleStyle = ScheduleStyle.FIXED_WINDOW,
            equipmentMode = EquipmentMode.BODYWEIGHT,
            trackFocus = MissionCategory.PHYSICAL,
            shoulderRiskFlag = false,
            heatRiskFlag = false
        )
    }

    private fun mission(
        id: String,
        date: LocalDate,
        completed: Boolean,
        failed: Boolean
    ): Mission {
        return Mission(
            id = id,
            title = "M",
            description = "D",
            systemLore = "L",
            type = MissionType.DAILY,
            category = MissionCategory.PHYSICAL,
            difficulty = Difficulty.E,
            xpReward = 20,
            penaltyXp = 5,
            penaltyHp = 5,
            isCompleted = completed,
            isFailed = failed,
            dueDate = date
        )
    }
}

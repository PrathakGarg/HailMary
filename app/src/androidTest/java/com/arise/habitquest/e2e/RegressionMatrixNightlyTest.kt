package com.arise.habitquest.e2e

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.mapper.AchievementMapper
import com.arise.habitquest.data.mapper.MissionMapper
import com.arise.habitquest.data.mapper.UserProfileMapper
import com.arise.habitquest.data.repository.AchievementRepositoryImpl
import com.arise.habitquest.data.repository.MissionRepositoryImpl
import com.arise.habitquest.data.repository.UserRepositoryImpl
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.domain.usecase.ApplyDailyResetUseCase
import com.arise.habitquest.domain.usecase.CheckLevelUpUseCase
import com.arise.habitquest.domain.usecase.CompleteMissionUseCase
import com.arise.habitquest.domain.usecase.GenerateDailyMissionsUseCase
import com.arise.habitquest.domain.usecase.RegenerateCurrentMissionsUseCase
import com.arise.habitquest.domain.usecase.UnlockAchievementUseCase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class RegressionMatrixNightlyTest {

    private val resetRule = ResetAppStateRule()

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(resetRule)

    @Test
    fun streakInflation_completingMultipleMissionsSameSession_incrementsDayStreakOnce() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 14)
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)

        seedBaseProfile(context, sessionDate, restDay = 6, streakCurrent = 4, streakBest = 6)
        E2ETestHarness.insertMission(regressionMission("streak_a", sessionDate), context)
        E2ETestHarness.insertMission(regressionMission("streak_b", sessionDate), context)

        val deps = buildDeps(context)
        val complete = deps.completeMissionUseCase
        val missionRepo = deps.missionRepository
        val userRepo = deps.userRepository

        runBlocking {
            val profile1 = requireNotNull(userRepo.getUserProfile())
            val mission1 = requireNotNull(missionRepo.getMissionById("streak_a"))
            complete(mission1, profile1)

            val profile2 = requireNotNull(userRepo.getUserProfile())
            val mission2 = requireNotNull(missionRepo.getMissionById("streak_b"))
            complete(mission2, profile2)
        }

        val finalProfile = requireNotNull(E2ETestHarness.getUserProfileEntity(context))
        assertEquals(5, finalProfile.streakCurrent)
        assertEquals(6, finalProfile.streakBest)
    }

    @Test
    fun delayedLevelUpAfterAchievementXp_completionTriggersImmediateLevelUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        E2ETestHarness.seedProgressionScenario(missionId = "reg_level_xp", context = context)

        val deps = buildDeps(context)
        val complete = deps.completeMissionUseCase

        runBlocking {
            val mission = requireNotNull(deps.missionRepository.getMissionById("reg_level_xp"))
            val profile = requireNotNull(deps.userRepository.getUserProfile())
            val result = complete(mission, profile)
            assertNotNull(result.levelUpResult)
        }

        val finalProfile = requireNotNull(E2ETestHarness.getUserProfileEntity(context))
        assertEquals(2, finalProfile.level)
        assertEquals(135L, finalProfile.xp)
    }

    @Test
    fun restDaySessionDayMismatch_regenerationUsesSessionDayAndSkipsGeneration() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Before default 04:30 boundary => session day is previous calendar date.
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 3, 45), context = context)
        val sessionDate = E2ETestHarness.getSessionDay(context)
        val sessionRestDay = sessionDate.dayOfWeek.value - 1

        seedBaseProfile(context, sessionDate, restDay = sessionRestDay)
        E2ETestHarness.insertMission(regressionMission("rest_mismatch_old", sessionDate), context)

        val deps = buildDeps(context)
        val regenerate = deps.regenerateCurrentMissionsUseCase

        runBlocking {
            regenerate()
        }

        val missions = E2ETestHarness.getMissionsForDate(sessionDate, context)
        assertTrue(missions.isEmpty())
    }

    @Test
    fun missionRegenerationCorrectness_nonRestDayReplacesCurrentDailySet() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)
        val sessionDate = E2ETestHarness.getSessionDay(context)

        // Tuesday session with Friday rest day => non-rest session.
        seedBaseProfile(context, sessionDate, restDay = 4)
        E2ETestHarness.insertMission(regressionMission("regen_old", sessionDate), context)

        val deps = buildDeps(context)
        runBlocking { deps.regenerateCurrentMissionsUseCase() }

        val missions = E2ETestHarness.getMissionsForDate(sessionDate, context)
        assertTrue(missions.isNotEmpty())
        assertTrue(missions.none { it.id == "regen_old" })
    }

    @Test
    fun rolloverPunishments_partialCompliance_appliesLiveHpAndXpLoss() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 15, 9, 0), context = context)
        val sessionDate = E2ETestHarness.getSessionDay(context)
        val expiringDate = sessionDate.minusDays(1)

        seedBaseProfile(
            context = context,
            sessionDate = sessionDate,
            restDay = 4,
            xp = 120L,
            hp = 100
        )
        E2ETestHarness.insertMission(
            regressionMission(
                id = "rollover_done",
                dueDate = expiringDate,
                isCompleted = true
            ),
            context
        )
        E2ETestHarness.insertMission(
            regressionMission(
                id = "rollover_missed",
                dueDate = expiringDate,
                penaltyXp = 7,
                penaltyHp = 9
            ),
            context
        )

        val deps = buildDeps(context)
        runBlocking { deps.applyDailyResetUseCase() }

        val finalProfile = requireNotNull(E2ETestHarness.getUserProfileEntity(context))
        assertEquals(113L, finalProfile.xp)
        assertEquals(91, finalProfile.hp)

        val dailyLog = runBlocking { AppDatabase.getInstance(context).dailyLogDao().getLogForDate(expiringDate.toString()) }
        assertNotNull(dailyLog)
        assertEquals(7, dailyLog?.xpLost)
        assertEquals(9, dailyLog?.hpLost)
    }

    @Test
    fun missionVariety_consecutiveDays_doNotReuseSameDailySet() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val firstDate = LocalDate.of(2026, 4, 14)
        val secondDate = firstDate.plusDays(1)

        seedBaseProfile(context, firstDate, restDay = 4)

        val deps = buildDeps(context)
        runBlocking {
            val profile = requireNotNull(deps.userRepository.getUserProfile())
            deps.generateDailyMissionsUseCase(profile, firstDate)
            deps.generateDailyMissionsUseCase(profile, secondDate)
        }

        val firstTemplateSet = E2ETestHarness
            .getMissionsForDate(firstDate, context)
            .mapNotNull { it.parentTemplateId }
            .toSet()
        val secondTemplateSet = E2ETestHarness
            .getMissionsForDate(secondDate, context)
            .mapNotNull { it.parentTemplateId }
            .toSet()

        assertTrue(firstTemplateSet.isNotEmpty())
        assertTrue(secondTemplateSet.isNotEmpty())
        assertNotEquals(firstTemplateSet, secondTemplateSet)
    }

    private fun seedBaseProfile(
        context: Context,
        sessionDate: LocalDate,
        restDay: Int,
        xp: Long = 0L,
        hp: Int = 100,
        streakCurrent: Int = 2,
        streakBest: Int = 3
    ) {
        runBlocking {
            val dataStore = OnboardingDataStore(context)
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("Regression Tester")
            dataStore.setNotificationHour(8)
            dataStore.setLastDailyResetDate(sessionDate.toString())
            dataStore.setFocusThemes(emptySet())
            dataStore.setDayStartMinutes(TimeProvider.DEFAULT_DAY_START_MINUTES)
        }

        E2ETestHarness.upsertUserProfile(
            UserProfileEntity(
                hunterName = "Regression Tester",
                epithet = "Silent",
                title = "The Unawakened",
                rank = "E",
                level = 1,
                xp = xp,
                xpToNextLevel = 100L,
                hp = hp,
                maxHp = 100,
                streakCurrent = streakCurrent,
                streakBest = streakBest,
                daysSinceJoin = 9,
                onboardingComplete = true,
                onboardingAnswersJson = "{\"templateIds\":[\"tpl_push_ups\",\"tpl_deep_work\",\"tpl_meditation\"],\"goals\":[\"BUILD_DISCIPLINE\",\"GET_STRONGER\",\"REDUCE_STRESS\"]}",
                joinDate = sessionDate.minusDays(9).toString(),
                restDay = restDay
            ),
            context = context
        )
    }

    private fun regressionMission(
        id: String,
        dueDate: LocalDate,
        penaltyXp: Int = 5,
        penaltyHp: Int = 5,
        isCompleted: Boolean = false
    ) = MissionEntity(
        id = id,
        title = "Regression Mission $id",
        description = "Regression matrix mission.",
        systemLore = "[E2E] Regression matrix",
        miniMissionDescription = "Regression mini.",
        type = MissionType.DAILY.name,
        category = MissionCategory.PHYSICAL.name,
        difficulty = Difficulty.E.name,
        xpReward = 20,
        penaltyXp = penaltyXp,
        penaltyHp = penaltyHp,
        statRewardsJson = JSONObject(mapOf("STR" to 1)).toString(),
        isCompleted = isCompleted,
        completedAt = if (isCompleted) System.currentTimeMillis() else null,
        dueDate = dueDate.toString(),
        scheduledTimeHint = "MORNING",
        iconName = MissionCategory.PHYSICAL.iconName
    )

    private data class Deps(
        val missionRepository: MissionRepositoryImpl,
        val userRepository: UserRepository,
        val completeMissionUseCase: CompleteMissionUseCase,
        val generateDailyMissionsUseCase: GenerateDailyMissionsUseCase,
        val applyDailyResetUseCase: ApplyDailyResetUseCase,
        val regenerateCurrentMissionsUseCase: RegenerateCurrentMissionsUseCase
    )

    private fun buildDeps(context: Context): Deps {
        val db = AppDatabase.getInstance(context)
        val dataStore = OnboardingDataStore(context)
        val timeProvider = TimeProvider.getInstance(context)

        val missionRepository = MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider, context)
        val userRepository = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())
        val achievementRepository = AchievementRepositoryImpl(db.achievementDao(), AchievementMapper())
        val generator = MissionGenerator(timeProvider)

        val unlockAchievement = UnlockAchievementUseCase(
            achievementRepository = achievementRepository,
            userRepository = userRepository,
            missionRepository = missionRepository,
            timeProvider = timeProvider
        )
        val checkLevelUp = CheckLevelUpUseCase(userRepository, generator, dataStore)
        val completeMission = CompleteMissionUseCase(
            missionRepository = missionRepository,
            userRepository = userRepository,
            dataStore = dataStore,
            timeProvider = timeProvider,
            checkLevelUp = checkLevelUp,
            unlockAchievement = unlockAchievement
        )

        val generateDailyMissions = GenerateDailyMissionsUseCase(
            missionRepository = missionRepository,
            userRepository = userRepository,
            generator = generator,
            dataStore = dataStore
        )
        val applyDailyReset = ApplyDailyResetUseCase(
            userRepository = userRepository,
            missionRepository = missionRepository,
            dailyLogDao = db.dailyLogDao(),
            generateDailyMissions = generateDailyMissions,
            generator = generator,
            dataStore = dataStore,
            timeProvider = timeProvider
        )
        val regenerateCurrentMissions = RegenerateCurrentMissionsUseCase(
            userRepository = userRepository,
            missionRepository = missionRepository,
            generateDailyMissions = generateDailyMissions,
            timeProvider = timeProvider
        )

        return Deps(
            missionRepository = missionRepository,
            userRepository = userRepository,
            completeMissionUseCase = completeMission,
            generateDailyMissionsUseCase = generateDailyMissions,
            applyDailyResetUseCase = applyDailyReset,
            regenerateCurrentMissionsUseCase = regenerateCurrentMissions
        )
    }
}

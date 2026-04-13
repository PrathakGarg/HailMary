package com.arise.habitquest.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class DayBoundaryNightlyTest {

    private val resetRule = ResetAppStateRule()

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(resetRule)

    @Test
    fun sessionDay_beforeAtAndAfterBoundary_isComputedFromTrustedTime() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val boundaryDate = LocalDate.of(2026, 4, 12)

        E2ETestHarness.pinTrustedTime(
            localDateTime = LocalDateTime.of(2026, 4, 12, 4, 29),
            context = context
        )
        assertEquals(boundaryDate.minusDays(1), E2ETestHarness.getSessionDay(context))
        val beforeBoundaryMinutes = E2ETestHarness.getMinutesUntilReset(context)
        assertTrue(beforeBoundaryMinutes in 0L..1L)

        E2ETestHarness.pinTrustedTime(
            localDateTime = LocalDateTime.of(2026, 4, 12, 4, 30),
            context = context
        )
        assertEquals(boundaryDate, E2ETestHarness.getSessionDay(context))
        val atBoundaryMinutes = E2ETestHarness.getMinutesUntilReset(context)
        assertTrue(atBoundaryMinutes in 1439L..1440L)

        E2ETestHarness.pinTrustedTime(
            localDateTime = LocalDateTime.of(2026, 4, 12, 4, 31),
            context = context
        )
        assertEquals(boundaryDate, E2ETestHarness.getSessionDay(context))
        val afterBoundaryMinutes = E2ETestHarness.getMinutesUntilReset(context)
        assertTrue(afterBoundaryMinutes in 1438L..1439L)
    }

    @Test
    fun applyDailyReset_rolloverFailsExpiringMissions_logsHistory_andGeneratesFreshDailyMissions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 12)
        val expiringDate = sessionDate.minusDays(1)

        E2ETestHarness.pinTrustedTime(
            localDateTime = LocalDateTime.of(2026, 4, 12, 4, 31),
            context = context
        )
        seedBoundaryProfile(
            sessionDate = sessionDate,
            restDay = sessionDate.plusDays(1).dayOfWeek.value - 1,
            streakCurrent = 5,
            streakBest = 5,
            context = context
        )

        E2ETestHarness.insertMission(
            missionEntity(
                id = "boundary_done",
                title = "Completed Boundary Mission",
                dueDate = expiringDate,
                isCompleted = true
            ),
            context = context
        )
        E2ETestHarness.insertMission(
            missionEntity(
                id = "boundary_active",
                title = "Active Boundary Mission",
                dueDate = expiringDate
            ),
            context = context
        )

        E2ETestHarness.applyDailyReset(context)

        val expiringMissions = E2ETestHarness.getMissionsForDate(expiringDate, context)
        val activeMission = expiringMissions.first { it.id == "boundary_active" }
        assertTrue(activeMission.isFailed)

        val log = E2ETestHarness.getDailyLog(expiringDate, context)
        assertNotNull(log)
        assertEquals(expiringDate.toString(), log?.date)
        assertEquals(2, log?.totalMissions)
        assertTrue(log?.failedIdsJson.orEmpty().contains("boundary_active"))
        assertTrue(log?.completedIdsJson.orEmpty().contains("boundary_done"))

        val newSessionMissions = E2ETestHarness.getMissionsForDate(sessionDate, context)
        assertTrue(newSessionMissions.isNotEmpty())
        assertEquals(sessionDate.toString(), E2ETestHarness.getLastDailyResetDate(context))

        val profile = requireNotNull(E2ETestHarness.getUserProfileEntity(context))
        assertEquals(5, profile.streakCurrent)
        assertEquals(5, profile.streakBest)
    }

    @Test
    fun applyDailyReset_restDaySkipsMissionGeneration_andBreaksStreakAfterMissedNonRestDay() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 13)
        val expiringDate = sessionDate.minusDays(1)
        val sessionRestDay = sessionDate.dayOfWeek.value - 1

        E2ETestHarness.pinTrustedTime(
            localDateTime = LocalDateTime.of(2026, 4, 13, 4, 31),
            context = context
        )
        seedBoundaryProfile(
            sessionDate = sessionDate,
            restDay = sessionRestDay,
            streakCurrent = 4,
            streakBest = 6,
            hp = 50,
            context = context
        )

        E2ETestHarness.insertMission(
            missionEntity(
                id = "rest_day_expiring_active",
                title = "Missed Before Rest Day",
                dueDate = expiringDate
            ),
            context = context
        )

        E2ETestHarness.applyDailyReset(context)

        val newSessionMissions = E2ETestHarness.getMissionsForDate(sessionDate, context)
        assertTrue(newSessionMissions.isEmpty())

        val log = E2ETestHarness.getDailyLog(expiringDate, context)
        assertNotNull(log)
        assertEquals(1, log?.totalMissions)
        assertTrue(log?.failedIdsJson.orEmpty().contains("rest_day_expiring_active"))

        val profile = requireNotNull(E2ETestHarness.getUserProfileEntity(context))
        assertEquals(0, profile.streakCurrent)
        assertEquals(6, profile.streakBest)
        assertEquals(80, profile.hp)
    }

    private fun seedBoundaryProfile(
        sessionDate: LocalDate,
        restDay: Int,
        streakCurrent: Int,
        streakBest: Int,
        hp: Int = 100,
        context: android.content.Context
    ) {
        val dataStore = OnboardingDataStore(context)
        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
            dataStore.setNotificationHour(8)
            dataStore.setLastDailyResetDate(sessionDate.minusDays(1).toString())
            dataStore.setFocusThemes(emptySet())
            dataStore.setDayStartMinutes(TimeProvider.DEFAULT_DAY_START_MINUTES)
        }

        E2ETestHarness.upsertUserProfile(
            UserProfileEntity(
                hunterName = "E2E Hunter",
                epithet = "Silent Fierce Relentless",
                title = "The Unawakened",
                rank = "E",
                level = 1,
                xp = 40L,
                xpToNextLevel = 100L,
                hp = hp,
                maxHp = 100,
                streakCurrent = streakCurrent,
                streakBest = streakBest,
                daysSinceJoin = 7,
                onboardingComplete = true,
                onboardingAnswersJson = "{\"templateIds\":[\"tpl_push_ups\",\"tpl_deep_work\",\"tpl_meditation\"]}",
                joinDate = sessionDate.minusDays(7).toString(),
                restDay = restDay
            ),
            context = context
        )
    }

    private fun missionEntity(
        id: String,
        title: String,
        dueDate: LocalDate,
        isCompleted: Boolean = false
    ): MissionEntity = MissionEntity(
        id = id,
        title = title,
        description = "Boundary scenario mission.",
        systemLore = "[E2E] Boundary scenario.",
        miniMissionDescription = "Boundary mini mission.",
        type = MissionType.DAILY.name,
        category = MissionCategory.PHYSICAL.name,
        difficulty = Difficulty.E.name,
        xpReward = 20,
        penaltyXp = 5,
        penaltyHp = 5,
        statRewardsJson = JSONObject(mapOf("STR" to 1)).toString(),
        dueDate = dueDate.toString(),
        scheduledTimeHint = "MORNING",
        iconName = MissionCategory.PHYSICAL.iconName,
        isCompleted = isCompleted
    )
}

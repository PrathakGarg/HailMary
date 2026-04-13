package com.arise.habitquest.e2e

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.mapper.MissionMapper
import com.arise.habitquest.data.mapper.UserProfileMapper
import com.arise.habitquest.data.repository.MissionRepositoryImpl
import com.arise.habitquest.data.repository.UserRepositoryImpl
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.FocusTheme
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.usecase.GenerateDailyMissionsUseCase
import com.arise.habitquest.domain.usecase.RegenerateCurrentMissionsUseCase
import com.arise.habitquest.presentation.settings.SettingsViewModel
import com.arise.habitquest.worker.MorningNotificationWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class SettingsActionsNightlyTest {

    private val resetRule = ResetAppStateRule()

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(resetRule)

    @Test
    fun setRestDay_updatesProfileImmediately() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 14)
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)
        seedSettingsProfile(context, sessionDate, restDay = 3)

        val vm = buildSettingsViewModel(context)
        waitForProfileLoaded(vm)
        vm.setRestDay(1)

        waitUntil {
            E2ETestHarness.getUserProfileEntity(context)?.restDay == 1
        }
        assertEquals(1, E2ETestHarness.getUserProfileEntity(context)?.restDay)
    }

    @Test
    fun regenerateMissions_nonRestDay_replacesCurrentDailySet() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 14)
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)
        seedSettingsProfile(context, sessionDate, restDay = 3) // non-rest on Tuesday

        E2ETestHarness.insertMission(
            settingsMission("settings_old_daily", sessionDate, "Old Daily Mission"),
            context = context
        )

        val vm = buildSettingsViewModel(context)
        waitForProfileLoaded(vm)
        vm.regenerateMissions()

        waitUntil {
            val missions = E2ETestHarness.getMissionsForDate(sessionDate, context)
            missions.isNotEmpty() && missions.none { it.id == "settings_old_daily" }
        }

        val missions = E2ETestHarness.getMissionsForDate(sessionDate, context)
        assertTrue(missions.isNotEmpty())
        assertFalse(missions.any { it.id == "settings_old_daily" })
    }

    @Test
    fun regenerateMissions_restDay_deletesDailySet_andSkipsRegeneration() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 14) // Tuesday
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)
        seedSettingsProfile(context, sessionDate, restDay = 1) // rest day maps to Tuesday

        E2ETestHarness.insertMission(
            settingsMission("settings_rest_old", sessionDate, "Old Rest-Day Mission"),
            context = context
        )

        val vm = buildSettingsViewModel(context)
        waitForProfileLoaded(vm)
        vm.regenerateMissions()

        waitUntil {
            E2ETestHarness.getMissionsForDate(sessionDate, context).isEmpty()
        }

        assertTrue(E2ETestHarness.getMissionsForDate(sessionDate, context).isEmpty())
    }

    @Test
    fun setNotificationHour_updatesDatastoreProfile_andReschedulesMorningWorker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
        val workManager = WorkManager.getInstance(context)

        val sessionDate = LocalDate.of(2026, 4, 14)
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)
        seedSettingsProfile(context, sessionDate, restDay = 3)

        val vm = buildSettingsViewModel(context)
        waitForProfileLoaded(vm)
        vm.setNotificationHour(10)

        waitUntil {
            val dataHour = runBlocking { OnboardingDataStore(context).notificationHour.first() }
            val profileHour = E2ETestHarness.getUserProfileEntity(context)?.notificationHour
            dataHour == 10 && profileHour == 10
        }

        assertEquals(10, runBlocking { OnboardingDataStore(context).notificationHour.first() })
        assertEquals(10, E2ETestHarness.getUserProfileEntity(context)?.notificationHour)

        val workInfos = workManager.getWorkInfosForUniqueWork(MorningNotificationWorker.WORK_NAME).get()
        assertTrue(workInfos.isNotEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }

    @Test
    fun toggleFocusTheme_enforcesMinOneAndMaxThree() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 14)
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)
        seedSettingsProfile(context, sessionDate, restDay = 3)

        val vm = buildSettingsViewModel(context)
        waitForProfileLoaded(vm)

        // Start from default 2 themes, add 2 -> should cap at 3
        vm.toggleFocusTheme(FocusTheme.CAREER_PRODUCTIVITY)
        vm.toggleFocusTheme(FocusTheme.SOCIAL_CONNECTION)

        waitUntil {
            val themes = runBlocking { OnboardingDataStore(context).focusThemes.first() }
            themes.size in 1..3
        }

        val afterAdds = runBlocking { OnboardingDataStore(context).focusThemes.first() }
        assertTrue(afterAdds.size <= 3)

        // Remove themes repeatedly; should never go below 1
        vm.toggleFocusTheme(FocusTheme.PHYSICAL_PERFORMANCE)
        vm.toggleFocusTheme(FocusTheme.MENTAL_CLARITY)
        vm.toggleFocusTheme(FocusTheme.CAREER_PRODUCTIVITY)
        vm.toggleFocusTheme(FocusTheme.SOCIAL_CONNECTION)
        vm.toggleFocusTheme(FocusTheme.WELLNESS_RECOVERY)

        waitUntil {
            val themes = runBlocking { OnboardingDataStore(context).focusThemes.first() }
            themes.isNotEmpty()
        }
        val finalThemes = runBlocking { OnboardingDataStore(context).focusThemes.first() }
        assertTrue(finalThemes.size >= 1)
    }

    @Test
    fun regenerateMissions_withSocialFocusTheme_injectsSocialCategoryMission() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 14)
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)
        seedSettingsProfile(context, sessionDate, restDay = 3)

        runBlocking {
            OnboardingDataStore(context).setFocusThemes(setOf(FocusTheme.SOCIAL_CONNECTION.name))
        }

        val vm = buildSettingsViewModel(context)
        waitForProfileLoaded(vm)
        vm.regenerateMissions()

        waitUntil {
            E2ETestHarness.getMissionsForDate(sessionDate, context).isNotEmpty()
        }

        val missions = E2ETestHarness.getMissionsForDate(sessionDate, context)
        assertTrue(
            "Expected at least one SOCIAL mission after social focus-theme regeneration",
            missions.any { it.category == MissionCategory.SOCIAL.name }
        )
    }

    @Test
    fun activateEmergencyGrace_consumesUse_andClearsWarningState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionDate = LocalDate.of(2026, 4, 14)
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 14, 9, 0), context = context)

        seedSettingsProfile(
            context = context,
            sessionDate = sessionDate,
            restDay = 3,
            graceUsesRemaining = 2,
            pendingWarning = true,
            consecutiveMissDays = 3
        )

        val vm = buildSettingsViewModel(context)
        waitForProfileLoaded(vm)
        vm.activateEmergencyGrace()

        waitUntil {
            val profile = E2ETestHarness.getUserProfileEntity(context) ?: return@waitUntil false
            profile.graceUsesRemaining == 1 && !profile.pendingWarning && profile.consecutiveMissDays == 0
        }

        val profile = requireNotNull(E2ETestHarness.getUserProfileEntity(context))
        assertEquals(1, profile.graceUsesRemaining)
        assertFalse(profile.pendingWarning)
        assertEquals(0, profile.consecutiveMissDays)
    }

    private fun buildSettingsViewModel(context: Context): SettingsViewModel {
        val db = AppDatabase.getInstance(context)
        val dataStore = OnboardingDataStore(context)
        val timeProvider = TimeProvider.getInstance(context)
        val missionRepo = MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider)
        val userRepo = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())
        val generator = MissionGenerator(timeProvider)
        val generateDailyMissions = GenerateDailyMissionsUseCase(
            missionRepository = missionRepo,
            userRepository = userRepo,
            generator = generator,
            dataStore = dataStore
        )
        val regenerateUseCase = RegenerateCurrentMissionsUseCase(
            userRepository = userRepo,
            missionRepository = missionRepo,
            generateDailyMissions = generateDailyMissions,
            timeProvider = timeProvider
        )

        return SettingsViewModel(
            context = context,
            userRepository = userRepo,
            dataStore = dataStore,
            timeProvider = timeProvider,
            regenerateCurrentMissions = regenerateUseCase
        )
    }

    private fun seedSettingsProfile(
        context: Context,
        sessionDate: LocalDate,
        restDay: Int,
        graceUsesRemaining: Int = 3,
        pendingWarning: Boolean = false,
        consecutiveMissDays: Int = 0
    ) {
        runBlocking {
            val dataStore = OnboardingDataStore(context)
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("Settings Tester")
            dataStore.setLastDailyResetDate(sessionDate.toString())
            dataStore.setNotificationHour(8)
            dataStore.setFocusThemes(emptySet())
        }

        E2ETestHarness.upsertUserProfile(
            UserProfileEntity(
                hunterName = "Settings Tester",
                epithet = "Silent",
                title = "The Unawakened",
                rank = "E",
                level = 3,
                xp = 120L,
                xpToNextLevel = 300L,
                hp = 100,
                maxHp = 100,
                streakCurrent = 2,
                streakBest = 5,
                daysSinceJoin = 10,
                onboardingComplete = true,
                onboardingAnswersJson = "{\"templateIds\":[\"tpl_push_ups\",\"tpl_deep_work\",\"tpl_meditation\"]}",
                joinDate = sessionDate.minusDays(10).toString(),
                restDay = restDay,
                notificationHour = 8,
                graceUsesRemaining = graceUsesRemaining,
                pendingWarning = pendingWarning,
                consecutiveMissDays = consecutiveMissDays
            ),
            context = context
        )
    }

    private fun settingsMission(id: String, dueDate: LocalDate, title: String) = MissionEntity(
        id = id,
        title = title,
        description = "Settings scenario mission.",
        systemLore = "[E2E] Settings scenario.",
        miniMissionDescription = "Settings mini.",
        type = MissionType.DAILY.name,
        category = MissionCategory.PHYSICAL.name,
        difficulty = Difficulty.E.name,
        xpReward = 20,
        penaltyXp = 5,
        penaltyHp = 5,
        statRewardsJson = JSONObject(mapOf("STR" to 1)).toString(),
        dueDate = dueDate.toString(),
        scheduledTimeHint = "MORNING",
        iconName = MissionCategory.PHYSICAL.iconName
    )

    private fun waitUntil(timeoutMs: Long = 2_500, block: () -> Boolean) {
        runBlocking {
            val deadline = System.currentTimeMillis() + timeoutMs
            while (System.currentTimeMillis() < deadline) {
                if (block()) return@runBlocking
                delay(50)
            }
            check(block()) { "Condition not met within ${timeoutMs}ms" }
        }
    }

    private fun waitForProfileLoaded(vm: SettingsViewModel) {
        waitUntil(timeoutMs = 4_000) {
            vm.uiState.value.profile != null
        }
    }
}

package com.arise.habitquest.e2e

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
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
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.usecase.ApplyDailyResetUseCase
import com.arise.habitquest.domain.usecase.GenerateDailyMissionsUseCase
import com.arise.habitquest.worker.BootReceiver
import com.arise.habitquest.worker.DailyResetWorker
import com.arise.habitquest.worker.EveningReminderWorker
import com.arise.habitquest.worker.MidDayCheckWorker
import com.arise.habitquest.worker.MonthlyReportWorker
import com.arise.habitquest.worker.MorningNotificationWorker
import com.arise.habitquest.worker.PreResetReminderWorker
import com.arise.habitquest.worker.WindDownWorker
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
import java.time.LocalTime

/**
 * Worker-level boundary tests living in the nightly bucket.
 *
 * Covers:
 *  - Worker schedule() helpers enqueue named periodic work in WorkManager.
 *  - BootReceiver re-registers all seven named workers on BOOT_COMPLETED.
 *  - DailyResetWorker.doWork() runs the full reset pipeline and returns success.
 */
@RunWith(AndroidJUnit4::class)
class WorkerNightlyTest {

    private val resetRule = ResetAppStateRule()

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(resetRule)

    // ── 1. DailyResetWorker.schedule() ───────────────────────────────────────

    @Test
    fun dailyResetWorker_schedule_enqueuesNamedPeriodicWork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
        val workManager = WorkManager.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)

        DailyResetWorker.schedule(workManager, timeProvider)

        val workInfos = workManager.getWorkInfosForUniqueWork(DailyResetWorker.WORK_NAME).get()
        assertFalse("DailyResetWorker should be enqueued after schedule()", workInfos.isEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, workInfos[0].state)
    }

    // ── 2. PreResetReminderWorker.schedule() ─────────────────────────────────

    @Test
    fun preResetReminderWorker_schedule_enqueuesNamedPeriodicWork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
        val workManager = WorkManager.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)

        PreResetReminderWorker.schedule(workManager, timeProvider)

        val workInfos = workManager.getWorkInfosForUniqueWork(PreResetReminderWorker.WORK_NAME).get()
        assertFalse("PreResetReminderWorker should be enqueued after schedule()", workInfos.isEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, workInfos[0].state)
    }

    // ── 3. BootReceiver ───────────────────────────────────────────────────────

    @Test
    fun bootReceiver_onBootCompleted_schedulesAllCoreWorkers() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
        val workManager = WorkManager.getInstance(context)

        val receiver = BootReceiver()
        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        val expectedNames = listOf(
            DailyResetWorker.WORK_NAME,
            PreResetReminderWorker.WORK_NAME,
            MorningNotificationWorker.WORK_NAME,
            MidDayCheckWorker.WORK_NAME,
            EveningReminderWorker.WORK_NAME,
            WindDownWorker.WORK_NAME,
            MonthlyReportWorker.WORK_NAME
        )
        for (name in expectedNames) {
            val infos = workManager.getWorkInfosForUniqueWork(name).get()
            assertFalse("Worker '$name' should be enqueued after BOOT_COMPLETED", infos.isEmpty())
        }
    }

    // ── 4. DailyResetWorker.doWork() ─────────────────────────────────────────

    @Test
    fun dailyResetWorker_doWork_withExpiringMissions_failsThemAndGeneratesNew() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        // Pin time so session day is deterministic regardless of NTP outcome.
        // 2026-04-12 is the actual current date, so NTP would give the same result.
        E2ETestHarness.pinTrustedTime(
            localDateTime = LocalDateTime.of(2026, 4, 12, 5, 0),
            context = context
        )
        val pinnedSessionDate = LocalDate.of(2026, 4, 12)
        val realNowDate = LocalDate.now()
        val resetTime = LocalTime.of(TimeProvider.getInstance(context).resetHour, TimeProvider.getInstance(context).resetMinute)
        val realSessionDate = if (LocalTime.now() < resetTime) realNowDate.minusDays(1) else realNowDate
        val candidateExpiringDates = setOf(
            pinnedSessionDate.minusDays(1),
            realSessionDate.minusDays(1)
        )

        val db = AppDatabase.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)
        val dataStore = OnboardingDataStore(context)

        // Build the production dependency chain (identical to E2ETestHarness.applyDailyReset)
        val missionRepo = MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider)
        val userRepo = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())
        val generator = MissionGenerator(timeProvider)
        val generateDailyMissions = GenerateDailyMissionsUseCase(missionRepo, userRepo, generator, dataStore)
        val applyDailyResetUseCase = ApplyDailyResetUseCase(
            userRepository = userRepo,
            missionRepository = missionRepo,
            dailyLogDao = db.dailyLogDao(),
            generateDailyMissions = generateDailyMissions,
            generator = generator,
            dataStore = dataStore,
            timeProvider = timeProvider
        )

        // Seed: a returning user with one active mission expiring today
        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("Worker Tester")
            dataStore.setLastDailyResetDate(candidateExpiringDates.first().toString())
        }
        E2ETestHarness.upsertUserProfile(
            UserProfileEntity(
                hunterName = "Worker Tester",
                epithet = "Silent",
                title = "The Unawakened",
                rank = "E",
                level = 1,
                xp = 0L,
                xpToNextLevel = 100L,
                hp = 100,
                maxHp = 100,
                onboardingComplete = true,
                onboardingAnswersJson = "{\"templateIds\":[\"tpl_push_ups\",\"tpl_deep_work\",\"tpl_meditation\"]}",
                joinDate = pinnedSessionDate.minusDays(2).toString()
            ),
            context = context
        )
        candidateExpiringDates.forEachIndexed { index, dueDate ->
            E2ETestHarness.insertMission(workerMission("worker_expiring_$index", dueDate), context = context)
        }

        // Build and run the worker via a custom factory that bypasses Hilt injection
        val worker = TestListenableWorkerBuilder<DailyResetWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == DailyResetWorker::class.java.name) {
                    DailyResetWorker(appContext, workerParameters, applyDailyResetUseCase, missionRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }

        assertEquals("Worker should return success", androidx.work.ListenableWorker.Result.success(), result)

        // Expiring mission must now be failed
        val expiringMissions = candidateExpiringDates.flatMap { date ->
            E2ETestHarness.getMissionsForDate(date, context)
        }
        assertTrue(
            "At least one candidate expiring mission must be failed after doWork()",
            expiringMissions.any { it.id.startsWith("worker_expiring_") && it.isFailed }
        )

        // New missions must be generated for the fresh session day
        val sessionDateAfterWork = TimeProvider.getInstance(context).sessionDay()
        val freshMissions = E2ETestHarness.getMissionsForDate(sessionDateAfterWork, context)
        val profileAfterWork = requireNotNull(E2ETestHarness.getUserProfileEntity(context))
        val isRestDay = sessionDateAfterWork.dayOfWeek.value - 1 == profileAfterWork.restDay
        if (isRestDay) {
            assertTrue(
                "No new daily missions should be generated on rest day",
                freshMissions.isEmpty()
            )
        } else {
            assertTrue(
                "New missions should be generated for non-rest session day",
                freshMissions.isNotEmpty()
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun workerMission(id: String, dueDate: LocalDate) = MissionEntity(
        id = id,
        title = "Worker Test Mission",
        description = "Created for WorkerNightlyTest.",
        systemLore = "[E2E] Worker scheduling test.",
        miniMissionDescription = "Worker mini.",
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
}

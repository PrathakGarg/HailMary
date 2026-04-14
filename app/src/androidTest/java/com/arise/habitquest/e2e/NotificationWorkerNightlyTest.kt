package com.arise.habitquest.e2e

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
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
import com.arise.habitquest.worker.EveningReminderWorker
import com.arise.habitquest.worker.MidDayCheckWorker
import com.arise.habitquest.worker.MorningNotificationWorker
import com.arise.habitquest.worker.MonthlyReportWorker
import com.arise.habitquest.worker.SleepReminderWorker
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

/**
 * Notification-worker doWork() integration tests — nightly bucket.
 *
 * Strategy: seed DB state that triggers each worker's notification path, then
 * call doWork() via [TestListenableWorkerBuilder] with a custom [WorkerFactory]
 * that injects production dependencies directly (no Hilt).
 *
 * Each test asserts [ListenableWorker.Result.success] — the side effect
 * (notification posted to OS) is not assert-able without a custom
 * notification listener, so the tests act as non-crash + state-correctness
 * contract tests.  The one exception is [MonthlyReportWorker] which writes to
 * DataStore so its idempotency can be verified directly.
 */
@RunWith(AndroidJUnit4::class)
class NotificationWorkerNightlyTest {

    private val resetRule = ResetAppStateRule()

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(resetRule)

    // ── 1. MorningNotificationWorker: active missions present ────────────────

    @Test
    fun morningNotificationWorker_doWork_withActiveMissions_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 8, 0), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        val (missionRepo, userRepo) = seedNotificationScenario(
            context = context,
            sessionDate = sessionDate,
            missionId = "morning_active_1",
            missionTitle = "Morning Gate",
            timeHint = "MORNING"
        )
        val timeProvider = TimeProvider.getInstance(context)

        val worker = TestListenableWorkerBuilder<MorningNotificationWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == MorningNotificationWorker::class.java.name) {
                    MorningNotificationWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 2. MorningNotificationWorker: no active missions → silent success ────

    @Test
    fun morningNotificationWorker_doWork_withNoActiveMissions_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 8, 0), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        // Seed profile, but no missions
        val db = AppDatabase.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)
        val missionRepo = MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider, context)
        val userRepo = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())
        seedProfile(context, sessionDate)

        val worker = TestListenableWorkerBuilder<MorningNotificationWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == MorningNotificationWorker::class.java.name) {
                    MorningNotificationWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 3. MidDayCheckWorker: stale morning missions ─────────────────────────

    @Test
    fun midDayCheckWorker_doWork_withStaleMorningMissions_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 13, 0), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        val (missionRepo, userRepo) = seedNotificationScenario(
            context = context,
            sessionDate = sessionDate,
            missionId = "midday_morning_1",
            missionTitle = "Push-Up Gate",
            timeHint = "MORNING"
        )
        val timeProvider = TimeProvider.getInstance(context)

        val worker = TestListenableWorkerBuilder<MidDayCheckWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == MidDayCheckWorker::class.java.name) {
                    MidDayCheckWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 4. MidDayCheckWorker: no stale morning missions → silent success ─────

    @Test
    fun midDayCheckWorker_doWork_withNoStaleMorningMissions_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 13, 0), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        // Seed an EVENING-hinted mission only — MidDayCheckWorker should stay silent
        val (missionRepo, userRepo) = seedNotificationScenario(
            context = context,
            sessionDate = sessionDate,
            missionId = "midday_evening_only",
            missionTitle = "Evening Gate",
            timeHint = "EVENING"
        )
        val timeProvider = TimeProvider.getInstance(context)

        val worker = TestListenableWorkerBuilder<MidDayCheckWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == MidDayCheckWorker::class.java.name) {
                    MidDayCheckWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 5. EveningReminderWorker: active missions ────────────────────────────

    @Test
    fun eveningReminderWorker_doWork_withActiveMissions_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 20, 0), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        val (missionRepo, userRepo) = seedNotificationScenario(
            context = context,
            sessionDate = sessionDate,
            missionId = "evening_active_1",
            missionTitle = "Evening Gate",
            timeHint = "EVENING"
        )
        val timeProvider = TimeProvider.getInstance(context)

        val worker = TestListenableWorkerBuilder<EveningReminderWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == EveningReminderWorker::class.java.name) {
                    EveningReminderWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 6. WindDownWorker: active wellness/evening missions ──────────────────

    @Test
    fun windDownWorker_doWork_withActiveWellnessMission_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 21, 15), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        val (missionRepo, userRepo) = seedNotificationScenario(
            context = context,
            sessionDate = sessionDate,
            missionId = "winddown_wellness",
            missionTitle = "Evening Journal Gate",
            category = MissionCategory.WELLNESS,
            timeHint = "EVENING"
        )
        val timeProvider = TimeProvider.getInstance(context)

        val worker = TestListenableWorkerBuilder<WindDownWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == WindDownWorker::class.java.name) {
                    WindDownWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 7. SleepReminderWorker: active sleep mission present ─────────────────

    @Test
    fun sleepReminderWorker_doWork_withActiveSleepMission_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 0, 15), context = context)
        val sessionDate = LocalDate.of(2026, 4, 11) // session day is prior to 4:30 boundary

        val (missionRepo, userRepo) = seedNotificationScenario(
            context = context,
            sessionDate = sessionDate,
            missionId = "sleep_reminder_1",
            missionTitle = "Recovery Gate: Sleep by 1:00",
            timeHint = "EVENING"
        )
        val timeProvider = TimeProvider.getInstance(context)

        val worker = TestListenableWorkerBuilder<SleepReminderWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == SleepReminderWorker::class.java.name) {
                    SleepReminderWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 8. SleepReminderWorker: sleep mission already completed → silent ──────

    @Test
    fun sleepReminderWorker_doWork_withCompletedSleepMission_returnsSuccess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 0, 15), context = context)
        val sessionDate = LocalDate.of(2026, 4, 11)

        val (missionRepo, userRepo) = seedNotificationScenario(
            context = context,
            sessionDate = sessionDate,
            missionId = "sleep_completed_1",
            missionTitle = "Recovery Gate: Sleep by 1:00",
            isCompleted = true,
            timeHint = "EVENING"
        )
        val timeProvider = TimeProvider.getInstance(context)

        val worker = TestListenableWorkerBuilder<SleepReminderWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == SleepReminderWorker::class.java.name) {
                    SleepReminderWorker(appContext, workerParameters, missionRepo, userRepo, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    // ── 9. MonthlyReportWorker: first run of month writes lastMonthlyReport ──

    @Test
    fun monthlyReportWorker_doWork_firstRunOfMonth_sendsReportAndRecordsKey() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        // Pin to a new month that hasn't been reported yet
        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 6, 0), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        val db = AppDatabase.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)
        val dataStore = OnboardingDataStore(context)
        val missionRepo = MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider, context)
        val userRepo = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())

        seedProfile(context, sessionDate)

        // Seed some completed missions in the prior 30-day window
        val completedDate = sessionDate.minusDays(5)
        E2ETestHarness.insertMission(
            notifMission("monthly_done_1", completedDate, "Monthly Check Gate", isCompleted = true),
            context = context
        )
        E2ETestHarness.insertMission(
            notifMission("monthly_done_2", completedDate, "Monthly Check Gate 2", isCompleted = true),
            context = context
        )

        val worker = TestListenableWorkerBuilder<MonthlyReportWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == MonthlyReportWorker::class.java.name) {
                    MonthlyReportWorker(appContext, workerParameters, userRepo, missionRepo, dataStore, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)

        val lastReportKey = E2ETestHarness.getLastMonthlyReport(context)
        assertEquals("2026-04", lastReportKey)
    }

    // ── 10. MonthlyReportWorker: second run same month → idempotent no-op ────

    @Test
    fun monthlyReportWorker_doWork_secondRunSameMonth_isIdempotentNoOp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())

        E2ETestHarness.pinTrustedTime(LocalDateTime.of(2026, 4, 12, 6, 0), context = context)
        val sessionDate = LocalDate.of(2026, 4, 12)

        val db = AppDatabase.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)
        val dataStore = OnboardingDataStore(context)
        val missionRepo = MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider, context)
        val userRepo = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())

        // Pre-set the lastMonthlyReport to indicate the report was already sent
        runBlocking { dataStore.setLastMonthlyReport("2026-04") }
        seedProfile(context, sessionDate)

        val worker = TestListenableWorkerBuilder<MonthlyReportWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = if (workerClassName == MonthlyReportWorker::class.java.name) {
                    MonthlyReportWorker(appContext, workerParameters, userRepo, missionRepo, dataStore, timeProvider)
                } else null
            })
            .build()

        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.success(), result)
        // Key should remain unchanged — worker exited early
        assertEquals("2026-04", E2ETestHarness.getLastMonthlyReport(context))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Seeds a minimal returning-user profile and a single mission, returns
     * the constructed [MissionRepositoryImpl] and [UserRepositoryImpl] ready
     * to be injected into workers.
     */
    private fun seedNotificationScenario(
        context: Context,
        sessionDate: LocalDate,
        missionId: String,
        missionTitle: String,
        timeHint: String = "MORNING",
        category: MissionCategory = MissionCategory.PHYSICAL,
        isCompleted: Boolean = false
    ): Pair<MissionRepositoryImpl, UserRepositoryImpl> {
        seedProfile(context, sessionDate)
        E2ETestHarness.insertMission(
            notifMission(missionId, sessionDate, missionTitle, timeHint, category, isCompleted),
            context = context
        )
        val db = AppDatabase.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)
        return MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider, context) to
                UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())
    }

    private fun seedProfile(context: Context, sessionDate: LocalDate) {
        runBlocking {
            OnboardingDataStore(context).apply {
                setOnboardingComplete(true)
                setHunterName("Notif Tester")
                setLastDailyResetDate(sessionDate.toString())
            }
        }
        E2ETestHarness.upsertUserProfile(
            UserProfileEntity(
                hunterName = "Notif Tester",
                epithet = "Silent",
                title = "The Unawakened",
                rank = "E",
                level = 5,
                xp = 200L,
                xpToNextLevel = 500L,
                hp = 100,
                maxHp = 100,
                streakCurrent = 3,
                streakBest = 7,
                daysSinceJoin = 15,
                onboardingComplete = true,
                joinDate = sessionDate.minusDays(15).toString()
            ),
            context = context
        )
    }

    private fun notifMission(
        id: String,
        dueDate: LocalDate,
        title: String,
        timeHint: String = "MORNING",
        category: MissionCategory = MissionCategory.PHYSICAL,
        isCompleted: Boolean = false
    ) = MissionEntity(
        id = id,
        title = title,
        description = "Notification scenario mission.",
        systemLore = "[E2E] Notification test.",
        miniMissionDescription = "Short pass.",
        type = MissionType.DAILY.name,
        category = category.name,
        difficulty = Difficulty.E.name,
        xpReward = 20,
        penaltyXp = 5,
        penaltyHp = 5,
        statRewardsJson = JSONObject(mapOf("STR" to 1)).toString(),
        dueDate = dueDate.toString(),
        scheduledTimeHint = timeHint,
        iconName = category.iconName,
        isCompleted = isCompleted
    )
}

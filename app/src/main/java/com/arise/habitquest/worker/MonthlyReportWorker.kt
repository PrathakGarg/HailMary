package com.arise.habitquest.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@HiltWorker
class MonthlyReportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val dataStore: OnboardingDataStore,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            timeProvider.sync()
            val profile = userRepository.observeUserProfile().first() ?: return Result.success()
            val sessionDate = timeProvider.sessionDay()

            // Check if we already ran this month
            val lastReport = dataStore.lastMonthlyReport.first()
            val thisMonthKey = sessionDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            if (lastReport == thisMonthKey) return Result.success()

            // Gather last 30 days of missions
            val from = sessionDate.minusDays(30).toString()
            val to = sessionDate.toString()
            val recentMissions = missionRepository.getMissionsInRange(from, to)
            val completed = recentMissions.count { it.isCompleted }
            val total = recentMissions.size
            val rate = if (total > 0) (completed * 100 / total) else 0

            // Build report message
            val title = "Monthly System Report — ${sessionDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}"
            val reportBody = buildReportBody(
                hunterName = profile.hunterName,
                rank = profile.rank.displayName,
                level = profile.level,
                streak = profile.streakCurrent,
                bestStreak = profile.streakBest,
                completed = completed,
                total = total,
                rate = rate
            )

            sendNotification(title, reportBody)
            dataStore.setLastMonthlyReport(thisMonthKey)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun buildReportBody(
        hunterName: String,
        rank: String,
        level: Int,
        streak: Int,
        bestStreak: Int,
        completed: Int,
        total: Int,
        rate: Int
    ): String {
        val assessment = when {
            rate >= 90 -> "Exceptional. The System recognises dominance."
            rate >= 75 -> "Strong output. Hunter, you are rising."
            rate >= 60 -> "Adequate. The gates are being held open."
            rate >= 40 -> "Warning: performance declining. Rally, Hunter."
            else -> "Critical. You are losing ground. The System demands action."
        }
        return "$hunterName · Rank $rank · Level $level\n" +
               "Gates cleared: $completed/$total ($rate%)\n" +
               "Current streak: $streak days | Best: $bestStreak days\n" +
               assessment
    }

    private fun sendNotification(title: String, body: String) {
        NotificationHelper.send(
            context = applicationContext,
            channelId = "arise_monthly_report",
            channelName = "Monthly System Report",
            channelDescription = "Monthly performance recap from the System.",
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            notificationId = NOTIFICATION_ID,
            title = title,
            body = body
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 4003
        const val WORK_NAME = "arise_monthly_report"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<MonthlyReportWorker>(30, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}

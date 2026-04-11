package com.arise.habitquest.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.arise.habitquest.MainActivity
import com.arise.habitquest.R
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
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
            val today = timeProvider.today()

            // Check if we already ran this month
            val lastReport = dataStore.lastMonthlyReport.first()
            val thisMonthKey = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            if (lastReport == thisMonthKey) return Result.success()

            // Gather last 30 days of missions
            val from = today.minusDays(30).toString()
            val to = today.toString()
            val recentMissions = missionRepository.getMissionsInRange(from, to)
            val completed = recentMissions.count { it.isCompleted }
            val total = recentMissions.size
            val rate = if (total > 0) (completed * 100 / total) else 0

            // Build report message
            val title = "Monthly System Report — ${today.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}"
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
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "arise_monthly_report"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monthly System Report",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Monthly performance recap from the System." }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_arise_logo)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
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

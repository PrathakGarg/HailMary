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
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Fires 1 hour before the day-reset boundary.
 * If the hunter still has incomplete daily missions it sends a high-priority push.
 */
@HiltWorker
class PreResetReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        timeProvider.sync()
        val profile = userRepository.getUserProfile() ?: return Result.success()
        val sessionDay = timeProvider.sessionDay()
        val missions = missionRepository.getMissionsForDate(sessionDay)
        val incomplete = missions.count { it.isActive && it.type == MissionType.DAILY }

        if (incomplete > 0) {
            sendNotification(profile.hunterName, incomplete)
        }
        return Result.success()
    }

    private fun sendNotification(hunterName: String, count: Int) {
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "arise_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Mission Reminders", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val intent = Intent(appContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val gate = if (count == 1) "Gate" else "Gates"
        val body = "The day ends in 1 hour, $hunterName. $count $gate remain${if (count == 1) "s" else ""} unclosed. " +
                "The System will not wait."

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("FINAL WARNING — DAY ENDS SOON")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1004, notification)
    }

    companion object {
        const val WORK_NAME = "arise_pre_reset_reminder"

        /**
         * Schedules the reminder to fire 60 minutes before [resetHour]:[resetMinute].
         */
        fun schedule(workManager: WorkManager, timeProvider: TimeProvider) {
            val now = timeProvider.trustedNow().toLocalDateTime()
            var target = timeProvider.nextDayStartDateTime().minusHours(1)
            if (!target.isAfter(now)) target = target.plusDays(1)
            val delayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(0L)

            val request = PeriodicWorkRequestBuilder<PreResetReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun schedule(workManager: WorkManager, resetHour: Int, resetMinute: Int = 30) {
            val targetTime = java.time.LocalTime.of(resetHour, resetMinute).minusMinutes(60)
            val now = java.time.LocalTime.now()
            var delayMinutes = now.until(targetTime, java.time.temporal.ChronoUnit.MINUTES)
            if (delayMinutes <= 0) delayMinutes += 24 * 60

            val request = PeriodicWorkRequestBuilder<PreResetReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}

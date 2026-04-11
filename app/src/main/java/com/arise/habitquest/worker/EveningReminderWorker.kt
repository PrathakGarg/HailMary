package com.arise.habitquest.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class EveningReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        timeProvider.sync()
        val activeMissions = missionRepository.getMissionsForDate(timeProvider.sessionDay())
            .filter { it.isActive }

        if (activeMissions.isNotEmpty()) {
            val profile = userRepository.getUserProfile()
            val hunterName = profile?.hunterName ?: "Hunter"
            sendNotification(
                title = "[ SYSTEM WARNING ]",
                body = "The System grows impatient, $hunterName. ${activeMissions.size} gates remain unsealed. " +
                        "Complete them before midnight or face consequences.",
                channelId = "arise_reminders"
            )
        }
        return Result.success()
    }

    private fun sendNotification(title: String, body: String, channelId: String) {
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Mission Reminders", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val notification = androidx.core.app.NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        manager.notify(1002, notification)
    }

    companion object {
        const val WORK_NAME = "arise_evening_reminder"

        fun schedule(workManager: WorkManager) {
            val now = LocalTime.now()
            val target = LocalTime.of(20, 0)
            var delayMinutes = now.until(target, java.time.temporal.ChronoUnit.MINUTES)
            if (delayMinutes <= 0) delayMinutes += 24 * 60

            val request = PeriodicWorkRequestBuilder<EveningReminderWorker>(1, TimeUnit.DAYS)
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

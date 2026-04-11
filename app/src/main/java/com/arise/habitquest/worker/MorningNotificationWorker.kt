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
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class MorningNotificationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        timeProvider.sync()
        val profile = userRepository.getUserProfile() ?: return Result.success()
        val todayMissions = missionRepository.getMissionsForDate(timeProvider.sessionDay())
        val activeMissions = todayMissions.count { it.isActive }

        if (activeMissions > 0) {
            sendNotification(
                title = "SYSTEM ALERT",
                body = "Your Gates await, ${profile.hunterName}. $activeMissions missions today. ARISE.",
                channelId = "arise_reminders"
            )
        }
        return Result.success()
    }

    private fun sendNotification(title: String, body: String, channelId: String) {
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Mission Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val intent = Intent(appContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }

    companion object {
        const val WORK_NAME = "arise_morning_notification"

        fun schedule(workManager: WorkManager, hourOfDay: Int) {
            val now = LocalTime.now()
            val targetTime = LocalTime.of(hourOfDay, 0)
            var delayMinutes = now.until(targetTime, java.time.temporal.ChronoUnit.MINUTES)
            if (delayMinutes <= 0) delayMinutes += 24 * 60

            val request = PeriodicWorkRequestBuilder<MorningNotificationWorker>(1, TimeUnit.DAYS)
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

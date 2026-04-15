package com.arise.habitquest.worker

import android.app.NotificationManager
import android.content.Context
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
class MorningNotificationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            timeProvider.sync()
            val profile = userRepository.getUserProfile() ?: return Result.success()
            val todayMissions = missionRepository.getMissionsForDate(timeProvider.sessionDay())
            val activeMissions = todayMissions.count { it.isActive }

            if (activeMissions > 0) {
                NotificationHelper.send(
                    context = appContext,
                    channelId = "arise_reminders",
                    channelName = "Mission Reminders",
                    importance = NotificationManager.IMPORTANCE_DEFAULT,
                    notificationId = 1001,
                    title = "SYSTEM ALERT",
                    body = "Your Gates await, ${profile.hunterName}. $activeMissions missions today. ARISE.",
                    smallIcon = android.R.drawable.ic_menu_compass
                )
            }
            Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "arise_morning_notification"

        fun schedule(workManager: WorkManager, timeProvider: TimeProvider, hourOfDay: Int) {
            val now = timeProvider.trustedNow().toLocalTime()
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

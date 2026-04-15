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
class EveningReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        timeProvider.sync()
        val activeMissions = missionRepository.getMissionsForDate(timeProvider.sessionDay())
            .filter { it.isActive }

        if (activeMissions.isNotEmpty()) {
            val profile = userRepository.getUserProfile()
            val hunterName = profile?.hunterName ?: "Hunter"
            NotificationHelper.send(
                context = appContext,
                channelId = "arise_reminders",
                channelName = "Mission Reminders",
                importance = NotificationManager.IMPORTANCE_DEFAULT,
                notificationId = 1002,
                title = "[ SYSTEM WARNING ]",
                body = "The System grows impatient, $hunterName. ${activeMissions.size} gates remain unsealed. " +
                        "Complete them before midnight or face consequences.",
                smallIcon = android.R.drawable.ic_dialog_alert
            )
        }
        Result.success()
    } catch (_: Exception) {
        Result.success()
    }

    companion object {
        const val WORK_NAME = "arise_evening_reminder"

        fun schedule(workManager: WorkManager, timeProvider: TimeProvider) {
            val now = timeProvider.trustedNow().toLocalTime()
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

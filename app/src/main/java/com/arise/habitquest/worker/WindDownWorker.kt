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
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Fires at 21:15 each evening to remind the user about evening-specific and
 * wellness missions (sleep, journaling, gratitude, stretching, etc.) that are
 * still incomplete.  Intentionally softer in tone than the generic 20:00
 * evening reminder — these tasks require intentional wind-down, not a panic.
 */
@HiltWorker
class WindDownWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        timeProvider.sync()
        val allMissions = missionRepository.getMissionsForDate(timeProvider.sessionDay())

        // Target: evening-hinted missions OR wellness category — that are still active.
        // Sleep missions are excluded: they have their own SleepReminderWorker that fires
        // at the correct time relative to the user's actual bedtime target.
        val eveningMissions = allMissions.filter { mission ->
            mission.isActive &&
                !SleepReminderWorker.isSleepMission(mission.title) &&
                (mission.scheduledTimeHint == "EVENING" ||
                    mission.category == MissionCategory.WELLNESS)
        }

        if (eveningMissions.isEmpty()) return Result.success()

        val profile = userRepository.getUserProfile()
        val hunterName = profile?.hunterName ?: "Hunter"

        val hasSleep = eveningMissions.any { it.title.contains("sleep", ignoreCase = true) ||
                it.title.contains("bed", ignoreCase = true) }
        val hasJournal = eveningMissions.any { it.title.contains("journal", ignoreCase = true) ||
                it.title.contains("reflect", ignoreCase = true) }
        val hasGratitude = eveningMissions.any { it.title.contains("gratitude", ignoreCase = true) }
        val hasMeditation = eveningMissions.any { it.title.contains("meditat", ignoreCase = true) ||
                it.title.contains("mind", ignoreCase = true) }

        val body = buildBody(hunterName, eveningMissions, hasSleep, hasJournal, hasGratitude, hasMeditation)

        sendNotification("[ EVENING PROTOCOL ]", body)
        return Result.success()
    }

    private fun buildBody(
        name: String,
        missions: List<Mission>,
        hasSleep: Boolean,
        hasJournal: Boolean,
        hasGratitude: Boolean,
        hasMeditation: Boolean
    ): String {
        return when {
            hasSleep -> "Hunter $name. Your sleep gate is still open. " +
                "The System monitors recovery — poor rest compounds into weakness. " +
                "Begin your bedtime ritual now."
            hasMeditation -> "$name. Your mind gate remains unsealed. " +
                "Stillness is not weakness — it is recovery. " +
                "${missions.size} evening ${if (missions.size == 1) "gate" else "gates"} await."
            hasJournal || hasGratitude -> "$name. The System requires your reflection. " +
                "${missions.size} evening ${if (missions.size == 1) "gate" else "gates"} remain. " +
                "Close the day with intention."
            else -> "Hunter $name. ${missions.size} evening " +
                "${if (missions.size == 1) "gate remains" else "gates remain"} unsealed. " +
                "The System does not allow unfinished business to carry into rest."
        }
    }

    private fun sendNotification(title: String, body: String) {
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "arise_wind_down"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Evening Wind-Down",
                NotificationManager.IMPORTANCE_DEFAULT  // Quieter than the 8 PM warning
            ).apply { description = "Evening ritual and wellness mission reminders." }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_arise_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "arise_wind_down"
        private const val NOTIFICATION_ID = 1003

        fun schedule(workManager: WorkManager) {
            val now = LocalTime.now()
            val target = LocalTime.of(21, 15)
            var delayMinutes = now.until(target, ChronoUnit.MINUTES)
            if (delayMinutes <= 0) delayMinutes += 24 * 60

            val request = PeriodicWorkRequestBuilder<WindDownWorker>(1, TimeUnit.DAYS)
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

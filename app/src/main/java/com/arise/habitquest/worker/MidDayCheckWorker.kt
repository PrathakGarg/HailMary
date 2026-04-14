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
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Fires at 13:00 each day.  Only notifies if MORNING-hinted missions are still
 * incomplete — these are tasks with an explicit morning window that the user has
 * let slip past noon.  The afternoon is still early enough to act; this is a
 * "you still have time" nudge, not a punishment.
 *
 * Silent on days where all morning missions are already done.
 */
@HiltWorker
class MidDayCheckWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        timeProvider.sync()
        val allMissions = missionRepository.getMissionsForDate(timeProvider.sessionDay())

        // Only care about morning-tagged missions still incomplete at midday
        val staleMorningMissions = allMissions.filter { mission ->
            mission.isActive && mission.scheduledTimeHint == "MORNING"
        }

        if (staleMorningMissions.isEmpty()) return Result.success()

        val profile = userRepository.getUserProfile()
        val hunterName = profile?.hunterName ?: "Hunter"
        val count = staleMorningMissions.size

        val missionNames = staleMorningMissions
            .take(2)
            .joinToString(", ") { it.title.substringBefore("[").trim() }
        val tail = if (count > 2) " and ${count - 2} more" else ""

        val body = "Hunter $hunterName. $count morning ${if (count == 1) "gate is" else "gates are"} still open " +
            "($missionNames$tail). The afternoon is yours — act before it becomes evening debt."

        sendNotification("[ MIDDAY STATUS CHECK ]", body)
        return Result.success()
    }

    private fun sendNotification(title: String, body: String) {
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "arise_midday"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Midday Gate Check",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Reminder for morning missions still open at midday." }
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
        const val WORK_NAME = "arise_midday_check"
        private const val NOTIFICATION_ID = 1006

        fun schedule(workManager: WorkManager) {
            val now = LocalTime.now()
            val target = LocalTime.of(13, 0)
            var delayMinutes = now.until(target, ChronoUnit.MINUTES)
            if (delayMinutes <= 0) delayMinutes += 24 * 60

            val request = PeriodicWorkRequestBuilder<MidDayCheckWorker>(1, TimeUnit.DAYS)
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

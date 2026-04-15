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
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * One-shot worker that fires 45 minutes before the user's sleep mission target.
 * Scheduled each morning by [DailyResetWorker] after missions are generated.
 *
 * For a "Sleep by 1:00" mission the reminder fires at 00:15 — after midnight
 * but well before the 4:30 AM reset, so the gate is still open to complete.
 */
@HiltWorker
class SleepReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            timeProvider.sync()
            val missions = missionRepository.getMissionsForDate(timeProvider.sessionDay())
            val sleepMission = missions.firstOrNull { isSleepMission(it.title) }
                ?: return Result.success()
            if (!sleepMission.isActive) return Result.success()

        val profile = userRepository.getUserProfile()
        val hunterName = profile?.hunterName ?: "Hunter"
        val targetHour = extractSleepHour(sleepMission.title)

        val timeStr = if (targetHour != null) {
            if (targetHour == 0) "midnight" else "$targetHour:00"
        } else "your target"

        val body = "Hunter $hunterName. Your sleep gate closes at $timeStr — 45 minutes remain. " +
            "Dim the lights. Put the phone down after you mark this complete. Recovery is not optional."

        NotificationHelper.send(
            context = appContext,
            channelId = "arise_sleep_reminder",
            channelName = "Sleep Reminder",
            channelDescription = "Fires 45 minutes before your sleep mission target.",
            importance = NotificationManager.IMPORTANCE_HIGH,
            notificationId = NOTIFICATION_ID,
            title = "[ SLEEP GATE CLOSING ]",
            body = body
        )
        Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "arise_sleep_reminder"
        private const val NOTIFICATION_ID = 1005
        private const val WIND_DOWN_MINUTES = 45L

        fun isSleepMission(title: String): Boolean =
            title.contains("Recovery Gate", ignoreCase = true) ||
                title.contains("sleep", ignoreCase = true) ||
                title.contains("bedtime", ignoreCase = true)

        fun extractSleepHour(title: String): Int? =
            Regex("""by (\d+):""").find(title)?.groupValues?.get(1)?.toIntOrNull()

        /**
         * Schedules a one-shot notification at [targetHour]:00 minus [WIND_DOWN_MINUTES].
         *
         * Hours < 6 are treated as "after midnight" — the reminder fires on the
         * next calendar date relative to when this is called (4:30 AM reset day).
         * e.g. "Sleep by 1:00" → fires tonight at 00:15.
         */
        fun scheduleFor(workManager: WorkManager, timeProvider: TimeProvider, targetHour: Int) {
            val now = timeProvider.trustedNow().toLocalDateTime()
            val today = now.toLocalDate()

            // Build the target datetime: hours < 6 are after midnight → next calendar day
            val targetDateTime = if (targetHour < 6) {
                today.plusDays(1).atTime(targetHour, 0)
            } else {
                today.atTime(targetHour, 0)
            }
            val windDownDateTime = targetDateTime.minusMinutes(WIND_DOWN_MINUTES)

            val delayMs = windDownDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                timeProvider.trustedNow().toInstant().toEpochMilli()

            if (delayMs <= 0) return // Wind-down time already passed today

            val request = OneTimeWorkRequestBuilder<SleepReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()

            // REPLACE so if missions are regenerated mid-day the schedule stays accurate
            workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}

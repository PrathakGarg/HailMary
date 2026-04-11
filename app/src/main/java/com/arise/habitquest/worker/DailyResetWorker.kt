package com.arise.habitquest.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.usecase.ApplyDailyResetUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val applyDailyReset: ApplyDailyResetUseCase,
    private val missionRepository: MissionRepository,
    private val timeProvider: TimeProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            timeProvider.sync()
            applyDailyReset()
            scheduleSleepReminderIfNeeded()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }

    private suspend fun scheduleSleepReminderIfNeeded() {
        val today = timeProvider.today()
        val missions = missionRepository.getMissionsForDate(today)
        val sleepMission = missions.firstOrNull { SleepReminderWorker.isSleepMission(it.title) }
            ?: return
        val targetHour = SleepReminderWorker.extractSleepHour(sleepMission.title) ?: return
        SleepReminderWorker.scheduleFor(WorkManager.getInstance(applicationContext), targetHour)
    }

    companion object {
        const val WORK_NAME = "arise_daily_reset"

        // Reset at 4:30 AM — late enough for night owls to complete sleep/evening
        // missions, early enough that morning people see fresh missions when they wake.
        private val RESET_HOUR = 4
        private val RESET_MINUTE = 30

        fun schedule(workManager: WorkManager, resetHour: Int = 4, resetMinute: Int = 30) {
            val now = LocalDateTime.now()
            val todayReset = now.toLocalDate().atTime(resetHour, resetMinute)
            val nextReset = if (now.isBefore(todayReset)) todayReset else todayReset.plusDays(1)
            val delay = nextReset.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                    System.currentTimeMillis()

            val request = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}

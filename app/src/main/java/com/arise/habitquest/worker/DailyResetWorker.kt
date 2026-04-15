package com.arise.habitquest.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.usecase.ApplyDailyResetUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
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
        val today = timeProvider.sessionDay()
        val missions = missionRepository.getMissionsForDate(today)
        val sleepMission = missions.firstOrNull { SleepReminderWorker.isSleepMission(it.title) }
            ?: return
        val targetHour = SleepReminderWorker.extractSleepHour(sleepMission.title) ?: return
        SleepReminderWorker.scheduleFor(WorkManager.getInstance(applicationContext), timeProvider, targetHour)
    }

    companion object {
        const val WORK_NAME = "arise_daily_reset"

        fun schedule(workManager: WorkManager, timeProvider: TimeProvider) {
            val now = timeProvider.trustedNow().toLocalDateTime()
            val nextReset = timeProvider.nextDayStartDateTime()
            val delay = Duration.between(now, nextReset).toMillis().coerceAtLeast(0L)

            val request = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
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

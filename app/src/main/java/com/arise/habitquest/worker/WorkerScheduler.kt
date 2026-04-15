package com.arise.habitquest.worker

import androidx.work.WorkManager
import com.arise.habitquest.data.time.TimeProvider

object WorkerScheduler {

    fun scheduleAll(
        workManager: WorkManager,
        timeProvider: TimeProvider,
        notificationHour: Int
    ) {
        DailyResetWorker.schedule(workManager, timeProvider)
        MorningNotificationWorker.schedule(workManager, timeProvider, notificationHour)
        PreResetReminderWorker.schedule(workManager, timeProvider)
        MidDayCheckWorker.schedule(workManager, timeProvider)
        EveningReminderWorker.schedule(workManager, timeProvider)
        WindDownWorker.schedule(workManager, timeProvider)
        MonthlyReportWorker.schedule(workManager)
    }
}

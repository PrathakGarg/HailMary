package com.arise.habitquest.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val tp = com.arise.habitquest.data.time.TimeProvider.getInstance(context)
            val workManager = WorkManager.getInstance(context)
            DailyResetWorker.schedule(workManager, tp)
            MorningNotificationWorker.schedule(workManager, hourOfDay = tp.resetHour.coerceAtLeast(6).let {
                // Morning notification should always be after the reset; default to 8 if reset is early
                if (it < 6) 8 else it
            })
            PreResetReminderWorker.schedule(workManager, tp)
            MidDayCheckWorker.schedule(workManager)
            EveningReminderWorker.schedule(workManager)
            WindDownWorker.schedule(workManager)
            MonthlyReportWorker.schedule(workManager)
        }
    }
}

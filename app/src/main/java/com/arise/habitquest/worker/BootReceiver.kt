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
            val workManager = WorkManager.getInstance(context)
            DailyResetWorker.schedule(workManager)
            MorningNotificationWorker.schedule(workManager, hourOfDay = 8)
            MidDayCheckWorker.schedule(workManager)
            EveningReminderWorker.schedule(workManager)
            WindDownWorker.schedule(workManager)
            MonthlyReportWorker.schedule(workManager)
        }
    }
}

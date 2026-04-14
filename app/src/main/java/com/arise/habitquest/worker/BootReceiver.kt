package com.arise.habitquest.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val tp = com.arise.habitquest.data.time.TimeProvider.getInstance(context)
            val workManager = WorkManager.getInstance(context)
            val notificationHour = runBlocking {
                OnboardingDataStore(context.applicationContext).notificationHour.first()
            }
            DailyResetWorker.schedule(workManager, tp)
            MorningNotificationWorker.schedule(workManager, hourOfDay = notificationHour)
            PreResetReminderWorker.schedule(workManager, tp)
            MidDayCheckWorker.schedule(workManager)
            EveningReminderWorker.schedule(workManager)
            WindDownWorker.schedule(workManager)
            MonthlyReportWorker.schedule(workManager)
        }
    }
}

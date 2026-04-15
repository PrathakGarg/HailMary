package com.arise.habitquest.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.time.TimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val tp = TimeProvider.getInstance(context)
            val workManager = WorkManager.getInstance(context)
            val notificationHour = runBlocking {
                OnboardingDataStore(context.applicationContext).notificationHour.first()
            }
            WorkerScheduler.scheduleAll(workManager, tp, notificationHour)
        }
    }
}

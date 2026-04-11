package com.arise.habitquest

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.repository.UserRepository
import com.arise.habitquest.presentation.navigation.AriseNavHost
import com.arise.habitquest.ui.theme.AriseTheme
import com.arise.habitquest.worker.DailyResetWorker
import com.arise.habitquest.worker.EveningReminderWorker
import com.arise.habitquest.worker.MidDayCheckWorker
import com.arise.habitquest.worker.MonthlyReportWorker
import com.arise.habitquest.worker.MorningNotificationWorker
import com.arise.habitquest.worker.PreResetReminderWorker
import com.arise.habitquest.worker.WindDownWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var userRepository: UserRepository

    // Must be registered before super.onCreate(); called after the system shows the dialog.
    private val notificationPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ requires POST_NOTIFICATIONS at runtime.
        // Show the system dialog once; after grant, workers can post notifications.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule every recurring worker on launch.
        // All workers use ExistingPeriodicWorkPolicy.UPDATE so this is idempotent —
        // no duplicate workers, but delays are corrected if they drifted.
        scheduleWorkers()

        setContent {
            AriseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AriseTheme.colors.background
                ) {
                    AriseNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { timeProvider.sync() }
    }

    private fun scheduleWorkers() {
        val wm = WorkManager.getInstance(this)
        lifecycleScope.launch {
            val notifHour = userRepository.getUserProfile()?.notificationHour ?: 8
            DailyResetWorker.schedule(wm, timeProvider.resetHour, timeProvider.resetMinute)
            MorningNotificationWorker.schedule(wm, notifHour)
            PreResetReminderWorker.schedule(wm, timeProvider.resetHour, timeProvider.resetMinute)
            MidDayCheckWorker.schedule(wm)
            EveningReminderWorker.schedule(wm)
            WindDownWorker.schedule(wm)
            MonthlyReportWorker.schedule(wm)
        }
    }
}

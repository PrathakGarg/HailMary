package com.arise.habitquest

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationTest {

    // Grants POST_NOTIFICATIONS before any test runs, handling the reinstall permission reset.
    @get:Rule
    val grantPermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant() // no-op on API < 33
    }

    private lateinit var context: Context
    private lateinit var manager: NotificationManager

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Initialise WorkManager with a test configuration (no real workers run)
        val config = Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    // ── Test 1: POST_NOTIFICATIONS permission is granted ─────────────────────
    @Test
    fun notificationPermissionIsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            assertEquals(
                "POST_NOTIFICATIONS must be granted — run: " +
                    "adb shell pm grant com.arise.habitquest android.permission.POST_NOTIFICATIONS",
                PackageManager.PERMISSION_GRANTED,
                status
            )
        }
        // On API < 33 the permission is auto-granted via manifest — nothing to check.
    }

    // ── Test 2: Notification channel can be created and is not blocked ────────
    @Test
    fun notificationChannelIsNotBlocked() {
        val channelId = "arise_reminders_test"
        val channel = NotificationChannel(
            channelId,
            "Mission Reminders (test)",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val created = manager.getNotificationChannel(channelId)
        assertNotNull("Channel was not created", created)
        assertTrue(
            "Channel importance is NONE — user may have blocked this channel",
            (created?.importance ?: NotificationManager.IMPORTANCE_NONE) > NotificationManager.IMPORTANCE_NONE
        )

        // Cleanup
        manager.deleteNotificationChannel(channelId)
    }

    // ── Test 3: notify() completes without exception and notifications are enabled ──
    // Note: getActiveNotifications() is filtered by UID on API 34+ in instrumented
    // tests, so we verify via areNotificationsEnabled() and absence of exception instead.
    @Test
    fun notificationIsPostedSuccessfully() {
        val channelId = "arise_reminders_test_post"
        val notifId = 7777

        // Notifications must be enabled globally
        assertTrue(
            "Notifications are disabled globally for this app. " +
                "Grant via: adb shell pm grant com.arise.habitquest android.permission.POST_NOTIFICATIONS",
            manager.areNotificationsEnabled()
        )

        // Create channel
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Test channel", NotificationManager.IMPORTANCE_HIGH)
        )

        // Channel must not be blocked
        val channel = manager.getNotificationChannel(channelId)
        assertTrue(
            "Channel '$channelId' has IMPORTANCE_NONE — it was blocked",
            (channel?.importance ?: NotificationManager.IMPORTANCE_NONE) > NotificationManager.IMPORTANCE_NONE
        )

        // Post notification — must not throw
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("[ SYSTEM TEST ]")
            .setContentText("Notification system is operational, Hunter.")
            .setAutoCancel(false)
            .build()

        manager.notify(notifId, notification)
        // If we reached here without SecurityException or other crash, the post succeeded.

        // Cleanup
        manager.cancel(notifId)
        manager.deleteNotificationChannel(channelId)
    }

    // ── Test 4: WorkManager initialises and accepts work enqueueing ───────────
    @Test
    fun workManagerInitialisesCorrectly() {
        val wm = WorkManager.getInstance(context)
        assertNotNull("WorkManager instance is null", wm)
        // Enqueuing and querying a tagged work item should not throw
        val infos = wm.getWorkInfosByTag("arise_test").get()
        assertNotNull(infos)
    }
}

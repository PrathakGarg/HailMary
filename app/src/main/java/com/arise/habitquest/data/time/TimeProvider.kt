package com.arise.habitquest.data.time

import android.content.Context
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tamper-resistant time provider.
 *
 * On every [sync] call it queries pool.ntp.org via SNTP and stores a
 * (networkTimeMs, elapsedRealtimeMs) anchor pair.  Subsequent calls to
 * [nowMillis] and [today] compute current time from:
 *
 *   networkTimeMs + (SystemClock.elapsedRealtime() – elapsedRealtimeMs)
 *
 * Because elapsedRealtime() is a monotonic clock rooted at device boot it
 * cannot be modified by the user — changing Settings → Date & Time only
 * affects System.currentTimeMillis().  This means even if the device is
 * offline the anchor stays accurate indefinitely, and any manual clock
 * adjustment is silently ignored.
 *
 * If no anchor has ever been saved and the device is offline, we fall back
 * to the device clock (first-launch, airplane mode edge case).
 */
@Singleton
class TimeProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory cache of the anchor (also persisted to SharedPreferences)
    @Volatile private var anchorNetworkMs: Long = prefs.getLong(KEY_NETWORK_MS, 0L)
    @Volatile private var anchorElapsedMs: Long = prefs.getLong(KEY_ELAPSED_MS, 0L)

    // Reset time — the hour/minute at which the app day rolls over.
    // Cached in SharedPrefs so sessionDay() is always synchronous.
    @Volatile var resetHour: Int = prefs.getInt(KEY_RESET_HOUR, DEFAULT_RESET_HOUR)
        private set
    @Volatile var resetMinute: Int = prefs.getInt(KEY_RESET_MINUTE, DEFAULT_RESET_MINUTE)
        private set

    fun setResetTime(hour: Int, minute: Int = 0) {
        resetHour = hour
        resetMinute = minute
        prefs.edit().putInt(KEY_RESET_HOUR, hour).putInt(KEY_RESET_MINUTE, minute).apply()
    }

    init {
        // Expose singleton for widget code running outside Hilt scope
        instance = this
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Milliseconds since Unix epoch, corrected for device clock tampering. */
    fun nowMillis(): Long = if (anchorNetworkMs > 0L) {
        anchorNetworkMs + (SystemClock.elapsedRealtime() - anchorElapsedMs)
    } else {
        System.currentTimeMillis()
    }

    /** Current local date according to the tamper-resistant clock. */
    fun today(): LocalDate = Instant.ofEpochMilli(nowMillis())
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    /**
     * The "session day" — the date the app considers active for missions.
     *
     * The day rolls over at [resetHour]:[resetMinute] (default 04:30).
     * Between midnight and that boundary the user is still completing the
     * previous calendar day's missions, so this returns yesterday.
     * At or after the boundary it matches [today].
     */
    fun sessionDay(): LocalDate {
        val zdt = Instant.ofEpochMilli(nowMillis()).atZone(ZoneId.systemDefault())
        val t = zdt.toLocalTime()
        return if (t.hour < resetHour || (t.hour == resetHour && t.minute < resetMinute)) {
            zdt.toLocalDate().minusDays(1)
        } else {
            zdt.toLocalDate()
        }
    }

    /**
     * Minutes remaining until the next day reset boundary.
     * Used by the UI to show "time almost up" warnings.
     */
    fun minutesUntilReset(): Long {
        val now = Instant.ofEpochMilli(nowMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val resetToday = now.toLocalDate().atTime(resetHour, resetMinute)
        val nextReset = if (now.isBefore(resetToday)) resetToday else resetToday.plusDays(1)
        return java.time.Duration.between(now, nextReset).toMinutes()
    }

    /**
     * Returns true when the device's system clock differs from our NTP-anchored
     * time by more than [TAMPER_THRESHOLD_MS].  Callers can surface a warning to
     * the user.
     */
    fun isClockTampered(): Boolean {
        if (anchorNetworkMs == 0L) return false
        return kotlin.math.abs(System.currentTimeMillis() - nowMillis()) > TAMPER_THRESHOLD_MS
    }

    /**
     * Synchronises with pool.ntp.org.  Safe to call from any coroutine —
     * switches to Dispatchers.IO internally.  Silently no-ops if offline.
     */
    suspend fun sync() = withContext(Dispatchers.IO) {
        try {
            val ntpMs = queryNtp(NTP_HOST)
            val elapsed = SystemClock.elapsedRealtime()
            anchorNetworkMs = ntpMs
            anchorElapsedMs = elapsed
            prefs.edit()
                .putLong(KEY_NETWORK_MS, ntpMs)
                .putLong(KEY_ELAPSED_MS, elapsed)
                .apply()
        } catch (_: Exception) {
            // Offline or NTP blocked — existing anchor remains valid via
            // monotonic elapsedRealtime tracking.
        }
    }

    // ── SNTP client ───────────────────────────────────────────────────────────

    private fun queryNtp(host: String): Long {
        val socket = DatagramSocket()
        socket.soTimeout = NTP_TIMEOUT_MS
        try {
            val buffer = ByteArray(48)
            // LI=0 (no warning), VN=3 (NTPv3), Mode=3 (client)
            buffer[0] = 0x1B.toByte()
            val address = InetAddress.getByName(host)
            socket.send(DatagramPacket(buffer, buffer.size, address, NTP_PORT))
            val response = DatagramPacket(ByteArray(48), 48)
            socket.receive(response)
            val data = response.data
            // Transmit Timestamp (T3) starts at byte 40 — seconds since 1900-01-01
            var seconds = 0L
            for (i in 40..43) seconds = (seconds shl 8) or (data[i].toLong() and 0xFF)
            // Convert from NTP epoch (1900) to Unix epoch (1970): subtract 70 years
            return (seconds - NTP_EPOCH_OFFSET_SECONDS) * 1000L
        } finally {
            socket.close()
        }
    }

    // ── Widget / non-Hilt access ──────────────────────────────────────────────

    companion object {
        private const val PREFS_NAME = "arise_time_anchor"
        private const val KEY_NETWORK_MS = "network_ms"
        private const val KEY_ELAPSED_MS = "elapsed_ms"
        private const val KEY_RESET_HOUR = "reset_hour"
        private const val KEY_RESET_MINUTE = "reset_minute"
        const val DEFAULT_RESET_HOUR = 4
        const val DEFAULT_RESET_MINUTE = 30
        private const val NTP_HOST = "pool.ntp.org"
        private const val NTP_PORT = 123
        private const val NTP_TIMEOUT_MS = 5_000
        private const val NTP_EPOCH_OFFSET_SECONDS = 2_208_988_800L // 70 years
        private const val TAMPER_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes

        @Volatile private var instance: TimeProvider? = null

        /**
         * Returns the singleton instance for code that runs outside the Hilt
         * component graph (e.g. the Glance widget).  The instance is always set
         * before the widget can possibly run because the Application creates it.
         */
        fun getInstance(context: Context): TimeProvider =
            instance ?: synchronized(this) {
                instance ?: TimeProvider(context.applicationContext).also { instance = it }
            }
    }
}

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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal time engine with tamper protection.
 *
 * - Trusted clock: SNTP anchor + elapsedRealtime() drift-free progression.
 * - Day boundary: single minute-of-day value in 30-minute increments.
 * - Session day: before day start => previous date; at/after => current date.
 */
@Singleton
class TimeProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory cache of the anchor (also persisted to SharedPreferences)
    @Volatile private var anchorNetworkMs: Long = prefs.getLong(KEY_NETWORK_MS, 0L)
    @Volatile private var anchorElapsedMs: Long = prefs.getLong(KEY_ELAPSED_MS, 0L)

    @Volatile
    var dayStartMinutes: Int = normalizeToHalfHour(prefs.getInt(KEY_DAY_START_MINUTES, DEFAULT_DAY_START_MINUTES))
        private set

    val resetHour: Int get() = dayStartMinutes / 60
    val resetMinute: Int get() = dayStartMinutes % 60

    fun setDayStartMinutes(minutes: Int) {
        val normalized = normalizeToHalfHour(minutes)
        dayStartMinutes = normalized
        prefs.edit()
            .putInt(KEY_DAY_START_MINUTES, normalized)
            .putInt(KEY_RESET_HOUR, normalized / 60)
            .putInt(KEY_RESET_MINUTE, normalized % 60)
            .apply()
    }

    // Compatibility shim for existing call-sites.
    fun setResetTime(hour: Int, minute: Int = 0) {
        setDayStartMinutes(hour * 60 + minute)
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

    fun trustedNow(): ZonedDateTime = Instant.ofEpochMilli(nowMillis()).atZone(ZoneId.systemDefault())

    /** Current local date according to the trusted clock. */
    fun today(): LocalDate = trustedNow().toLocalDate()

    /**
     * Date currently active for missions.
     */
    fun sessionDay(): LocalDate {
        val now = trustedNow()
        return if (now.toLocalTime() < dayStartTime()) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
    }

    fun nextDayStartDateTime(): LocalDateTime {
        val now = trustedNow().toLocalDateTime()
        val startToday = now.toLocalDate().atTime(dayStartTime())
        return if (now < startToday) startToday else startToday.plusDays(1)
    }

    /**
     * Minutes until the next day start boundary.
     */
    fun minutesUntilReset(): Long {
        val now = trustedNow().toLocalDateTime()
        return java.time.Duration.between(now, nextDayStartDateTime()).toMinutes()
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
        private const val KEY_DAY_START_MINUTES = "day_start_minutes"
        private const val KEY_RESET_HOUR = "reset_hour"
        private const val KEY_RESET_MINUTE = "reset_minute"
        const val DEFAULT_RESET_HOUR = 4
        const val DEFAULT_RESET_MINUTE = 30
        const val DEFAULT_DAY_START_MINUTES = DEFAULT_RESET_HOUR * 60 + DEFAULT_RESET_MINUTE
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

    private fun dayStartTime(): LocalTime = LocalTime.of(resetHour, resetMinute)

    private fun normalizeToHalfHour(minutes: Int): Int {
        val clamped = minutes.coerceIn(0, 23 * 60 + 30)
        return (clamped / 30) * 30
    }
}

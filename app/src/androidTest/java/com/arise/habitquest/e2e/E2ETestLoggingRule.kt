package com.arise.habitquest.e2e

import android.util.Log
import org.junit.rules.TestWatcher
import org.junit.runner.Description

open class E2ETestLoggingRule : TestWatcher() {
    private val starts = mutableMapOf<String, Long>()

    override fun starting(description: Description) {
        val key = description.displayName
        starts[key] = System.currentTimeMillis()
        emit("START", description)
    }

    override fun succeeded(description: Description) {
        emit("PASS", description, durationMs(description))
    }

    override fun failed(e: Throwable, description: Description) {
        val reason = e.message?.lineSequence()?.firstOrNull()?.trim().orEmpty()
        emit("FAIL", description, durationMs(description), reason)
    }

    override fun finished(description: Description) {
        starts.remove(description.displayName)
    }

    private fun durationMs(description: Description): Long {
        val started = starts[description.displayName] ?: return -1L
        return System.currentTimeMillis() - started
    }

    private fun emit(status: String, description: Description, durationMs: Long? = null, reason: String? = null) {
        val suffix = buildString {
            if (durationMs != null && durationMs >= 0) append(" (${durationMs}ms)")
            if (!reason.isNullOrBlank()) append(" :: $reason")
        }
        val line = "[E2E][$status] ${description.className}#${description.methodName}$suffix"
        println(line)
        Log.i("E2E", line)
    }
}

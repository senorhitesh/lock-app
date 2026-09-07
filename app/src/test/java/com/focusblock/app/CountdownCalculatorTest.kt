package com.focusblock.app

import com.focusblock.app.data.model.BlockSession
import com.focusblock.app.utils.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountdownCalculatorTest {

    @Test
    fun testFormatDuration() {
        assertEquals("00:00:00", TimeUtils.formatDuration(0))
        assertEquals("00:01:00", TimeUtils.formatDuration(60_000))
        assertEquals("01:00:00", TimeUtils.formatDuration(3600_000))
        assertEquals("01:30:15", TimeUtils.formatDuration(5415_000))
    }

    @Test
    fun testFormatDurationShort() {
        assertEquals("0m", TimeUtils.formatDurationShort(0))
        assertEquals("15m", TimeUtils.formatDurationShort(15 * 60_000L))
        assertEquals("1h", TimeUtils.formatDurationShort(60 * 60_000L))
        assertEquals("1h 30m", TimeUtils.formatDurationShort(90 * 60_000L))
    }

    @Test
    fun testBlockSessionActive() {
        val now = System.currentTimeMillis()
        val session = BlockSession(
            startTime = now - 1000,
            endTime = now + 60_000,
            blockedPackages = setOf("com.example.app"),
            isStrictMode = false
        )
        assertTrue(session.isActive)
        assertTrue(session.remainingMillis > 0)
    }
}

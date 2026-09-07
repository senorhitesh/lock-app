package com.focusblock.app.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

object TimeUtils {

    fun formatDuration(millis: Long): String {
        if (millis <= 0) return "00:00:00"
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun formatDurationShort(millis: Long): String {
        if (millis <= 0) return "0m"
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

    fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getYesterdayStartMillis(): Long = getTodayStartMillis() - 24 * 60 * 60 * 1000L

    fun getNextAlarmTime(hour: Int, minute: Int, daysOfWeek: Int): Long {
        val now = Calendar.getInstance()
        for (dayOffset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.timeInMillis <= now.timeInMillis) continue
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            val bit = 1 shl (dayOfWeek - 1)
            if (daysOfWeek and bit != 0) return candidate.timeInMillis
        }
        return -1L
    }

    fun formatDayLabel(dayStartMillis: Long): String {
        val todayStart = getTodayStartMillis()
        val yesterdayStart = getYesterdayStartMillis()
        return when (dayStartMillis) {
            todayStart -> "Today"
            yesterdayStart -> "Yesterday"
            else -> {
                val cal = Calendar.getInstance().apply { timeInMillis = dayStartMillis }
                "%02d/%02d/%04d".format(
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR)
                )
            }
        }
    }
}

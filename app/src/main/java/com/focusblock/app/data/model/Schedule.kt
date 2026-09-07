package com.focusblock.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val daysOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val blockedPackages: String,
    val isEnabled: Boolean = true
) {
    companion object {
        const val SUNDAY = 1
        const val MONDAY = 2
        const val TUESDAY = 4
        const val WEDNESDAY = 8
        const val THURSDAY = 16
        const val FRIDAY = 32
        const val SATURDAY = 64
        const val EVERY_DAY = 127

        fun dayName(bit: Int): String = when (bit) {
            SUNDAY -> "Sun"; MONDAY -> "Mon"; TUESDAY -> "Tue"
            WEDNESDAY -> "Wed"; THURSDAY -> "Thu"; FRIDAY -> "Fri"
            SATURDAY -> "Sat"; else -> ""
        }
    }

    fun getDaysList(): List<Int> =
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
            .filter { daysOfWeek and it != 0 }

    fun getDaysDisplay(): String = getDaysList().joinToString(", ") { dayName(it) }

    fun getTimeDisplay(): String = "%02d:%02d → %02d:%02d".format(startHour, startMinute, endHour, endMinute)

    fun crossesMidnight(): Boolean {
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        return endMinutes <= startMinutes
    }
}

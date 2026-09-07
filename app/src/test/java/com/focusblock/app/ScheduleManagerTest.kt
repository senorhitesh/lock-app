package com.focusblock.app

import com.focusblock.app.data.model.Schedule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleManagerTest {

    @Test
    fun testCrossesMidnight() {
        val daySchedule = Schedule(
            name = "Work",
            daysOfWeek = Schedule.EVERY_DAY,
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            blockedPackages = "[]"
        )
        assertFalse(daySchedule.crossesMidnight())

        val nightSchedule = Schedule(
            name = "Sleep",
            daysOfWeek = Schedule.EVERY_DAY,
            startHour = 22,
            startMinute = 0,
            endHour = 6,
            endMinute = 0,
            blockedPackages = "[]"
        )
        assertTrue(nightSchedule.crossesMidnight())
    }

    @Test
    fun testDaysList() {
        val schedule = Schedule(
            name = "Workday",
            daysOfWeek = Schedule.MONDAY or Schedule.TUESDAY,
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            blockedPackages = "[]"
        )
        val days = schedule.getDaysList()
        assertEquals(2, days.size)
        assertTrue(days.contains(Schedule.MONDAY))
        assertTrue(days.contains(Schedule.TUESDAY))
    }
}

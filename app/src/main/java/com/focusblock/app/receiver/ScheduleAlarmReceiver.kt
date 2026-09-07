package com.focusblock.app.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.data.model.Schedule
import com.focusblock.app.domain.usecase.ScheduleBlockingUseCase
import com.focusblock.app.domain.usecase.StopBlockingUseCase
import com.focusblock.app.service.BlockerForegroundService
import com.focusblock.app.utils.TimeUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ScheduleAlarmReceiver : BroadcastReceiver() {
    private val gson = Gson()

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        if (scheduleId == -1L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as FocusBlockApp
                when (intent.action) {
                    ACTION_SCHEDULE_START -> handleStart(context, app, scheduleId)
                    ACTION_SCHEDULE_END -> handleEnd(context, app)
                }
            } finally { pendingResult.finish() }
        }
    }

    private suspend fun handleStart(context: Context, app: FocusBlockApp, scheduleId: Long) {
        val schedule = app.scheduleRepository.getScheduleById(scheduleId) ?: return
        if (!schedule.isEnabled) return
        val type = object : TypeToken<Set<String>>() {}.type
        val packages: Set<String> = try { gson.fromJson(schedule.blockedPackages, type) ?: emptySet() } catch (e: Exception) { emptySet() }
        if (packages.isEmpty()) return
        val now = System.currentTimeMillis()
        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.endHour)
            set(Calendar.MINUTE, schedule.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (schedule.crossesMidnight()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val endTime = endCal.timeInMillis.coerceAtLeast(now + 60_000L)
        app.sessionRepository.startSession(endTime, packages, false)
        AlarmReceiver.scheduleSessionEnd(context, endTime)
        BlockerForegroundService.startService(context)
        ScheduleBlockingUseCase(context, app.scheduleRepository).registerSchedule(schedule)
    }

    private suspend fun handleEnd(context: Context, app: FocusBlockApp) {
        val session = app.sessionRepository.activeSession.first()
        StopBlockingUseCase(context, app.sessionRepository, app.historyRepository).execute(session, forceStop = true)
    }

    companion object {
        const val ACTION_SCHEDULE_START = "com.focusblock.app.ACTION_SCHEDULE_START"
        const val ACTION_SCHEDULE_END = "com.focusblock.app.ACTION_SCHEDULE_END"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        private const val START_REQUEST_BASE = 2000
        private const val END_REQUEST_BASE = 3000

        fun scheduleAlarms(context: Context, schedule: Schedule) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val startTime = TimeUtils.getNextAlarmTime(schedule.startHour, schedule.startMinute, schedule.daysOfWeek)
            if (startTime < 0) return
            val startIntent = PendingIntent.getBroadcast(
                context, START_REQUEST_BASE + schedule.id.toInt(),
                Intent(context, ScheduleAlarmReceiver::class.java).apply {
                    action = ACTION_SCHEDULE_START
                    putExtra(EXTRA_SCHEDULE_ID, schedule.id)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, startIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, startIntent)
            }
        }

        fun cancelScheduleAlarms(context: Context, scheduleId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            listOf(
                PendingIntent.getBroadcast(context, START_REQUEST_BASE + scheduleId.toInt(),
                    Intent(context, ScheduleAlarmReceiver::class.java).apply { action = ACTION_SCHEDULE_START },
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE),
                PendingIntent.getBroadcast(context, END_REQUEST_BASE + scheduleId.toInt(),
                    Intent(context, ScheduleAlarmReceiver::class.java).apply { action = ACTION_SCHEDULE_END },
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            ).forEach { it?.let { pi -> alarmManager.cancel(pi) } }
        }
    }
}

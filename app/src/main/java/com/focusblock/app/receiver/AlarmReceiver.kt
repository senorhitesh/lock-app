package com.focusblock.app.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.domain.usecase.StopBlockingUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SESSION_END) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as FocusBlockApp
                val session = app.sessionRepository.activeSession.first()
                StopBlockingUseCase(context, app.sessionRepository, app.historyRepository).execute(session, forceStop = true)
            } finally { pendingResult.finish() }
        }
    }

    companion object {
        const val ACTION_SESSION_END = "com.focusblock.app.ACTION_SESSION_END"
        private const val REQUEST_CODE = 1001

        fun scheduleSessionEnd(context: Context, endTime: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = PendingIntent.getBroadcast(
                context, REQUEST_CODE,
                Intent(context, AlarmReceiver::class.java).apply { action = ACTION_SESSION_END },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, intent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, intent)
            }
        }

        fun cancelSessionEnd(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = PendingIntent.getBroadcast(
                context, REQUEST_CODE,
                Intent(context, AlarmReceiver::class.java).apply { action = ACTION_SESSION_END },
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            intent?.let { alarmManager.cancel(it) }
        }
    }
}

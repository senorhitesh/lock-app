package com.focusblock.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.domain.usecase.ScheduleBlockingUseCase
import com.focusblock.app.service.BlockerForegroundService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON" &&
            action != "com.htc.intent.action.QUICKBOOT_POWERON") return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as FocusBlockApp
                val sessionEndTime = app.appPreferences.sessionEndTime.first()
                val now = System.currentTimeMillis()
                if (sessionEndTime > now) {
                    AlarmReceiver.scheduleSessionEnd(context, sessionEndTime)
                    BlockerForegroundService.startService(context)
                } else if (sessionEndTime > 0L) {
                    app.sessionRepository.endSession()
                }
                ScheduleBlockingUseCase(context, app.scheduleRepository).registerAllSchedules()
            } finally { pendingResult.finish() }
        }
    }
}

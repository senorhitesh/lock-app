package com.focusblock.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.ui.BlockingActivity
import com.focusblock.app.utils.AppUtils
import com.focusblock.app.utils.NotificationUtils
import com.focusblock.app.utils.PermissionUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BlockerForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var monitorJob: Job? = null
    private var notificationUpdateJob: Job? = null
    private lateinit var app: FocusBlockApp

    companion object {
        const val ACTION_START = "com.focusblock.app.ACTION_START"
        const val ACTION_STOP = "com.focusblock.app.ACTION_STOP"
        var isRunning = false

        fun startService(context: Context) {
            context.startForegroundService(Intent(context, BlockerForegroundService::class.java).apply { action = ACTION_START })
        }

        fun stopService(context: Context) {
            context.startService(Intent(context, BlockerForegroundService::class.java).apply { action = ACTION_STOP })
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = application as FocusBlockApp
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }

        val notification = NotificationUtils.buildBlockingNotification(this, 0, 0L)
        startForeground(FocusBlockApp.NOTIFICATION_ID, notification)
        startMonitoring()
        startNotificationUpdates()
        return START_STICKY
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                checkForegroundApp()
                delay(1500)
            }
        }
    }

    private fun startNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = serviceScope.launch {
            while (isActive) {
                updateNotification()
                delay(60_000)
            }
        }
    }

    private suspend fun checkForegroundApp() {
        if (!PermissionUtils.hasUsageAccess(this)) return
        val sessionEndTime = app.appPreferences.sessionEndTime.first()
        val now = System.currentTimeMillis()
        if (sessionEndTime <= 0L || now >= sessionEndTime) { stopSelf(); return }

        val blockedPackages = app.appPreferences.blockedPackages.first()
        val foregroundPkg = AppUtils.getForegroundPackage(this) ?: return
        if (blockedPackages.contains(foregroundPkg)) {
            val appName = AppUtils.getAppName(this, foregroundPkg)
            BlockingActivity.launch(this, foregroundPkg, appName, sessionEndTime)
        }
    }

    private suspend fun updateNotification() {
        val endTime = app.appPreferences.sessionEndTime.first()
        val blockedPkgs = app.appPreferences.blockedPackages.first()
        val remaining = maxOf(0L, endTime - System.currentTimeMillis())
        NotificationUtils.updateBlockingNotification(this, blockedPkgs.size, remaining)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        NotificationUtils.cancelBlockingNotification(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

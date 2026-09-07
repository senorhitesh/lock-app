package com.focusblock.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.ui.BlockingActivity
import com.focusblock.app.utils.AppUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var app: FocusBlockApp
    private var lastBlockedPackage: String? = null
    private var lastBlockLaunchTime: Long = 0L
    private val blockLaunchCooldownMs = 2000L

    companion object {
        var isRunning = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        app = application as FocusBlockApp
        serviceInfo = serviceInfo.also {
            it.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            it.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            it.notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName) return
        if (packageName == "com.android.systemui" || packageName == "android") return
        serviceScope.launch { checkIfBlocked(packageName) }
    }

    private suspend fun checkIfBlocked(packageName: String) {
        val endTime = app.appPreferences.sessionEndTime.first()
        val now = System.currentTimeMillis()
        if (endTime <= 0L || now >= endTime) return
        val blockedPackages = app.appPreferences.blockedPackages.first()
        if (!blockedPackages.contains(packageName)) return
        val timeSinceLast = now - lastBlockLaunchTime
        if (packageName == lastBlockedPackage && timeSinceLast < blockLaunchCooldownMs) return
        lastBlockedPackage = packageName
        lastBlockLaunchTime = now
        val appName = AppUtils.getAppName(this@BlockerAccessibilityService, packageName)
        BlockingActivity.launch(this@BlockerAccessibilityService, packageName, appName, endTime)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }
}

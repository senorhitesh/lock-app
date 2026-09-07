package com.focusblock.app.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.R
import com.focusblock.app.ui.MainActivity

object NotificationUtils {

    fun buildBlockingNotification(context: Context, blockedCount: Int, remainingMillis: Long): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contentText = context.getString(R.string.notification_text, blockedCount, TimeUtils.formatDuration(remainingMillis))
        return NotificationCompat.Builder(context, FocusBlockApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(contentText)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.notification_action_open), openIntent)
            .build()
    }

    fun updateBlockingNotification(context: Context, blockedCount: Int, remainingMillis: Long) {
        val notification = buildBlockingNotification(context, blockedCount, remainingMillis)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(FocusBlockApp.NOTIFICATION_ID, notification)
    }

    fun cancelBlockingNotification(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(FocusBlockApp.NOTIFICATION_ID)
    }
}

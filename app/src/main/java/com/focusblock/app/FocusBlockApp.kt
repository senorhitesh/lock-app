package com.focusblock.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.focusblock.app.data.db.AppDatabase
import com.focusblock.app.data.prefs.AppPreferences
import com.focusblock.app.data.repository.AppRepository
import com.focusblock.app.data.repository.HistoryRepository
import com.focusblock.app.data.repository.ScheduleRepository
import com.focusblock.app.data.repository.SessionRepository

class FocusBlockApp : Application() {

    val appPreferences: AppPreferences by lazy { AppPreferences(this) }
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val appRepository: AppRepository by lazy { AppRepository(this, appPreferences) }
    val sessionRepository: SessionRepository by lazy { SessionRepository(appPreferences) }
    val scheduleRepository: ScheduleRepository by lazy { ScheduleRepository(database.scheduleDao()) }
    val historyRepository: HistoryRepository by lazy { HistoryRepository(database.historyDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "focusblock_blocking_channel"
        const val NOTIFICATION_ID = 1
    }
}

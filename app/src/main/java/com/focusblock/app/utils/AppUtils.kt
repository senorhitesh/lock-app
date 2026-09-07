package com.focusblock.app.utils

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager

object AppUtils {

    fun getForegroundPackage(context: Context): String? {
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 10_000, now)
            stats?.filter { it.lastTimeUsed > 0 }?.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (e: Exception) { null }
    }

    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) { packageName }
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try { context.packageManager.getApplicationInfo(packageName, 0); true }
        catch (e: PackageManager.NameNotFoundException) { false }
    }
}

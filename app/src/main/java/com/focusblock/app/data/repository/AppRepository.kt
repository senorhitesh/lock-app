package com.focusblock.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.focusblock.app.data.model.InstalledApp
import com.focusblock.app.data.prefs.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val prefs: AppPreferences
) {
    val blockedPackages: Flow<Set<String>> = prefs.blockedPackages

    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val ownPackage = context.packageName
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val activities = pm.queryIntentActivities(intent, 0)
        activities
            .mapNotNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == ownPackage) return@mapNotNull null
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val icon = try { pm.getApplicationIcon(pkg) } catch (e: Exception) { null }
                    InstalledApp(packageName = pkg, appName = label, icon = icon)
                } catch (e: PackageManager.NameNotFoundException) { null }
            }
            .sortedBy { it.appName.lowercase() }
            .distinctBy { it.packageName }
    }

    suspend fun saveBlockedPackages(packages: Set<String>) { prefs.setBlockedPackages(packages) }
    suspend fun getBlockedPackages(): Set<String> = prefs.getBlockedPackagesNow()
}

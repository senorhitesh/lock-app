package com.focusblock.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focusblock_prefs")

class AppPreferences(private val context: Context) {

    private val gson = Gson()

    private object Keys {
        val BLOCKED_PACKAGES = stringPreferencesKey("blocked_packages")
        val SESSION_START_TIME = longPreferencesKey("session_start_time")
        val SESSION_END_TIME = longPreferencesKey("session_end_time")
        val SESSION_STRICT_MODE = booleanPreferencesKey("session_strict_mode")
        val IS_STRICT_MODE = booleanPreferencesKey("is_strict_mode")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    private val dataStore = context.dataStore

    val blockedPackages: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val json = prefs[Keys.BLOCKED_PACKAGES] ?: return@map emptySet()
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(json, type) ?: emptySet()
        }

    val sessionStartTime: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SESSION_START_TIME] ?: 0L }

    val sessionEndTime: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SESSION_END_TIME] ?: 0L }

    val sessionStrictMode: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SESSION_STRICT_MODE] ?: false }

    val isStrictMode: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.IS_STRICT_MODE] ?: false }

    val onboardingComplete: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    suspend fun setBlockedPackages(packages: Set<String>) {
        dataStore.edit { prefs ->
            prefs[Keys.BLOCKED_PACKAGES] = gson.toJson(packages)
        }
    }

    suspend fun setSession(startTime: Long, endTime: Long, strictMode: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SESSION_START_TIME] = startTime
            prefs[Keys.SESSION_END_TIME] = endTime
            prefs[Keys.SESSION_STRICT_MODE] = strictMode
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.SESSION_START_TIME)
            prefs.remove(Keys.SESSION_END_TIME)
            prefs.remove(Keys.SESSION_STRICT_MODE)
        }
    }

    suspend fun setStrictMode(strict: Boolean) {
        dataStore.edit { it[Keys.IS_STRICT_MODE] = strict }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun getBlockedPackagesNow(): Set<String> {
        return try {
            val prefs = dataStore.data.first()
            val json = prefs[Keys.BLOCKED_PACKAGES] ?: return emptySet()
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(json, type) ?: emptySet()
        } catch (e: Exception) { emptySet() }
    }

    suspend fun getSessionEndTimeNow(): Long {
        return try { dataStore.data.first()[Keys.SESSION_END_TIME] ?: 0L } catch (e: Exception) { 0L }
    }

    suspend fun getSessionStrictModeNow(): Boolean {
        return try { dataStore.data.first()[Keys.SESSION_STRICT_MODE] ?: false } catch (e: Exception) { false }
    }
}

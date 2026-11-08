package com.focusflow.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.focusflow.app.domain.model.AvatarStyle
import com.focusflow.app.domain.model.ThemeMode
import com.focusflow.app.domain.model.WeekStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.focusFlowDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "focusflow_prefs",
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val userName = stringPreferencesKey("user_name")
        val avatarStyle = stringPreferencesKey("avatar_style")
        val themeMode = stringPreferencesKey("theme_mode")
        val startOfWeek = stringPreferencesKey("start_of_week")
        val dailyFocusTarget = intPreferencesKey("daily_focus_target_minutes")
        val defaultFocus = intPreferencesKey("default_focus_minutes")
        val defaultShortBreak = intPreferencesKey("default_short_break_minutes")
        val defaultLongBreak = intPreferencesKey("default_long_break_minutes")
        val defaultCycle = intPreferencesKey("default_sessions_before_long_break")
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val focusCycleCount = intPreferencesKey("focus_cycle_count")
    }

    val preferences: Flow<UserPreferences> = context.focusFlowDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { prefs ->
            val defaults = UserPreferences()
            UserPreferences(
                userName = prefs[Keys.userName] ?: defaults.userName,
                avatarStyle = prefs[Keys.avatarStyle]?.toEnum(defaults.avatarStyle) ?: defaults.avatarStyle,
                themeMode = prefs[Keys.themeMode]?.toEnum(defaults.themeMode) ?: defaults.themeMode,
                startOfWeek = prefs[Keys.startOfWeek]?.toEnum(defaults.startOfWeek) ?: defaults.startOfWeek,
                dailyFocusTargetMinutes = prefs[Keys.dailyFocusTarget] ?: defaults.dailyFocusTargetMinutes,
                defaultFocusMinutes = prefs[Keys.defaultFocus] ?: defaults.defaultFocusMinutes,
                defaultShortBreakMinutes = prefs[Keys.defaultShortBreak] ?: defaults.defaultShortBreakMinutes,
                defaultLongBreakMinutes = prefs[Keys.defaultLongBreak] ?: defaults.defaultLongBreakMinutes,
                defaultSessionsBeforeLongBreak = prefs[Keys.defaultCycle] ?: defaults.defaultSessionsBeforeLongBreak,
                notificationsEnabled = prefs[Keys.notificationsEnabled] ?: defaults.notificationsEnabled,
                onboardingComplete = prefs[Keys.onboardingComplete] ?: defaults.onboardingComplete,
                focusCycleCount = prefs[Keys.focusCycleCount] ?: defaults.focusCycleCount,
            )
        }

    suspend fun snapshot(): UserPreferences = preferences.first()

    suspend fun setUserName(name: String) = update { it[Keys.userName] = name.trim() }

    suspend fun setAvatarStyle(style: AvatarStyle) = update { it[Keys.avatarStyle] = style.name }

    suspend fun setThemeMode(mode: ThemeMode) = update { it[Keys.themeMode] = mode.name }

    suspend fun setStartOfWeek(weekStart: WeekStart) = update { it[Keys.startOfWeek] = weekStart.name }

    suspend fun setDailyFocusTarget(minutes: Int) =
        update { it[Keys.dailyFocusTarget] = minutes.coerceIn(15, 960) }

    suspend fun setPomodoroDefaults(
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int,
        sessionsBeforeLongBreak: Int,
    ) = update {
        it[Keys.defaultFocus] = focusMinutes.coerceIn(1, 180)
        it[Keys.defaultShortBreak] = shortBreakMinutes.coerceIn(1, 60)
        it[Keys.defaultLongBreak] = longBreakMinutes.coerceIn(1, 120)
        it[Keys.defaultCycle] = sessionsBeforeLongBreak.coerceIn(2, 12)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        update { it[Keys.notificationsEnabled] = enabled }

    suspend fun setOnboardingComplete(complete: Boolean) =
        update { it[Keys.onboardingComplete] = complete }

    suspend fun setFocusCycleCount(count: Int) =
        update { it[Keys.focusCycleCount] = count.coerceAtLeast(0) }

    suspend fun replaceAll(preferences: UserPreferences) = update {
        it[Keys.userName] = preferences.userName
        it[Keys.avatarStyle] = preferences.avatarStyle.name
        it[Keys.themeMode] = preferences.themeMode.name
        it[Keys.startOfWeek] = preferences.startOfWeek.name
        it[Keys.dailyFocusTarget] = preferences.dailyFocusTargetMinutes
        it[Keys.defaultFocus] = preferences.defaultFocusMinutes
        it[Keys.defaultShortBreak] = preferences.defaultShortBreakMinutes
        it[Keys.defaultLongBreak] = preferences.defaultLongBreakMinutes
        it[Keys.defaultCycle] = preferences.defaultSessionsBeforeLongBreak
        it[Keys.notificationsEnabled] = preferences.notificationsEnabled
        it[Keys.onboardingComplete] = preferences.onboardingComplete
        it[Keys.focusCycleCount] = preferences.focusCycleCount
    }

    private suspend fun update(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.focusFlowDataStore.edit(block)
    }

    private inline fun <reified T : Enum<T>> String.toEnum(default: T): T =
        runCatching { enumValueOf<T>(this) }.getOrDefault(default)
}

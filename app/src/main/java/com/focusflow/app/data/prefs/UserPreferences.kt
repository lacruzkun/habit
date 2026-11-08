package com.focusflow.app.data.prefs

import com.focusflow.app.domain.model.AvatarStyle
import com.focusflow.app.domain.model.ThemeMode
import com.focusflow.app.domain.model.WeekStart
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val userName: String = "Friend",
    val avatarStyle: AvatarStyle = AvatarStyle.MOON,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val startOfWeek: WeekStart = WeekStart.MONDAY,
    val dailyFocusTargetMinutes: Int = 120,
    val defaultFocusMinutes: Int = 25,
    val defaultShortBreakMinutes: Int = 5,
    val defaultLongBreakMinutes: Int = 15,
    val defaultSessionsBeforeLongBreak: Int = 4,
    val notificationsEnabled: Boolean = true,
    val onboardingComplete: Boolean = false,
    val focusCycleCount: Int = 0,
)

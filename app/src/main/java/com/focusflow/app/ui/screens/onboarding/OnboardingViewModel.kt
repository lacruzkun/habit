package com.focusflow.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.domain.model.AvatarStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val name: String = "",
    val avatar: AvatarStyle = AvatarStyle.MOON,
    val dailyTargetMinutes: Int = 120,
    val saving: Boolean = false,
    val done: Boolean = false,
)

class OnboardingViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun setName(value: String) = _state.update { it.copy(name = value.take(40)) }

    fun setAvatar(style: AvatarStyle) = _state.update { it.copy(avatar = style) }

    fun setDailyTarget(minutes: Int) =
        _state.update { it.copy(dailyTargetMinutes = minutes.coerceIn(15, 480)) }

    fun finish() {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val current = _state.value
            val prefs = container.preferencesRepository.snapshot()
            container.preferencesRepository.setUserName(
                current.name.trim().ifBlank { "Friend" },
            )
            container.preferencesRepository.setAvatarStyle(current.avatar)
            container.preferencesRepository.setDailyFocusTarget(current.dailyTargetMinutes)
            if (prefs.defaultFocusMinutes == 25) {
                val focus = if (current.dailyTargetMinutes <= 60) 15 else 25
                container.preferencesRepository.setPomodoroDefaults(
                    focusMinutes = focus,
                    shortBreakMinutes = prefs.defaultShortBreakMinutes,
                    longBreakMinutes = prefs.defaultLongBreakMinutes,
                    sessionsBeforeLongBreak = prefs.defaultSessionsBeforeLongBreak,
                )
            }
            container.preferencesRepository.setOnboardingComplete(true)
            _state.update { it.copy(saving = false, done = true) }
        }
    }
}

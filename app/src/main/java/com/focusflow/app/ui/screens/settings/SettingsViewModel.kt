package com.focusflow.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.BuildConfig
import com.focusflow.app.data.prefs.UserPreferences
import com.focusflow.app.domain.model.AvatarStyle
import com.focusflow.app.domain.model.ThemeMode
import com.focusflow.app.domain.model.WeekStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val prefs: UserPreferences = UserPreferences(),
    val appVersion: String = BuildConfig.VERSION_NAME,
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<SettingsUiState> = container.preferencesRepository.preferences
        .map { SettingsUiState(prefs = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    fun setUserName(name: String) {
        viewModelScope.launch { container.preferencesRepository.setUserName(name) }
    }

    fun setAvatar(style: AvatarStyle) {
        viewModelScope.launch { container.preferencesRepository.setAvatarStyle(style) }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { container.preferencesRepository.setThemeMode(mode) }
    }

    fun setWeekStart(value: WeekStart) {
        viewModelScope.launch { container.preferencesRepository.setStartOfWeek(value) }
    }

    fun setDailyTarget(minutes: Int) {
        viewModelScope.launch { container.preferencesRepository.setDailyFocusTarget(minutes) }
    }

    fun setPomodoroDefaults(focus: Int, shortBreak: Int, longBreak: Int, sessions: Int) {
        viewModelScope.launch {
            container.preferencesRepository.setPomodoroDefaults(focus, shortBreak, longBreak, sessions)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesRepository.setNotificationsEnabled(enabled)
            container.reminderScheduler.sync()
        }
    }

    fun exportJson(uri: Uri) {
        viewModelScope.launch {
            _busy.value = true
            val result = container.exportManager.exportJson(uri)
            _busy.value = false
            result.onSuccess {
                _messages.emit("Backup saved (${it.tasks} tasks, ${it.sessions} sessions).")
            }.onFailure {
                _messages.emit(it.message ?: "Could not save the backup.")
            }
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            _busy.value = true
            val result = container.exportManager.exportCsv(uri)
            _busy.value = false
            result.onSuccess {
                _messages.emit("Exported $it focus sessions as CSV.")
            }.onFailure {
                _messages.emit(it.message ?: "Could not export the CSV.")
            }
        }
    }

    fun importJson(uri: Uri) {
        viewModelScope.launch {
            _busy.value = true
            container.timerManager.cancelActive()
            val result = container.exportManager.importJson(uri)
            _busy.value = false
            result.onSuccess {
                _messages.emit("Backup restored (${it.tasks} tasks, ${it.sessions} sessions).")
            }.onFailure {
                _messages.emit(it.message ?: "Could not restore the backup.")
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _busy.value = true
            container.timerManager.cancelActive()
            container.exportManager.clearAllData()
            _busy.value = false
            _messages.emit("All data cleared.")
        }
    }
}

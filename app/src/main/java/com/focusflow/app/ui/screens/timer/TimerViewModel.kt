package com.focusflow.app.ui.screens.timer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.timer.TimerConfig
import com.focusflow.app.domain.timer.TimerState
import com.focusflow.app.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimerTaskInfo(
    val task: TaskEntity? = null,
    val config: TimerConfig = TimerConfig(),
    val userName: String = "Friend",
)

class TimerViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val taskId: Long = savedStateHandle["taskId"] ?: -1L
    private val _taskInfo = MutableStateFlow(TimerTaskInfo())
    val taskInfo: StateFlow<TimerTaskInfo> = _taskInfo.asStateFlow()

    val timerState: StateFlow<TimerState?> = container.timerManager.state
    val tick: StateFlow<Long> = container.timerManager.tick
    val events = container.timerManager.events

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = container.preferencesRepository.snapshot()
            val task = if (taskId > 0) container.taskRepository.getTask(taskId) else null
            val config = if (task != null) {
                TimerConfig(
                    focusMinutes = task.focusMinutes,
                    shortBreakMinutes = task.shortBreakMinutes,
                    longBreakMinutes = task.longBreakMinutes,
                    sessionsBeforeLongBreak = task.sessionsBeforeLongBreak,
                )
            } else {
                TimerConfig(
                    focusMinutes = prefs.defaultFocusMinutes,
                    shortBreakMinutes = prefs.defaultShortBreakMinutes,
                    longBreakMinutes = prefs.defaultLongBreakMinutes,
                    sessionsBeforeLongBreak = prefs.defaultSessionsBeforeLongBreak,
                )
            }
            _taskInfo.value = TimerTaskInfo(
                task = task,
                config = config,
                userName = prefs.userName,
            )
            _loading.value = false
        }
    }

    fun startFocus() {
        val info = _taskInfo.value
        val task = info.task
        viewModelScope.launch {
            container.timerManager.startFocus(
                taskId = task?.id,
                taskName = task?.name ?: "Focus session",
                config = info.config,
            )
            NotificationHelper.startTimerService(container.appContext)
        }
    }

    fun toggle() {
        viewModelScope.launch { container.timerManager.toggle() }
    }

    fun endSession() {
        viewModelScope.launch { container.timerManager.endSession() }
    }

    fun skipBreak() {
        viewModelScope.launch { container.timerManager.skipBreak() }
    }

    fun setSelectedTask(task: TaskEntity?) {
        val info = _taskInfo.value
        val config = if (task != null) {
            TimerConfig(
                focusMinutes = task.focusMinutes,
                shortBreakMinutes = task.shortBreakMinutes,
                longBreakMinutes = task.longBreakMinutes,
                sessionsBeforeLongBreak = task.sessionsBeforeLongBreak,
            )
        } else {
            info.config
        }
        _taskInfo.value = info.copy(task = task, config = config)
    }

    fun logDistraction(
        activity: String,
        minutes: Int,
        note: String?,
        pauseTimer: Boolean,
    ) {
        viewModelScope.launch {
            val now = container.timeSource.wallMillis()
            container.distractionRepository.log(
                activity = activity,
                startedAt = now - minutes.coerceIn(1, 240) * 60_000L,
                endedAt = now,
                note = note,
            )
            if (pauseTimer && container.timerManager.state.value?.sessionType == SessionType.FOCUS) {
                container.timerManager.pause()
            }
        }
    }
}

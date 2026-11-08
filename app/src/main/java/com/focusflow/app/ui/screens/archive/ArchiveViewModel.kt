package com.focusflow.app.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val loading: Boolean = true,
    val tasks: List<TaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
)

class ArchiveViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<ArchiveUiState> = combine(
        container.taskRepository.archivedTasks,
        container.habitRepository.archivedHabits,
    ) { tasks, habits ->
        ArchiveUiState(loading = false, tasks = tasks, habits = habits)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArchiveUiState(),
    )

    fun restoreTask(id: Long) {
        viewModelScope.launch { container.taskRepository.restoreTask(id) }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { container.taskRepository.deleteTaskPermanently(id) }
    }

    fun restoreHabit(id: Long) {
        viewModelScope.launch { container.habitRepository.restoreHabit(id) }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch { container.habitRepository.deleteHabitPermanently(id) }
    }
}

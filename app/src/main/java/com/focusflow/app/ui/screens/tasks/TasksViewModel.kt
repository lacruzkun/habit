package com.focusflow.app.ui.screens.tasks

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.data.repository.matchesSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TaskFilter { ACTIVE, ALL, ARCHIVED }

@Immutable
data class TaskRowUi(
    val task: TaskEntity,
    val category: CategoryEntity?,
    val scheduledToday: Boolean,
)

@Immutable
data class TasksUiState(
    val loading: Boolean = true,
    val filter: TaskFilter = TaskFilter.ACTIVE,
    val query: String = "",
    val tasks: List<TaskRowUi> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val totalActive: Int = 0,
    val totalArchived: Int = 0,
)

class TasksViewModel(private val container: AppContainer) : ViewModel() {
    private val filter = MutableStateFlow(TaskFilter.ACTIVE)
    private val query = MutableStateFlow("")

    val state: StateFlow<TasksUiState> = combine(
        container.taskRepository.currentTasks,
        container.taskRepository.archivedTasks,
        container.taskRepository.categories,
        filter,
        query,
    ) { current, archived, categories, selectedFilter, search ->
        val categoryMap = categories.associateBy { it.id }
        val today = LocalDate.now()
        val source = when (selectedFilter) {
            TaskFilter.ACTIVE -> current
            TaskFilter.ARCHIVED -> archived
            TaskFilter.ALL -> current + archived
        }
        val filtered = source.filter {
            search.isBlank() || it.name.contains(search.trim(), ignoreCase = true) ||
                it.description.contains(search.trim(), ignoreCase = true)
        }
        TasksUiState(
            loading = false,
            filter = selectedFilter,
            query = search,
            tasks = filtered.map { task ->
                TaskRowUi(
                    task = task,
                    category = task.categoryId?.let { categoryMap[it] },
                    scheduledToday = task.archivedAt == null && task.matchesSchedule(today),
                )
            }.sortedWith(
                compareByDescending<TaskRowUi> { it.task.active }
                    .thenByDescending { it.task.priority.level }
                    .thenBy { it.task.sortOrder },
            ),
            categories = categories,
            totalActive = current.size,
            totalArchived = archived.size,
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState(),
    )

    fun setFilter(value: TaskFilter) {
        filter.value = value
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun toggleActive(taskId: Long, active: Boolean) {
        viewModelScope.launch { container.taskRepository.setActive(taskId, active) }
    }

    fun archive(taskId: Long) {
        viewModelScope.launch { container.taskRepository.archiveTask(taskId) }
    }

    fun restore(taskId: Long) {
        viewModelScope.launch { container.taskRepository.restoreTask(taskId) }
    }

    fun deletePermanently(taskId: Long) {
        viewModelScope.launch { container.taskRepository.deleteTaskPermanently(taskId) }
    }
}

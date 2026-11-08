package com.focusflow.app.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.CategoryEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryRowUi(
    val category: CategoryEntity,
    val usageCount: Int,
)

data class CategoriesUiState(
    val loading: Boolean = true,
    val categories: List<CategoryRowUi> = emptyList(),
)

class CategoriesViewModel(private val container: AppContainer) : ViewModel() {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    val state: StateFlow<CategoriesUiState> = combine(
        container.taskRepository.categories,
        container.taskRepository.currentTasks,
    ) { categories, tasks ->
        CategoriesUiState(
            loading = false,
            categories = categories.map { category ->
                CategoryRowUi(category, tasks.count { it.categoryId == category.id })
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun create(name: String, colorArgb: Long, iconKey: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            container.taskRepository.createCategory(name.trim(), colorArgb, iconKey)
        }
    }

    fun update(category: CategoryEntity) {
        if (category.name.isBlank()) return
        viewModelScope.launch { container.taskRepository.updateCategory(category) }
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch {
            val usage = container.taskRepository.categoryUsageCount(category.id)
            if (usage > 0) {
                _messages.emit("Move ${usage} task(s) to another category before deleting this one.")
            } else {
                container.taskRepository.deleteCategory(category.id)
            }
        }
    }
}

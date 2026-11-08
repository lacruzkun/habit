package com.focusflow.app.data.repository

import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.domain.time.TimeSource
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val database: FocusFlowDatabase,
    private val timeSource: TimeSource,
) {
    private val taskDao = database.taskDao()
    private val categoryDao = database.categoryDao()

    val currentTasks: Flow<List<TaskEntity>> = taskDao.observeCurrent()
    val allTasks: Flow<List<TaskEntity>> = taskDao.observeAll()
    val archivedTasks: Flow<List<TaskEntity>> = taskDao.observeArchived()
    val categories: Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeTask(id: Long): Flow<TaskEntity?> = taskDao.observeById(id)

    suspend fun getTask(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun getAllTasks(): List<TaskEntity> = taskDao.getAll()

    suspend fun seedDefaultCategories() {
        if (categoryDao.count() > 0) return
        val now = timeSource.wallMillis()
        categoryDao.insertAll(
            listOf(
                CategoryEntity(name = "Programming", colorArgb = 0xFF6C4CF1, iconKey = "code", createdAt = now),
                CategoryEntity(name = "Languages", colorArgb = 0xFFFF7A9E, iconKey = "translate", createdAt = now),
                CategoryEntity(name = "Education", colorArgb = 0xFF67B7F7, iconKey = "school", createdAt = now),
                CategoryEntity(name = "Personal Development", colorArgb = 0xFF45C98A, iconKey = "psychology", createdAt = now),
                CategoryEntity(name = "Health", colorArgb = 0xFFFFB25C, iconKey = "fitness", createdAt = now),
                CategoryEntity(name = "Creativity", colorArgb = 0xFFB07CFF, iconKey = "palette", createdAt = now),
                CategoryEntity(name = "Other", colorArgb = 0xFF8A79C9, iconKey = "category", createdAt = now),
            ),
        )
    }

    suspend fun createTask(task: TaskEntity): Long {
        val sortOrder = taskDao.maxSortOrder() + 1
        return taskDao.insert(
            task.copy(
                createdAt = if (task.createdAt == 0L) timeSource.wallMillis() else task.createdAt,
                sortOrder = sortOrder,
            ),
        )
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.update(task)
    }

    suspend fun archiveTask(id: Long) {
        val task = taskDao.getById(id) ?: return
        taskDao.update(task.copy(archivedAt = timeSource.wallMillis(), active = false))
    }

    suspend fun restoreTask(id: Long) {
        val task = taskDao.getById(id) ?: return
        taskDao.update(task.copy(archivedAt = null, active = true))
    }

    suspend fun setActive(id: Long, active: Boolean) {
        val task = taskDao.getById(id) ?: return
        taskDao.update(task.copy(active = active))
    }

    suspend fun deleteTaskPermanently(id: Long) {
        taskDao.deleteById(id)
    }

    suspend fun moveTask(id: Long, moveUp: Boolean): Boolean {
        val tasks = taskDao.getCurrent()
        val index = tasks.indexOfFirst { it.id == id }
        if (index < 0) return false
        val swapIndex = if (moveUp) index - 1 else index + 1
        if (swapIndex !in tasks.indices) return false
        val current = tasks[index]
        val other = tasks[swapIndex]
        val updated = tasks.toMutableList()
        updated[index] = current.copy(sortOrder = other.sortOrder)
        updated[swapIndex] = other.copy(sortOrder = current.sortOrder)
        taskDao.updateAll(updated)
        return true
    }

    suspend fun createCategory(name: String, colorArgb: Long, iconKey: String): Long =
        categoryDao.upsert(
            CategoryEntity(
                name = name.trim(),
                colorArgb = colorArgb,
                iconKey = iconKey,
                createdAt = timeSource.wallMillis(),
            ),
        )

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category.copy(name = category.name.trim()))
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.deleteById(id)
    }

    suspend fun getAllCategories(): List<CategoryEntity> = categoryDao.getAll()

    suspend fun categoryUsageCount(id: Long): Int = taskDao.getCurrent().count { it.categoryId == id }

    fun recommendedTask(
        tasks: List<TaskEntity>,
        focusSecondsByTask: Map<Long, Long>,
        today: java.time.LocalDate,
    ): TaskEntity? = tasks
        .filter { it.active && it.archivedAt == null && it.matchesSchedule(today) }
        .sortedWith(
            compareByDescending<TaskEntity> { it.priority.level }
                .thenBy { task ->
                    val done = focusSecondsByTask[task.id] ?: 0L
                    val target = task.dailyTargetMinutes.coerceAtLeast(1) * 60L
                    (done.toDouble() / target).coerceIn(0.0, 2.0)
                }
                .thenBy { it.sortOrder },
        )
        .firstOrNull()
}

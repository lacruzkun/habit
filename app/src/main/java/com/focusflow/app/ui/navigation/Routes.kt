package com.focusflow.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val TASK_EDITOR = "task_editor?taskId={taskId}"
    const val TASK_DETAIL = "task_detail/{taskId}"
    const val TIMER = "timer?taskId={taskId}"
    const val STATS = "stats"
    const val HABITS = "habits"
    const val HABIT_EDITOR = "habit_editor?habitId={habitId}"
    const val HABIT_DETAIL = "habit_detail/{habitId}"
    const val WEEKLY_REVIEW = "weekly_review"
    const val REMINDERS = "reminders"
    const val SETTINGS = "settings"
    const val ARCHIVE = "archive"
    const val CATEGORIES = "categories"
    const val MORE = "more"

    fun taskEditor(taskId: Long? = null): String =
        if (taskId == null) "task_editor?taskId=-1" else "task_editor?taskId=$taskId"

    fun taskDetail(taskId: Long): String = "task_detail/$taskId"

    fun timer(taskId: Long? = null): String =
        if (taskId == null) "timer?taskId=-1" else "timer?taskId=$taskId"

    fun habitEditor(habitId: Long? = null): String =
        if (habitId == null) "habit_editor?habitId=-1" else "habit_editor?habitId=$habitId"

    fun habitDetail(habitId: Long): String = "habit_detail/$habitId"
}

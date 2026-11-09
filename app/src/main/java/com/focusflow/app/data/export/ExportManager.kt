package com.focusflow.app.data.export

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.prefs.UserPreferencesRepository
import com.focusflow.app.data.repository.DistractionRepository
import com.focusflow.app.data.repository.HabitRepository
import com.focusflow.app.data.repository.ReflectionRepository
import com.focusflow.app.data.repository.ReminderRepository
import com.focusflow.app.data.repository.SessionRepository
import com.focusflow.app.data.repository.TaskRepository
import com.focusflow.app.domain.model.ReminderSource
import com.focusflow.app.domain.time.TimeSource
import com.focusflow.app.notifications.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ExportManager(
    private val context: Context,
    private val database: FocusFlowDatabase,
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    private val habitRepository: HabitRepository,
    private val distractionRepository: DistractionRepository,
    private val reflectionRepository: ReflectionRepository,
    private val reminderRepository: ReminderRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
    private val timeSource: TimeSource,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    suspend fun exportJson(uri: Uri): Result<BackupSummary> = runCatching {
        withContext(Dispatchers.IO) {
            val backup = collectBackup()
            context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { writer ->
                writer.write(json.encodeToString(backup))
            } ?: error("Could not open the selected file for writing.")
            BackupSummary(
                tasks = backup.tasks.size,
                sessions = backup.sessions.size,
                habits = backup.habits.size,
                completions = backup.completions.size,
                reminders = backup.reminders.size,
            )
        }
    }

    suspend fun exportCsv(uri: Uri): Result<Int> = runCatching {
        withContext(Dispatchers.IO) {
            val sessions = sessionRepository.allSessionsSnapshot()
            context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { writer ->
                writeSessionsCsv(writer, sessions)
            } ?: error("Could not open the selected file for writing.")
            sessions.size
        }
    }

    suspend fun importJson(uri: Uri): Result<BackupSummary> = runCatching {
        withContext(Dispatchers.IO) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                readBounded(it)
            } ?: error("Could not open the selected file for reading.")
            val backup = json.decodeFromString<BackupData>(text)
            require(backup.schemaVersion == SCHEMA_VERSION) {
                "Unsupported backup version ${backup.schemaVersion}."
            }
            val totalRecords = backup.categories.size + backup.tasks.size + backup.sessions.size +
                backup.segments.size + backup.habits.size + backup.completions.size +
                backup.distractions.size + backup.reflections.size + backup.reminders.size
            require(totalRecords <= 500_000) { "Backup contains too many records." }

            val previousReminders = reminderRepository.getAll()
            val previousTasks = taskRepository.getAllTasks()
            val previousHabits = habitRepository.getAllHabits()

            database.withTransaction {
                database.focusSessionDao().deleteAllSegments()
                database.focusSessionDao().deleteAllSessions()
                database.habitDao().deleteAllCompletions()
                database.habitDao().deleteAll()
                database.taskDao().deleteAll()
                database.categoryDao().deleteAll()
                database.distractionDao().deleteAll()
                database.reflectionDao().deleteAll()
                database.reminderDao().deleteAll()
                database.activeTimerDao().clear()

                database.categoryDao().insertAll(backup.categories)
                database.taskDao().insertAll(backup.tasks)
                database.focusSessionDao().insertSessions(backup.sessions)
                database.focusSessionDao().insertSegments(backup.segments)
                database.habitDao().insertAll(backup.habits)
                database.habitDao().insertCompletions(backup.completions)
                database.distractionDao().insertAll(backup.distractions)
                database.reflectionDao().insertAll(backup.reflections)
                database.reminderDao().insertAll(backup.reminders)
            }
            preferencesRepository.replaceAll(backup.preferences)
            previousReminders.forEach { reminderScheduler.cancel(ReminderSource.CUSTOM, it.id) }
            previousTasks.forEach { reminderScheduler.cancel(ReminderSource.TASK, it.id) }
            previousHabits.forEach { reminderScheduler.cancel(ReminderSource.HABIT, it.id) }
            reminderScheduler.sync()
            BackupSummary(
                tasks = backup.tasks.size,
                sessions = backup.sessions.size,
                habits = backup.habits.size,
                completions = backup.completions.size,
                reminders = backup.reminders.size,
            )
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        val previousReminders = reminderRepository.getAll()
        val previousTasks = taskRepository.getAllTasks()
        val previousHabits = habitRepository.getAllHabits()

        database.withTransaction {
            database.focusSessionDao().deleteAllSegments()
            database.focusSessionDao().deleteAllSessions()
            database.habitDao().deleteAllCompletions()
            database.habitDao().deleteAll()
            database.taskDao().deleteAll()
            database.categoryDao().deleteAll()
            database.distractionDao().deleteAll()
            database.reflectionDao().deleteAll()
            database.reminderDao().deleteAll()
            database.activeTimerDao().clear()
        }
        preferencesRepository.replaceAll(com.focusflow.app.data.prefs.UserPreferences())
        previousReminders.forEach { reminderScheduler.cancel(ReminderSource.CUSTOM, it.id) }
        previousTasks.forEach { reminderScheduler.cancel(ReminderSource.TASK, it.id) }
        previousHabits.forEach { reminderScheduler.cancel(ReminderSource.HABIT, it.id) }
        taskRepository.seedDefaultCategories()
        reminderRepository.seedDefault()
        reminderScheduler.sync()
    }

    private suspend fun collectBackup(): BackupData = BackupData(
        schemaVersion = SCHEMA_VERSION,
        exportedAt = timeSource.wallMillis(),
        preferences = preferencesRepository.snapshot(),
        categories = taskRepository.getAllCategories(),
        tasks = taskRepository.getAllTasks(),
        sessions = sessionRepository.allSessionsSnapshot(),
        segments = sessionRepository.allSegmentsSnapshot(),
        habits = habitRepository.getAllHabits(),
        completions = habitRepository.getAllCompletions(),
        distractions = distractionRepository.getAll(),
        reflections = reflectionRepository.getAll(),
        reminders = reminderRepository.getAll(),
    )

    private fun readBounded(reader: BufferedReader): String {
        val builder = StringBuilder()
        val buffer = CharArray(8192)
        while (true) {
            val read = reader.read(buffer)
            if (read < 0) break
            builder.append(buffer, 0, read)
            require(builder.length <= MAX_IMPORT_CHARS) { "Backup file is too large." }
        }
        return builder.toString()
    }

    private fun writeSessionsCsv(
        writer: BufferedWriter,
        sessions: List<com.focusflow.app.data.db.FocusSessionEntity>,
    ) {
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val zone = ZoneId.systemDefault()
        writer.appendLine(
            "session_id,task,type,status,started_at,ended_at,planned_minutes,focused_minutes,note",
        )
        sessions.sortedBy { it.startedAt }.forEach { session ->
            writer.append(
                listOf(
                    session.id.toString(),
                    session.taskNameSnapshot,
                    session.sessionType.name,
                    session.status.name,
                    Instant.ofEpochMilli(session.startedAt).atZone(zone).format(formatter),
                    Instant.ofEpochMilli(session.endedAt).atZone(zone).format(formatter),
                    (session.plannedSeconds / 60).toString(),
                    (session.focusSeconds / 60).toString(),
                    session.note.orEmpty(),
                ).joinToString(",") { csvEscape(it) },
            )
            writer.newLine()
        }
    }

    private fun csvEscape(value: String): String {
        if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    companion object {
        const val SCHEMA_VERSION = 1
        private const val MAX_IMPORT_CHARS = 25_000_000
        const val JSON_MIME = "application/json"
        const val CSV_MIME = "text/csv"
    }
}

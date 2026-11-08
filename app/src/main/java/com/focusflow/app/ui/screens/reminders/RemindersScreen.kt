package com.focusflow.app.ui.screens.reminders

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.ReminderEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.domain.model.ALL_DAYS_MASK
import com.focusflow.app.domain.time.formatClock
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.screens.tasks.formatTime
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RemindersScreen(
    state: RemindersUiState,
    onBack: () -> Unit,
    onCreate: (String, String, Int, Int) -> Unit,
    onUpdate: (ReminderEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onToggleCustom: (Long, Boolean) -> Unit,
    onToggleTask: (TaskEntity, Boolean) -> Unit,
    onTaskTime: (TaskEntity, Int) -> Unit,
    onToggleHabit: (HabitEntity, Boolean) -> Unit,
    onHabitTime: (HabitEntity, Int) -> Unit,
    onEnableNotifications: (Boolean) -> Unit,
) {
    val colors = LocalFocusColors.current
    val context = LocalContext.current
    var editing by remember { mutableStateOf<ReminderEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> onEnableNotifications(granted) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Reminders",
                subtitle = "Gentle nudges, never noise",
                onBack = onBack,
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { creating = true },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "New reminder",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                },
            )
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(
                        icon = Icons.Rounded.NotificationsActive,
                        tint = MaterialTheme.colorScheme.primary,
                        background = colors.lavender,
                        size = 42.dp,
                        iconSize = 20.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Notifications", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (state.enabled) "Enabled on this device" else "Paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { enabled ->
                            val needsPermission = enabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) != PackageManager.PERMISSION_GRANTED
                            if (needsPermission) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onEnableNotifications(enabled)
                            }
                        },
                    )
                }
                if (!state.enabled) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Reminders are scheduled even while paused; turn notifications back on to receive them.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.muted,
                    )
                }
            }
        }
        item {
            SectionHeader(
                title = "Custom reminders",
                subtitle = "${state.customs.size} configured",
            )
        }
        if (state.customs.isEmpty()) {
            item {
                Text(
                    "No custom reminders yet. Add one for a daily planning nudge.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        items(state.customs.size) { index ->
            val reminder = state.customs[index]
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(
                        icon = Icons.Rounded.Alarm,
                        tint = colors.peach,
                        background = colors.peach.copy(alpha = 0.15f),
                        size = 40.dp,
                        iconSize = 19.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(reminder.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${formatClock(reminder.timeMinutes)} · " +
                                daysLabel(reminder.daysMask),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                        if (reminder.message.isNotBlank()) {
                            Text(
                                reminder.message,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                            )
                        }
                    }
                    IconButton(onClick = { editing = reminder }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = colors.muted)
                    }
                    IconButton(onClick = { pendingDelete = reminder }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                    Switch(
                        checked = reminder.enabled,
                        onCheckedChange = { onToggleCustom(reminder.id, it) },
                    )
                }
            }
        }
        item {
            SectionHeader(
                title = "Task reminders",
                subtitle = "Sent on the days each task is scheduled",
            )
        }
        if (state.taskReminders.isEmpty()) {
            item {
                Text(
                    "No active tasks yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        items(state.taskReminders.size) { index ->
            val task = state.taskReminders[index]
            ReminderTargetRow(
                iconKey = task.iconKey,
                color = Color(task.colorArgb),
                title = task.name,
                subtitle = if (task.reminderEnabled) {
                    "Reminds at ${formatTime(task.reminderMinutes ?: 540)}"
                } else {
                    "No reminder"
                },
                enabled = task.reminderEnabled,
                onToggle = { onToggleTask(task, it) },
                onTimeClick = {
                    val minutes = task.reminderMinutes ?: 540
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onTaskTime(task, hour * 60 + minute) },
                        minutes / 60,
                        minutes % 60,
                        true,
                    ).show()
                },
            )
        }
        item {
            SectionHeader(
                title = "Habit reminders",
                subtitle = "A consistent cue each day or week",
            )
        }
        if (state.habitReminders.isEmpty()) {
            item {
                Text(
                    "No active habits yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        items(state.habitReminders.size) { index ->
            val habit = state.habitReminders[index]
            ReminderTargetRow(
                iconKey = habit.iconKey,
                color = Color(habit.colorArgb),
                title = habit.name,
                subtitle = if (habit.reminderEnabled) {
                    "Reminds at ${formatTime(habit.reminderMinutes ?: 480)}"
                } else {
                    "No reminder"
                },
                enabled = habit.reminderEnabled,
                onToggle = { onToggleHabit(habit, it) },
                onTimeClick = {
                    val minutes = habit.reminderMinutes ?: 480
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onHabitTime(habit, hour * 60 + minute) },
                        minutes / 60,
                        minutes % 60,
                        true,
                    ).show()
                },
            )
        }
    }

    if (creating || editing != null) {
        ReminderDialog(
            existing = editing,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { title, message, time, days ->
                val current = editing
                if (current == null) {
                    onCreate(title, message, time, days)
                } else {
                    onUpdate(
                        current.copy(
                            title = title,
                            message = message,
                            timeMinutes = time,
                            daysMask = days,
                        ),
                    )
                }
                creating = false
                editing = null
            },
        )
    }

    ConfirmDialog(
        visible = pendingDelete != null,
        title = "Delete reminder?",
        message = "This reminder will stop appearing on your schedule.",
        onConfirm = {
            pendingDelete?.let { onDelete(it.id) }
            pendingDelete = null
        },
        onDismiss = { pendingDelete = null },
    )
}

@Composable
private fun ReminderTargetRow(
    iconKey: String,
    color: Color,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
) {
    val colors = LocalFocusColors.current
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(
                icon = IconCatalog.icon(iconKey),
                tint = color,
                background = color.copy(alpha = 0.13f),
                size = 40.dp,
                iconSize = 19.dp,
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            if (enabled) {
                Surface(
                    onClick = onTimeClick,
                    shape = RoundedCornerShape(50),
                    color = colors.lavender,
                ) {
                    Text(
                        "Set time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ReminderDialog(
    existing: ReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int) -> Unit,
) {
    val context = LocalContext.current
    var title by remember(existing?.id) { mutableStateOf(existing?.title ?: "") }
    var message by remember(existing?.id) { mutableStateOf(existing?.message ?: "") }
    var time by remember(existing?.id) { mutableStateOf(existing?.timeMinutes ?: (21 * 60)) }
    var days by remember(existing?.id) { mutableStateOf(existing?.daysMask ?: ALL_DAYS_MASK) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New reminder" else "Edit reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(80) },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it.take(200) },
                    label = { Text("Message (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> time = hour * 60 + minute },
                            time / 60,
                            time % 60,
                            true,
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = LocalFocusColors.current.background,
                ) {
                    Text(
                        "Time: ${formatClock(time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val selected = days and (1 shl (day.value - 1)) != 0
                        Surface(
                            onClick = {
                                val bit = 1 shl (day.value - 1)
                                val next = days xor bit
                                days = if (next == 0) days else next
                            },
                            shape = CircleShape,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalFocusColors.current.background
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        LocalFocusColors.current.muted
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), message.trim(), time, days) },
                enabled = title.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun daysLabel(mask: Int): String {
    val days = DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }
    return when {
        days.size == 7 -> "Every day"
        days.size == 5 && days.none { it.value > 5 } -> "Weekdays"
        days.isEmpty() -> "No days"
        else -> days.joinToString(", ") {
            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }
}

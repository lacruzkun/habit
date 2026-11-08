package com.focusflow.app.ui.screens.tasks

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.TaskPriority
import com.focusflow.app.domain.time.formatDurationFromMinutes
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.PrimaryButton
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val taskColors = listOf(
    0xFF6C4CF1, 0xFF3FCB92, 0xFFFFB877, 0xFFFF7A9E, 0xFF67B7F7, 0xFFE5484D,
)

@Composable
fun TaskEditorScreen(
    state: TaskEditorUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    onDailyTargetChange: (Int) -> Unit,
    onWeeklyTargetChange: (Int) -> Unit,
    onYearlyTargetChange: (Int?) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onAllDays: () -> Unit,
    onWeekdays: () -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (Int) -> Unit,
    onFocusMinutesChange: (Int) -> Unit,
    onShortBreakChange: (Int) -> Unit,
    onLongBreakChange: (Int) -> Unit,
    onSessionsChange: (Int) -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalFocusColors.current
    var showDelete by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = if (state.isNew) "New task" else "Edit task",
            subtitle = if (state.archived) "Archived task" else null,
            onBack = onBack,
            actions = {
                if (!state.isNew) {
                    if (state.archived) {
                        IconButton(onClick = onRestore) {
                            Icon(Icons.Rounded.Restore, contentDescription = "Restore", tint = colors.muted)
                        }
                    } else {
                        IconButton(onClick = { showArchive = true }) {
                            Icon(Icons.Rounded.Archive, contentDescription = "Archive", tint = colors.muted)
                        }
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    isError = state.error != null && state.name.isBlank(),
                )
            }
            item {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                )
            }
            item {
                SectionHeader(title = "Category")
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        ChoiceChip(
                            label = "None",
                            selected = state.categoryId == null,
                            onClick = { onCategoryChange(null) },
                        )
                    }
                    items(state.categories) { category ->
                        ChoiceChip(
                            label = category.name,
                            selected = state.categoryId == category.id,
                            color = Color(category.colorArgb),
                            onClick = { onCategoryChange(category.id) },
                        )
                    }
                }
            }
            item {
                SectionHeader(title = "Icon & color")
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(IconCatalog.categoryChoices) { key ->
                        val selected = state.iconKey == key
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) colors.lavender else colors.card,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else colors.grid,
                            ),
                            onClick = { onIconChange(key) },
                            modifier = Modifier.size(46.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    IconCatalog.icon(key),
                                    contentDescription = key,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else colors.muted,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    taskColors.forEach { color ->
                        val selected = state.colorArgb == color
                        Box(
                            modifier = Modifier
                                .size(if (selected) 32.dp else 28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { onColorChange(color) }
                                .then(
                                    if (selected) {
                                        Modifier.padding(4.dp)
                                    } else {
                                        Modifier
                                    },
                                ),
                        )
                    }
                }
            }
            item {
                SectionHeader(title = "Priority")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { priority ->
                        ChoiceChip(
                            label = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = state.priority == priority,
                            onClick = { onPriorityChange(priority) },
                        )
                    }
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Active", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Inactive tasks stay out of your daily plan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Switch(checked = state.active, onCheckedChange = onActiveChange)
                    }
                }
            }
            item {
                SectionHeader(title = "Schedule")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val selected = state.scheduledDaysMask and (1 shl (day.value - 1)) != 0
                        Surface(
                            onClick = { onToggleDay(day) },
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.primary else colors.card,
                            modifier = Modifier.weight(1f).height(42.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else colors.muted,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(label = "Every day", selected = false, onClick = onAllDays)
                    ChoiceChip(label = "Weekdays", selected = false, onClick = onWeekdays)
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Daily target", style = MaterialTheme.typography.titleSmall)
                            Text(
                                formatDurationFromMinutes(state.dailyTargetMinutes),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Text(
                            "${state.dailyTargetMinutes}m",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Slider(
                        value = state.dailyTargetMinutes.toFloat(),
                        onValueChange = { onDailyTargetChange(it.toInt()) },
                        valueRange = 15f..480f,
                        steps = 30,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Weekly target", style = MaterialTheme.typography.titleSmall)
                            Text(
                                formatDurationFromMinutes(state.weeklyTargetMinutes),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Text(
                            "${state.weeklyTargetMinutes}m",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Slider(
                        value = state.weeklyTargetMinutes.toFloat(),
                        onValueChange = { onWeeklyTargetChange(it.toInt()) },
                        valueRange = 30f..3360f,
                        steps = 20,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.yearlyTargetMinutes?.toString().orEmpty(),
                        onValueChange = { text ->
                            val parsed = text.filter { it.isDigit() }
                            onYearlyTargetChange(
                                parsed.toIntOrNull()?.takeIf { it > 0 },
                            )
                        },
                        label = { Text("Yearly target (minutes, optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    )
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Task reminder", style = MaterialTheme.typography.titleSmall)
                            Text(
                                state.reminderMinutes?.let(::formatTime) ?: "No reminder time",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = onReminderEnabledChange,
                        )
                    }
                    if (state.reminderEnabled) {
                        Spacer(Modifier.height(10.dp))
                        ChoiceChip(
                            label = "Reminder at ${formatTime(state.reminderMinutes ?: 540)}",
                            selected = true,
                            onClick = {
                                val minutes = state.reminderMinutes ?: 540
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute -> onReminderTimeChange(hour * 60 + minute) },
                                    minutes / 60,
                                    minutes % 60,
                                    true,
                                ).show()
                            },
                        )
                    }
                }
            }
            item {
                SectionHeader(title = "Pomodoro for this task")
                Spacer(Modifier.height(8.dp))
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    StepperRow(
                        label = "Focus",
                        value = state.focusMinutes,
                        suffix = "min",
                        onChange = onFocusMinutesChange,
                    )
                    StepperRow(
                        label = "Short break",
                        value = state.shortBreakMinutes,
                        suffix = "min",
                        onChange = onShortBreakChange,
                    )
                    StepperRow(
                        label = "Long break",
                        value = state.longBreakMinutes,
                        suffix = "min",
                        onChange = onLongBreakChange,
                    )
                    StepperRow(
                        label = "Sessions before long break",
                        value = state.sessionsBeforeLongBreak,
                        suffix = "",
                        onChange = onSessionsChange,
                    )
                }
            }
            if (state.error != null) {
                item {
                    Text(
                        state.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            PrimaryButton(
                text = if (state.isNew) "Create task" else "Save changes",
                enabled = !state.loading,
                onClick = onSave,
            )
        }
    }

    ConfirmDialog(
        visible = showArchive,
        title = "Archive task?",
        message = "You can restore it later from the archive. Focus history is kept.",
        confirmLabel = "Archive",
        destructive = false,
        onConfirm = {
            showArchive = false
            onArchive()
        },
        onDismiss = { showArchive = false },
    )
    ConfirmDialog(
        visible = showDelete,
        title = "Delete task permanently?",
        message = "This removes the task. Focus sessions remain in your history.",
        onConfirm = {
            showDelete = false
            onDelete()
        },
        onDismiss = { showDelete = false },
    )
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color? = null,
) {
    val colors = LocalFocusColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = when {
            selected -> (color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.16f)
            else -> colors.card
        },
        border = BorderStroke(
            1.dp,
            when {
                selected -> color ?: MaterialTheme.colorScheme.primary
                else -> colors.grid
            },
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                color ?: MaterialTheme.colorScheme.primary
            } else {
                colors.muted
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    suffix: String,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        StepperButton("−") { onChange(value - 1) }
        Text(
            text = "$value$suffix",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .width(64.dp)
                .padding(horizontal = 8.dp),
        )
        StepperButton("+") { onChange(value + 1) }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = LocalFocusColors.current.lavender,
        modifier = Modifier.size(30.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

internal fun formatTime(minutes: Int): String {
    val hour = (minutes / 60) % 24
    val minute = minutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

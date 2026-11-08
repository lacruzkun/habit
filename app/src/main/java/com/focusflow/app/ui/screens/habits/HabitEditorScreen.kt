package com.focusflow.app.ui.screens.habits

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.PrimaryButton
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.screens.tasks.formatTime
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val habitColors = listOf(
    0xFF45C98A, 0xFF6C4CF1, 0xFFFFB877, 0xFFFF7A9E, 0xFF67B7F7, 0xFFE5484D,
)

private val unitChoices = listOf("times", "glasses", "pages", "minutes", "km")

@Composable
fun HabitEditorScreen(
    state: HabitEditorUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    onFrequencyChange: (HabitFrequency) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onAllDays: () -> Unit,
    onWeeklyTargetChange: (Int) -> Unit,
    onTargetValueChange: (Double) -> Unit,
    onUnitChange: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (Int) -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalFocusColors.current
    val context = LocalContext.current
    var showDelete by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = if (state.isNew) "New habit" else "Edit habit",
            subtitle = if (state.archived) "Archived habit" else null,
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
                    label = { Text("Habit name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    isError = state.error != null && state.name.isBlank(),
                )
            }
            item {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Why this habit? (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                )
            }
            item {
                SectionHeader(title = "Icon & color")
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(IconCatalog.habitChoices) { key ->
                        val selected = state.iconKey == key
                        Surface(
                            onClick = { onIconChange(key) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) colors.lavender else colors.card,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else colors.grid,
                            ),
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
                    habitColors.forEach { color ->
                        val selected = state.colorArgb == color
                        Box(
                            modifier = Modifier
                                .size(if (selected) 32.dp else 28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { onColorChange(color) },
                        )
                    }
                }
            }
            item {
                SectionHeader(title = "Frequency")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(
                        label = "Every day",
                        selected = state.frequency == HabitFrequency.DAILY,
                        onClick = { onFrequencyChange(HabitFrequency.DAILY) },
                    )
                    ChoiceChip(
                        label = "Specific days",
                        selected = state.frequency == HabitFrequency.WEEKLY_DAYS,
                        onClick = { onFrequencyChange(HabitFrequency.WEEKLY_DAYS) },
                    )
                    ChoiceChip(
                        label = "Times per week",
                        selected = state.frequency == HabitFrequency.WEEKLY_COUNT,
                        onClick = { onFrequencyChange(HabitFrequency.WEEKLY_COUNT) },
                    )
                }
                if (state.frequency == HabitFrequency.WEEKLY_DAYS) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val selected = state.daysMask and (1 shl (day.value - 1)) != 0
                            Surface(
                                onClick = { onToggleDay(day) },
                                shape = CircleShape,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    colors.card
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            colors.muted
                                        },
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ChoiceChip(label = "Select all days", selected = false, onClick = onAllDays)
                }
                if (state.frequency == HabitFrequency.WEEKLY_COUNT) {
                    Spacer(Modifier.height(10.dp))
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Weekly target: ${state.weeklyTargetCount} times",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            (1..7).forEach { count ->
                                val selected = state.weeklyTargetCount == count
                                Surface(
                                    onClick = { onWeeklyTargetChange(count) },
                                    shape = CircleShape,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        colors.card
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "$count",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                colors.muted
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "Daily target")
                Spacer(Modifier.height(8.dp))
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = if (state.targetValue % 1.0 == 0.0) {
                                state.targetValue.toInt().toString()
                            } else {
                                state.targetValue.toString()
                            },
                            onValueChange = { text ->
                                text.filter { it.isDigit() || it == '.' }
                                    .toDoubleOrNull()
                                    ?.let(onTargetValueChange)
                            },
                            label = { Text("Amount") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                        Spacer(Modifier.size(10.dp))
                        OutlinedTextField(
                            value = state.unit,
                            onValueChange = onUnitChange,
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(unitChoices) { unit ->
                            ChoiceChip(
                                label = unit,
                                selected = state.unit == unit,
                                onClick = { onUnitChange(unit) },
                            )
                        }
                    }
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Habit reminder", style = MaterialTheme.typography.titleSmall)
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
                            label = "Remind at ${formatTime(state.reminderMinutes ?: 480)}",
                            selected = true,
                            onClick = {
                                val minutes = state.reminderMinutes ?: 480
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
                text = if (state.isNew) "Create habit" else "Save changes",
                enabled = !state.loading,
                onClick = onSave,
            )
        }
    }

    ConfirmDialog(
        visible = showArchive,
        title = "Archive habit?",
        message = "The habit and its history stay available in the archive.",
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
        title = "Delete habit permanently?",
        message = "This removes the habit and all of its completion history.",
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
) {
    val colors = LocalFocusColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) colors.lavender else colors.card,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else colors.grid,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else colors.muted,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

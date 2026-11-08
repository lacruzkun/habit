package com.focusflow.app.ui.screens.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors

@Composable
fun ArchiveScreen(
    state: ArchiveUiState,
    onBack: () -> Unit,
    onRestoreTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onRestoreHabit: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
) {
    val colors = LocalFocusColors.current
    var pendingTaskDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var pendingHabitDelete by remember { mutableStateOf<HabitEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(
                title = "Archive",
                subtitle = "${state.tasks.size} tasks · ${state.habits.size} habits",
                onBack = onBack,
            )
        }
        if (state.tasks.isEmpty() && state.habits.isEmpty() && !state.loading) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Archive,
                    title = "Nothing archived",
                    message = "Tasks and habits you archive will wait here until you restore them.",
                )
            }
        }
        if (state.tasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Tasks")
            }
            items(state.tasks.size) { index ->
                val task = state.tasks[index]
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(
                            icon = IconCatalog.icon(task.iconKey),
                            tint = Color(task.colorArgb),
                            background = Color(task.colorArgb).copy(alpha = 0.13f),
                            size = 40.dp,
                            iconSize = 19.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(task.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${task.dailyTargetMinutes}m daily target",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                            )
                        }
                        Surface(
                            onClick = { onRestoreTask(task.id) },
                            shape = RoundedCornerShape(50),
                            color = colors.lavender,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Restore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(14.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Restore",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        IconButton(onClick = { pendingTaskDelete = task }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
        if (state.habits.isNotEmpty()) {
            item {
                SectionHeader(title = "Habits")
            }
            items(state.habits.size) { index ->
                val habit = state.habits[index]
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(
                            icon = IconCatalog.icon(habit.iconKey),
                            tint = Color(habit.colorArgb),
                            background = Color(habit.colorArgb).copy(alpha = 0.13f),
                            size = 40.dp,
                            iconSize = 19.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Every ${habit.unit} goal of ${habit.targetValue.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                            )
                        }
                        Surface(
                            onClick = { onRestoreHabit(habit.id) },
                            shape = RoundedCornerShape(50),
                            color = colors.lavender,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Restore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(14.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Restore",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        IconButton(onClick = { pendingHabitDelete = habit }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    ConfirmDialog(
        visible = pendingTaskDelete != null,
        title = "Delete task permanently?",
        message = "The task is removed from the archive. Focus history is kept.",
        onConfirm = {
            pendingTaskDelete?.let { onDeleteTask(it.id) }
            pendingTaskDelete = null
        },
        onDismiss = { pendingTaskDelete = null },
    )
    ConfirmDialog(
        visible = pendingHabitDelete != null,
        title = "Delete habit permanently?",
        message = "The habit and its completion history are removed.",
        onConfirm = {
            pendingHabitDelete?.let { onDeleteHabit(it.id) }
            pendingHabitDelete = null
        },
        onDismiss = { pendingHabitDelete = null },
    )
}

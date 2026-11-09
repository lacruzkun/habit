package com.focusflow.app.ui.screens.tasks

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.TaskPriority
import com.focusflow.app.domain.model.maskDays
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.PillTabs
import com.focusflow.app.ui.components.RoundAddButton
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TasksScreen(
    state: TasksUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onStartFocus: (Long) -> Unit,
    onFilterChange: (TaskFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onArchive: (Long) -> Unit,
    onRestore: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val colors = LocalFocusColors.current
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = "Tasks",
            subtitle = "${state.totalActive} active · ${state.totalArchived} archived",
            onBack = onBack,
            actions = {
                RoundAddButton(onClick = onAdd, contentDescription = "New task")
            },
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = { Text("Search tasks") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )
        Spacer(Modifier.height(12.dp))
        PillTabs(
            options = TaskFilter.entries.toList(),
            selected = state.filter,
            label = {
                when (it) {
                    TaskFilter.ACTIVE -> "Active"
                    TaskFilter.ALL -> "All"
                    TaskFilter.ARCHIVED -> "Archived"
                }
            },
            onSelect = onFilterChange,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(10.dp))

        if (state.tasks.isEmpty() && !state.loading) {
            EmptyState(
                icon = Icons.Rounded.CheckCircle,
                title = if (state.query.isNotBlank()) "No matching tasks" else "No tasks here yet",
                message = if (state.query.isNotBlank()) {
                    "Try a different search or filter."
                } else {
                    "Create your first task and give it a daily focus target."
                },
                actionLabel = if (state.query.isBlank()) "Create task" else null,
                onAction = onAdd,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.tasks, key = { it.task.id }) { row ->
                    val task = row.task
                    var menuOpen by remember(task.id) { mutableStateOf(false) }
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenTask(task.id) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBubble(
                                icon = IconCatalog.icon(task.iconKey),
                                tint = Color(task.colorArgb),
                                background = Color(task.colorArgb).copy(alpha = 0.13f),
                                size = 44.dp,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = task.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (task.active) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            colors.muted
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (task.priority != TaskPriority.LOW) {
                                        PriorityBadge(task.priority)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = buildString {
                                        row.category?.let { append(it.name) }
                                        if (row.category != null) append(" · ")
                                        append("${task.dailyTargetMinutes}m daily")
                                        if (row.scheduledToday) append(" · today")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (task.scheduledDaysMask != 0 && task.scheduledDaysMask != 0x7F) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = maskDays(task.scheduledDaysMask).joinToString(", ") {
                                            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.muted,
                                        maxLines = 1,
                                    )
                                }
                            }
                            if (task.archivedAt == null) {
                                Surface(
                                    onClick = { onStartFocus(task.id) },
                                    shape = CircleShape,
                                    color = colors.lavender,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Focus",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(
                                        Icons.Rounded.MoreVert,
                                        contentDescription = "Task menu",
                                        tint = colors.muted,
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuOpen,
                                    onDismissRequest = { menuOpen = false },
                                ) {
                                    if (task.archivedAt == null) {
                                        DropdownMenuItem(
                                            text = { Text(if (task.active) "Mark inactive" else "Mark active") },
                                            leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                                            onClick = {
                                                menuOpen = false
                                                onToggleActive(task.id, !task.active)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Archive") },
                                            leadingIcon = { Icon(Icons.Rounded.Archive, contentDescription = null) },
                                            onClick = {
                                                menuOpen = false
                                                onArchive(task.id)
                                            },
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Restore") },
                                            leadingIcon = { Icon(Icons.Rounded.Restore, contentDescription = null) },
                                            onClick = {
                                                menuOpen = false
                                                onRestore(task.id)
                                            },
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Delete permanently",
                                                color = MaterialTheme.colorScheme.error,
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        },
                                        onClick = {
                                            menuOpen = false
                                            pendingDeleteId = task.id
                                            pendingDeleteName = task.name
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        ConfirmDialog(
            visible = pendingDeleteId != null,
            title = "Delete task permanently?",
            message = "\"$pendingDeleteName\" will be removed. Focus history is kept.",
            confirmLabel = "Delete",
            onConfirm = {
                pendingDeleteId?.let(onDelete)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null },
        )
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val color = when (priority) {
        TaskPriority.URGENT -> MaterialTheme.colorScheme.error
        TaskPriority.HIGH -> MaterialTheme.colorScheme.secondary
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primary
        TaskPriority.LOW -> LocalFocusColors.current.muted
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

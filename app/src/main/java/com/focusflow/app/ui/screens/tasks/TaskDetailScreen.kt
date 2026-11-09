package com.focusflow.app.ui.screens.tasks

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.stats.FocusSummary
import com.focusflow.app.domain.time.formatDurationOrZero
import com.focusflow.app.ui.components.ChartLabels
import com.focusflow.app.ui.components.LineChart
import com.focusflow.app.ui.components.PillTabs
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.HorizontalDivider
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.PrimaryButton
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.components.StatTile
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskDetailScreen(
    state: TaskDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onStartFocus: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onDeleted: () -> Unit,
) {
    val colors = LocalFocusColors.current
    val task = state.task
    var showDelete by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }
    var range by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = "Task Analytics",
            onBack = onBack,
            actions = {
                if (task != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = colors.muted)
                    }
                    if (task.archivedAt != null) {
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

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = colors.muted)
            }

            task == null -> EmptyState(
                icon = Icons.Rounded.Timer,
                title = "Task not found",
                message = state.error ?: "This task may have been deleted.",
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBubble(
                                icon = IconCatalog.icon(task.iconKey),
                                tint = Color(task.colorArgb),
                                background = Color(task.colorArgb).copy(alpha = 0.13f),
                                size = 52.dp,
                                iconSize = 24.dp,
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    task.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "${task.dailyTargetMinutes}m daily · ${task.weeklyTargetMinutes}m weekly",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.muted,
                                )
                            }
                            Surface(
                                onClick = onStartFocus,
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Focus",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                        if (task.description.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (task.active) "Active" else "Inactive",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.muted,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = task.active,
                                onCheckedChange = onToggleActive,
                            )
                        }
                    }
                }

                item {
                    PillTabs(
                        options = listOf(0, 1),
                        selected = range,
                        label = { if (it == 0) "Week" else "Month" },
                        onSelect = { range = it },
                    )
                }

                item {
                    val summary = if (range == 0) state.week else state.month
                    val daily = summary?.daily.orEmpty()
                    val values = daily.map { it.seconds }
                    val peak = values.indices.maxByOrNull { values[it] } ?: -1
                    val dayFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    formatDurationOrZero(summary?.totalSeconds ?: 0),
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                                Text("Total Time", style = MaterialTheme.typography.labelSmall, color = colors.muted)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${summary?.completedSessions ?: 0}",
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                                Text("Pomodoros", style = MaterialTheme.typography.labelSmall, color = colors.muted)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        LineChart(
                            values = values,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            highlightIndex = if (peak >= 0 && values[peak] > 0) peak else -1,
                            tooltip = if (peak >= 0 && values[peak] > 0) formatDurationOrZero(values[peak]) else null,
                        )
                        Spacer(Modifier.height(6.dp))
                        if (range == 0 && daily.size in 1..7) {
                            ChartLabels(
                                labels = daily.map {
                                    it.date.dayOfWeek.getDisplayName(
                                        java.time.format.TextStyle.SHORT,
                                        Locale.getDefault(),
                                    )
                                },
                            )
                        } else if (daily.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    daily.first().date.format(dayFmt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.muted,
                                )
                                Text(
                                    daily.last().date.format(dayFmt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.muted,
                                )
                            }
                        }
                    }
                }

                item {
                    val summary = if (range == 0) state.week else state.month
                    val best = summary?.bestDay?.takeIf { it.seconds > 0 }
                    SoftCard(modifier = Modifier.fillMaxWidth(), color = colors.lavender) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFFB020),
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Insights", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (best != null) {
                                        "Your best day was " +
                                            best.date.dayOfWeek.getDisplayName(
                                                java.time.format.TextStyle.FULL,
                                                Locale.getDefault(),
                                            ) +
                                            " with ${formatDurationOrZero(best.seconds)} of focus. " +
                                            "You average ${formatDurationOrZero(summary?.averageDailySeconds ?: 0)} per day."
                                    } else {
                                        "No focus recorded for this task in this period yet."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.muted,
                                )
                            }
                        }
                    }
                }

                item {
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Pomodoro settings", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(10.dp))
                        SettingLine("Focus session", "${task.focusMinutes} min")
                        SettingLine("Short break", "${task.shortBreakMinutes} min")
                        SettingLine("Long break", "${task.longBreakMinutes} min")
                        SettingLine("Long break after", "${task.sessionsBeforeLongBreak} sessions")
                        SettingLine(
                            "Reminder",
                            if (task.reminderEnabled) {
                                formatTime(task.reminderMinutes ?: 540)
                            } else {
                                "Off"
                            },
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = "Recent sessions",
                        subtitle = "${state.recentSessions.size} shown",
                    )
                }
                if (state.recentSessions.isEmpty()) {
                    item {
                        Text(
                            "No focus sessions yet. Start the timer to build momentum.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.muted,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                } else {
                    items(state.recentSessions.size) { index ->
                        val session = state.recentSessions[index]
                        SoftCard(modifier = Modifier.fillMaxWidth(), onClick = null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconBubble(
                                    icon = Icons.Rounded.Timer,
                                    tint = MaterialTheme.colorScheme.primary,
                                    background = colors.lavender,
                                    size = 36.dp,
                                    iconSize = 17.dp,
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        formatSessionDate(session.startedAt),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        session.status.name.lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.muted,
                                    )
                                }
                                Text(
                                    formatDurationOrZero(session.focusSeconds.toLong()),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    ConfirmDialog(
        visible = showArchive,
        title = "Archive task?",
        message = "The task moves to the archive and stops appearing in your plan.",
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
        message = "The task will be removed. Focus history is kept.",
        onConfirm = {
            showDelete = false
            onDelete()
            onDeleted()
        },
        onDismiss = { showDelete = false },
    )
}

@Composable
private fun SummaryTile(label: String, summary: FocusSummary?, modifier: Modifier = Modifier) {
    val colors = LocalFocusColors.current
    SoftCard(modifier = modifier, contentPadding = 12.dp) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.muted)
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatDurationOrZero(summary?.totalSeconds ?: 0),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${summary?.completedSessions ?: 0} sessions",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
        )
    }
}

@Composable
private fun SettingLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalFocusColors.current.muted,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatSessionDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.getDefault()))

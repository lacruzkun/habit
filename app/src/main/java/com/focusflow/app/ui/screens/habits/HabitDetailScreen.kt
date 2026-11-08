package com.focusflow.app.ui.screens.habits

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.time.epochDayToDate
import com.focusflow.app.domain.time.formatDateShort
import com.focusflow.app.domain.time.percentLabel
import com.focusflow.app.ui.components.CheckDot
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.HabitHeatmap
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.components.StatTile
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.LocalDate

@Composable
fun HabitDetailScreen(
    state: HabitDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleToday: () -> Unit,
    onSetValue: (Double) -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onDeleted: () -> Unit,
) {
    val colors = LocalFocusColors.current
    val habit = state.habit
    var showDelete by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = habit?.name ?: "Habit",
            onBack = onBack,
            actions = {
                if (habit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = colors.muted)
                    }
                    if (habit.archivedAt != null) {
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

            habit == null -> EmptyState(
                icon = Icons.Rounded.LocalFireDepartment,
                title = "Habit not found",
                message = state.error ?: "This habit may have been deleted.",
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    GradientCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = listOf(
                            Color(habit.colorArgb).copy(alpha = 0.35f),
                            Color(habit.colorArgb).copy(alpha = 0.12f),
                        ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBubble(
                                icon = IconCatalog.icon(habit.iconKey),
                                tint = Color(habit.colorArgb),
                                background = Color.White.copy(alpha = 0.6f),
                                size = 54.dp,
                                iconSize = 26.dp,
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    habit.name,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    "${state.stats30?.currentStreak ?: 0} day streak · " +
                                        "best ${state.statsAll?.longestStreak ?: 0}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.muted,
                                )
                            }
                            IconBubble(
                                icon = Icons.Rounded.LocalFireDepartment,
                                tint = colors.peach,
                                background = Color.White.copy(alpha = 0.55f),
                                size = 40.dp,
                                iconSize = 20.dp,
                            )
                        }
                        if (habit.description.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                habit.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "This week",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.muted,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${state.weeklyCount}/${state.weeklyTarget}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = {
                                (state.weeklyCount.toFloat() / state.weeklyTarget.toFloat())
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(habit.colorArgb),
                            trackColor = Color.White.copy(alpha = 0.5f),
                        )
                    }
                }
                item {
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Today", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    if (state.todayCompleted) {
                                        "Completed — nice work."
                                    } else {
                                        "Target: ${formatTarget(habit.targetValue)} ${habit.unit}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.muted,
                                )
                            }
                            if (habit.targetValue <= 1.0) {
                                CheckDot(
                                    checked = state.todayCompleted,
                                    onClick = onToggleToday,
                                )
                            }
                        }
                        if (habit.targetValue > 1.0) {
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    onClick = {
                                        onSetValue((state.todayValue - 1).coerceAtLeast(0.0))
                                    },
                                    shape = CircleShape,
                                    color = colors.lavender,
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("−", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                Text(
                                    "${formatTarget(state.todayValue)} / " +
                                        "${formatTarget(habit.targetValue)} ${habit.unit}",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp),
                                )
                                Surface(
                                    onClick = {
                                        onSetValue((state.todayValue + 1).coerceAtMost(habit.targetValue))
                                    },
                                    shape = CircleShape,
                                    color = Color(habit.colorArgb),
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "+",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            label = "30-day",
                            value = percentLabel(state.stats30?.consistencyPercent),
                            modifier = Modifier.weight(1f),
                            accent = MaterialTheme.colorScheme.primary,
                        )
                        StatTile(
                            label = "Streak",
                            value = "${state.stats30?.currentStreak ?: 0}",
                            modifier = Modifier.weight(1f),
                            accent = colors.peach,
                        )
                        StatTile(
                            label = "All time",
                            value = "${state.statsAll?.completedOccurrences ?: 0}",
                            modifier = Modifier.weight(1f),
                            accent = colors.mint,
                        )
                    }
                }
                item {
                    SectionHeader(title = "History", subtitle = "Last 14 weeks")
                    Spacer(Modifier.height(10.dp))
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        HabitHeatmap(
                            completedDays = state.completedDays,
                            endDate = LocalDate.now(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            activeColor = Color(habit.colorArgb),
                        )
                    }
                }
                if (state.recentCompletions.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recent check-ins")
                    }
                    items(state.recentCompletions.size) { index ->
                        val completion = state.recentCompletions[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(habit.colorArgb), RoundedCornerShape(50)),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                epochDayToDate(completion.epochDay).let(::formatDateShort),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${formatTarget(completion.value)} ${habit.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }

    ConfirmDialog(
        visible = showArchive,
        title = "Archive habit?",
        message = "You can restore it later from the archive.",
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
        message = "All completion history for this habit is removed.",
        onConfirm = {
            showDelete = false
            onDelete()
            onDeleted()
        },
        onDismiss = { showDelete = false },
    )
}

private fun formatTarget(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(java.util.Locale.getDefault(), "%.1f", value)

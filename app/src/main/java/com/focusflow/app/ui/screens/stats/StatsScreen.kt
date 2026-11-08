package com.focusflow.app.ui.screens.stats

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.stats.PeriodType
import com.focusflow.app.domain.stats.TaskFocus
import com.focusflow.app.domain.time.formatDurationOrZero
import com.focusflow.app.domain.time.formatDateShort
import com.focusflow.app.domain.time.percentLabel
import com.focusflow.app.ui.components.ChartLabels
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.MiniBarChart
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.components.StatTile
import com.focusflow.app.ui.theme.LocalFocusColors

@Composable
fun StatsScreen(
    state: StatsUiState,
    onPeriodChange: (PeriodType) -> Unit,
    onSeeTasks: () -> Unit,
) {
    val colors = LocalFocusColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    "Statistics",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    state.rangeLabel.ifBlank { "Your focus at a glance" },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PeriodType.entries.filter { it != PeriodType.CUSTOM }) { period ->
                    val selected = state.period == period
                    Surface(
                        onClick = { onPeriodChange(period) },
                        shape = RoundedCornerShape(50),
                        color = if (selected) MaterialTheme.colorScheme.primary else colors.card,
                    ) {
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                colors.muted
                            },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        )
                    }
                }
            }
        }
        item {
            GradientCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Total focus",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatDurationOrZero(state.summary?.totalSeconds ?: 0),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        ChangeText(
                            percent = state.totalChangePercent,
                            amountLabel = "vs previous period",
                        )
                    }
                    IconBubble(
                        icon = Icons.Rounded.Timer,
                        tint = MaterialTheme.colorScheme.primary,
                        background = Color.White.copy(alpha = 0.6f),
                        size = 52.dp,
                        iconSize = 24.dp,
                    )
                }
                Spacer(Modifier.height(18.dp))
                MiniBarChart(
                    values = state.chartValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    barColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = Color.White.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(8.dp))
                ChartLabelsFor(state)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "Sessions",
                    value = "${state.summary?.completedSessions ?: 0}",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.primary,
                    icon = Icons.Rounded.CheckCircle,
                )
                StatTile(
                    label = "Focus rate",
                    value = percentLabel(state.summary?.focusRate),
                    modifier = Modifier.weight(1f),
                    accent = colors.mint,
                    icon = Icons.Rounded.Bolt,
                )
                StatTile(
                    label = "Daily avg",
                    value = formatDurationOrZero(state.summary?.averageDailySeconds ?: 0),
                    modifier = Modifier.weight(1f),
                    accent = colors.peach,
                    icon = Icons.Rounded.Schedule,
                )
            }
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Focus rate",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "${state.summary?.completedSessions ?: 0} completed of " +
                                "${state.summary?.startedSessions ?: 0} started",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    Text(
                        percentLabel(state.summary?.focusRate),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(colors.grid, CircleShape),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                ((state.summary?.focusRate ?: 0.0).coerceIn(0.0, 1.0)).toFloat(),
                            )
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Best day: " + (state.bestDay?.let {
                        "${formatDateShort(it.date)} · ${formatDurationOrZero(it.seconds)}"
                    } ?: "Not enough data yet"),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        item {
            SectionHeader(
                title = "By task",
                subtitle = "Where your focus went",
                actionLabel = "All tasks",
                onAction = onSeeTasks,
            )
        }
        if (state.topTasks.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Timer,
                    title = "No focus recorded",
                    message = "Run the timer for any task and the breakdown will appear here.",
                )
            }
        } else {
            items(state.topTasks, key = { "${it.taskId}-${it.name}" }) { task ->
                TaskBar(task = task, maxSeconds = state.topTasks.first().seconds)
            }
        }
        item {
            SectionHeader(title = "Distractions", subtitle = "This period")
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${state.distractions.size} distractions",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "${formatDurationOrZero(state.distractionMinutes * 60)} logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    IconBubble(
                        icon = Icons.Rounded.Bolt,
                        tint = colors.pink,
                        background = colors.pink.copy(alpha = 0.14f),
                        size = 38.dp,
                        iconSize = 18.dp,
                    )
                }
                if (state.distractions.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    state.distractions.take(4).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                item.entity.activity,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${item.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLabelsFor(state: StatsUiState) {
    val colors = LocalFocusColors.current
    if (state.chartValues.size == state.chartLabels.size && state.chartLabels.size <= 12) {
        ChartLabels(labels = state.chartLabels)
    } else if (state.chartValues.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val first = state.summary?.range?.start?.let(::formatDateShort).orEmpty()
            val last = state.summary?.range?.endExclusive?.minusDays(1)
                ?.let(::formatDateShort).orEmpty()
            Text(first, style = MaterialTheme.typography.labelSmall, color = colors.muted)
            Text("${state.chartValues.size} days", style = MaterialTheme.typography.labelSmall, color = colors.muted)
            Text(last, style = MaterialTheme.typography.labelSmall, color = colors.muted)
        }
    }
}

@Composable
private fun ChangeText(percent: Double?, amountLabel: String) {
    val colors = LocalFocusColors.current
    val text = when {
        percent == null -> "New activity"
        percent > 0 -> "+${percent.toInt()}% $amountLabel"
        percent < 0 -> "${percent.toInt()}% $amountLabel"
        else -> "Same as before"
    }
    val color = when {
        percent == null || percent > 0 -> colors.mint
        percent < 0 -> MaterialTheme.colorScheme.error
        else -> colors.muted
    }
    Text(text, style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
private fun TaskBar(task: TaskFocus, maxSeconds: Long) {
    val colors = LocalFocusColors.current
    val ratio = if (maxSeconds <= 0) 0f else (task.seconds.toFloat() / maxSeconds).coerceIn(0f, 1f)
    SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                task.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                formatDurationOrZero(task.seconds),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(colors.grid, CircleShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}

package com.focusflow.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import com.focusflow.app.domain.time.formatTimer
import com.focusflow.app.ui.components.HorizontalDivider
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
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.AvatarStyle
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.time.formatDuration
import com.focusflow.app.domain.time.formatDurationOrZero
import com.focusflow.app.ui.components.CheckDot
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.FocusRing
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors
import com.focusflow.app.ui.theme.Palette
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    state: HomeUiState,
    onToggleHabit: (Long) -> Unit,
    onOpenTask: (Long) -> Unit,
    onStartTaskFocus: (Long) -> Unit,
    onStartFocus: () -> Unit,
    onSeeTasks: () -> Unit,
    onSeeStats: () -> Unit,
    onSeeHabits: () -> Unit,
) {
    val colors = LocalFocusColors.current
    val progress = if (state.targetSeconds <= 0) 0f else {
        (state.focusSeconds.toFloat() / state.targetSeconds.toFloat()).coerceIn(0f, 1f)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Hi, ${state.userName} \uD83D\uDC4B",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Let's make today count.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = colors.lavender,
                    onClick = onSeeStats,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = IconCatalog.icon(state.avatar.name.lowercase()),
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }

        item {
            val focusTask = state.recommended ?: state.tasks.firstOrNull()
            val focusMinutes = focusTask?.task?.focusMinutes ?: 25
            val waveColor = MaterialTheme.colorScheme.primary
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartFocus,
                contentPadding = 0.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val w = size.width
                            val h = size.height
                            val wave = Path().apply {
                                moveTo(0f, h * 0.78f)
                                cubicTo(w * 0.25f, h * 0.62f, w * 0.45f, h * 0.95f, w * 0.7f, h * 0.78f)
                                cubicTo(w * 0.85f, h * 0.68f, w * 0.95f, h * 0.72f, w, h * 0.7f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(
                                path = wave,
                                brush = Brush.verticalGradient(
                                    listOf(waveColor.copy(alpha = 0.22f), waveColor.copy(alpha = 0.08f)),
                                ),
                            )
                        }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.TrackChanges,
                                contentDescription = null,
                                tint = Palette.Pink,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Today's Focus",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = colors.muted,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = formatTimer(focusMinutes * 60L),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = focusTask?.task?.name ?: "Open focus session",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${formatDurationOrZero(state.focusSeconds)} of " +
                                        "${formatDuration(state.targetSeconds)} today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.muted,
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Palette.Purple,
                                onClick = {
                                    if (focusTask != null) onStartTaskFocus(focusTask.task.id) else onStartFocus()
                                },
                                shadowElevation = 6.dp,
                                modifier = Modifier.size(68.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Start focus",
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(26.dp))
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Your Tasks",
                subtitle = if (state.tasks.isEmpty()) "Nothing scheduled for today" else "${state.tasks.size} planned",
                actionLabel = "See all",
                onAction = onSeeTasks,
            )
        }

        if (state.tasks.isEmpty()) {
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(
                            icon = IconCatalog.icon("leaf"),
                            tint = MaterialTheme.colorScheme.tertiary,
                            background = colors.mint,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("A clear day", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Add a task or enjoy the breathing room.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Icon(
                            Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = colors.muted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        } else {
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
                    state.tasks.take(4).forEachIndexed { index, task ->
                        val perSession = (task.task.focusMinutes * 60L).coerceAtLeast(60L)
                        val target = ((task.task.dailyTargetMinutes * 60L + perSession - 1) / perSession).toInt()
                        val done = (task.todaySeconds / perSession).toInt()
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenTask(task.task.id) }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconBubble(
                                icon = IconCatalog.icon(task.task.iconKey),
                                tint = Color(task.task.colorArgb),
                                background = Color(task.task.colorArgb).copy(alpha = 0.13f),
                                size = 42.dp,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = task.task.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = if (done > 0) "$done / $target Pomodoros" else "$target Pomodoros",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.muted,
                                )
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { task.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = Color(task.task.colorArgb),
                                    trackColor = colors.grid,
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = colors.lavender,
                                onClick = { onStartTaskFocus(task.task.id) },
                                modifier = Modifier.size(30.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Start focus",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Habits",
                subtitle = "Small steps, every day",
                actionLabel = "All habits",
                onAction = onSeeHabits,
            )
        }

        if (state.habits.isEmpty()) {
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), onClick = onSeeHabits) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(
                            icon = Icons.Rounded.SelfImprovement,
                            tint = MaterialTheme.colorScheme.primary,
                            background = colors.lavender,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Start a tiny habit and watch it grow.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.muted,
                        )
                    }
                }
            }
        } else {
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
                    state.habits.take(5).forEach { habit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconBubble(
                                icon = IconCatalog.icon(habit.habit.iconKey),
                                tint = Color(habit.habit.colorArgb),
                                background = Color(habit.habit.colorArgb).copy(alpha = 0.12f),
                                size = 34.dp,
                                iconSize = 17.dp,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = habit.habit.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            CheckDot(
                                checked = habit.completed,
                                onClick = { onToggleHabit(habit.habit.id) },
                            )
                        }
                    }
                }
            }
        }

        if (state.recentSessions.isNotEmpty()) {
            item {
                SectionHeader(title = "Recent sessions", onAction = onSeeStats, actionLabel = "Stats")
            }
            items(state.recentSessions.size) { index ->
                val session = state.recentSessions[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconBubble(
                        icon = if (session.sessionType == SessionType.FOCUS) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.SelfImprovement
                        },
                        tint = if (session.sessionType == SessionType.FOCUS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        background = colors.card,
                        size = 36.dp,
                        iconSize = 18.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            session.taskNameSnapshot,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            timeLabel(session.startedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.muted,
                        )
                    }
                    Text(
                        formatDuration(session.focusSeconds.toLong()),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.muted,
                    )
                }
            }
        }
    }
}

private fun timeLabel(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, h:mm a", Locale.getDefault()))

package com.focusflow.app.ui.screens.home

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
                IconBubble(
                    icon = IconCatalog.icon(state.avatar.name.lowercase()),
                    tint = MaterialTheme.colorScheme.primary,
                    background = colors.lavender,
                    size = 46.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${state.greeting}, ${state.userName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = state.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = colors.card,
                    onClick = onSeeStats,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        IconBubble(
                            icon = IconCatalog.icon("star"),
                            tint = MaterialTheme.colorScheme.secondary,
                            background = Color.Transparent,
                            size = 30.dp,
                            iconSize = 22.dp,
                        )
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                Palette.PurpleDeep,
                                Palette.Indigo,
                            ),
                        ),
                    )
                    .padding(22.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FocusRing(
                        progress = progress,
                        modifier = Modifier.size(122.dp),
                        strokeWidth = 12f,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        color = Color.White,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatDurationOrZero(state.focusSeconds),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "of ${formatDuration(state.targetSeconds)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (progress >= 1f) "Daily goal reached" else "Today's focus",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${state.sessionsStarted} session${if (state.sessionsStarted == 1) "" else "s"} started",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onStartFocus,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Start focus", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Today's tasks",
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
            items(state.tasks.take(4).size) { index ->
                val task = state.tasks[index]
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenTask(task.task.id) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(
                            icon = IconCatalog.icon(task.task.iconKey),
                            tint = Color(task.task.colorArgb),
                            background = Color(task.task.colorArgb).copy(alpha = 0.13f),
                            size = 42.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = task.task.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${(task.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.muted,
                                )
                            }
                            Spacer(Modifier.height(7.dp))
                            LinearProgressIndicator(
                                progress = { task.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = Color(task.task.colorArgb),
                                trackColor = colors.grid,
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "${formatDurationOrZero(task.todaySeconds)} of ${formatDuration(task.task.dailyTargetMinutes * 60L)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = colors.lavender,
                            onClick = { onStartTaskFocus(task.task.id) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = "Start focus",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
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

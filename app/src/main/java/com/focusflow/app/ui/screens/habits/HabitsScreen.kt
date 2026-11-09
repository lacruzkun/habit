package com.focusflow.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import com.focusflow.app.ui.components.RoundAddButton
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.time.percentLabel
import com.focusflow.app.ui.components.CheckDot
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.components.StatTile
import com.focusflow.app.ui.theme.LocalFocusColors

@Composable
fun HabitsScreen(
    state: HabitsUiState,
    listState: LazyListState,
    onAdd: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    onToggle: (Long) -> Unit,
    onSetProgress: (Long, Double) -> Unit,
) {
    val colors = LocalFocusColors.current
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Habits", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Small actions, repeated daily.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                RoundAddButton(onClick = onAdd, contentDescription = "New habit")
            }
        }
        item {
            val streak = state.habits.maxOfOrNull { it.stats.currentStreak } ?: 0
            GradientCard(
                modifier = Modifier.fillMaxWidth(),
                colors = listOf(Color(0xFFFFEFC9), Color(0xFFFFD9A0)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF8A1F),
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Daily Streak",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF2E2459),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$streak ${if (streak == 1) "day" else "days"}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF2E2459),
                        )
                        Text(
                            "${state.completedToday} of ${state.scheduledToday} done today",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B5A3A),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        listOf(18, 30, 42, 56, 70).forEachIndexed { index, h ->
                            Box(
                                modifier = Modifier
                                    .width(11.dp)
                                    .height(h.dp)
                                    .background(
                                        Color(0xFFFF9F1C).copy(alpha = 0.55f + index * 0.11f),
                                        RoundedCornerShape(5.dp),
                                    ),
                            )
                        }
                    }
                }
            }
        }
        if (state.habits.isEmpty() && !state.loading) {
            item {
                EmptyState(
                    icon = Icons.Rounded.SelfImprovement,
                    title = "Build your first habit",
                    message = "Track reading, water, movement, or anything you want to repeat.",
                    actionLabel = "Create habit",
                    onAction = onAdd,
                )
            }
        }
        item {
            SectionHeader(
                title = "Your habits",
                subtitle = "${state.habits.size} active",
            )
        }
        items(state.habits, key = { it.habit.id }) { card ->
            val habit = card.habit
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenHabit(habit.id) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(
                        icon = IconCatalog.icon(habit.iconKey),
                        tint = Color(habit.colorArgb),
                        background = Color(habit.colorArgb).copy(alpha = 0.13f),
                        size = 44.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            habit.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            buildString {
                                append("${card.weeklyProgress}/${card.weeklyTarget} this week")
                                if (card.stats.currentStreak > 0) {
                                    append(" · ${card.stats.currentStreak} day streak")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.muted,
                        )
                        if (habit.targetValue > 1.0) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Target ${habit.targetValue.toInt()} ${habit.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(habit.colorArgb),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    if (habit.targetValue > 1.0) {
                        Surface(
                            onClick = {
                                val next = if (card.todayCompleted) 0.0 else habit.targetValue
                                onSetProgress(habit.id, next)
                            },
                            shape = RoundedCornerShape(50),
                            color = if (card.todayCompleted) {
                                Color(habit.colorArgb)
                            } else {
                                colors.lavender
                            },
                        ) {
                            Text(
                                text = if (card.todayCompleted) "Done" else "Log",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (card.todayCompleted) {
                                    Color.White
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    } else {
                        CheckDot(
                            checked = card.todayCompleted,
                            onClick = { onToggle(habit.id) },
                        )
                    }
                }
            }
        }
        if (state.habits.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val bestStreak = state.habits.maxOfOrNull { it.stats.currentStreak } ?: 0
                    val avgConsistency = state.habits.mapNotNull { it.stats.consistencyPercent }
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                    StatTile(
                        label = "Best streak",
                        value = "$bestStreak days",
                        modifier = Modifier.weight(1f),
                        accent = colors.peach,
                        icon = Icons.Rounded.LocalFireDepartment,
                    )
                    StatTile(
                        label = "30-day consistency",
                        value = percentLabel(avgConsistency),
                        modifier = Modifier.weight(1f),
                        accent = colors.mint,
                        icon = Icons.Rounded.SelfImprovement,
                    )
                }
            }
            item {
                val bestStreak = state.habits.maxOfOrNull { it.stats.currentStreak } ?: 0
                if (bestStreak > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF4A3F8F), Color(0xFF2E2563))),
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "You're on fire! \uD83D\uDD25",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                            Text(
                                "Keep up the great work!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f),
                            )
                        }
                        Icon(
                            imageVector = IconCatalog.icon("cat"),
                            contentDescription = null,
                            tint = Color(0xFFD8CCFA),
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }
        }
    }
}

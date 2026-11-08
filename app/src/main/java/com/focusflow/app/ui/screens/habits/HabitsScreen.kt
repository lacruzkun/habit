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
    onAdd: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    onToggle: (Long) -> Unit,
    onSetProgress: (Long, Double) -> Unit,
) {
    val colors = LocalFocusColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Habits", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Small actions, repeated daily.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onAdd,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "New habit",
                            tint = MaterialTheme.colorScheme.onPrimary,
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
                            "Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${state.completedToday} of ${state.scheduledToday}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "scheduled habits completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    IconBubble(
                        icon = Icons.Rounded.LocalFireDepartment,
                        tint = colors.peach,
                        background = Color.White.copy(alpha = 0.6f),
                        size = 52.dp,
                        iconSize = 26.dp,
                    )
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
        items(state.habits.size) { index ->
            val card = state.habits[index]
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
        }
    }
}

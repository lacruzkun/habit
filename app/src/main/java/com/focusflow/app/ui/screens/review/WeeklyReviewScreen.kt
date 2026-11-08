package com.focusflow.app.ui.screens.review

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.time.dateRangeLabel
import com.focusflow.app.domain.time.formatDateShort
import com.focusflow.app.domain.time.formatDurationOrZero
import com.focusflow.app.domain.time.percentLabel
import com.focusflow.app.ui.components.ChartLabels
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.MiniBarChart
import com.focusflow.app.ui.components.PrimaryButton
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.components.StatTile
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyReviewScreen(
    state: WeeklyReviewUiState,
    onBack: () -> Unit,
    onSaveReflection: (String, String, String) -> Unit,
) {
    val colors = LocalFocusColors.current
    val context = LocalContext.current
    var wentWell by remember(state.weekStart) { mutableStateOf("") }
    var needsImprovement by remember(state.weekStart) { mutableStateOf("") }
    var nextFocus by remember(state.weekStart) { mutableStateOf("") }

    LaunchedEffect(state.reflection) {
        val reflection = state.reflection
        if (reflection != null) {
            wentWell = reflection.wentWell
            needsImprovement = reflection.needsImprovement
            nextFocus = reflection.nextFocus
        }
    }

    val data = state.data
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "Weekly review",
                subtitle = dateRangeLabel(state.weekStart, state.weekStart.plusWeeks(1)),
                onBack = onBack,
            )
        }
        if (data == null) {
            item {
                EmptyState(
                    icon = Icons.Rounded.SelfImprovement,
                    title = "Nothing to review yet",
                    message = "Once you record focus time this week, your summary appears here.",
                )
            }
        } else {
            item {
                GradientCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Total focus",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.muted,
                    )
                    Text(
                        formatDurationOrZero(data.current.totalSeconds),
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (data.weeklyGoalSeconds > 0) {
                            "${((data.current.totalSeconds.toDouble() / data.weeklyGoalSeconds) * 100)
                                .toInt()
                                .coerceIn(0, 999)}% of your " +
                                "${formatDurationOrZero(data.weeklyGoalSeconds)} weekly goal"
                        } else {
                            "Set weekly targets on your tasks to track a goal."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                    Spacer(Modifier.height(16.dp))
                    MiniBarChart(
                        values = data.current.daily.map { it.seconds },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    ChartLabels(
                        labels = data.current.daily.map {
                            it.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                        },
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "Sessions",
                        value = "${data.current.completedSessions}",
                        modifier = Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.primary,
                    )
                    StatTile(
                        label = "Focus rate",
                        value = percentLabel(data.current.focusRate),
                        modifier = Modifier.weight(1f),
                        accent = colors.mint,
                    )
                    StatTile(
                        label = "Daily avg",
                        value = formatDurationOrZero(data.current.averageDailySeconds),
                        modifier = Modifier.weight(1f),
                        accent = colors.peach,
                    )
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Insight", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        data.insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        data.bestDay?.let {
                            Text(
                                "Best day: ${formatDateShort(it)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.mint,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        data.leastProductiveDay?.let {
                            Text(
                                "Quietest: ${formatDateShort(it)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                            )
                        }
                    }
                }
            }
            if (data.taskTotals.isNotEmpty()) {
                item {
                    SectionHeader(title = "Task focus", subtitle = "This week")
                }
                items(data.taskTotals.size) { index ->
                    val task = data.taskTotals[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            task.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            formatDurationOrZero(task.seconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            if (state.habitRows.isNotEmpty()) {
                item {
                    SectionHeader(title = "Habits", subtitle = "Consistency this week")
                }
                item {
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        state.habitRows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                            ) {
                                Text(
                                    row.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    "${row.stats.completedOccurrences}/${row.stats.scheduledOccurrences}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (
                                        row.stats.consistencyPercent != null &&
                                        row.stats.consistencyPercent >= 70.0
                                    ) {
                                        colors.mint
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
        item {
            SectionHeader(
                title = "Reflection",
                subtitle = "Saved automatically for this week",
            )
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = wentWell,
                    onValueChange = { wentWell = it.take(600) },
                    label = { Text("What went well?") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = needsImprovement,
                    onValueChange = { needsImprovement = it.take(600) },
                    label = { Text("What needs improvement?") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = nextFocus,
                    onValueChange = { nextFocus = it.take(300) },
                    label = { Text("Next week's focus") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "Save reflection",
                    onClick = {
                        onSaveReflection(wentWell.trim(), needsImprovement.trim(), nextFocus.trim())
                        Toast.makeText(context, "Reflection saved", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}

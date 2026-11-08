package com.focusflow.app.ui.screens.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.time.formatDurationFromMinutes
import com.focusflow.app.domain.time.formatTimer
import com.focusflow.app.domain.timer.TimerMath
import com.focusflow.app.domain.timer.TimerState
import com.focusflow.app.domain.timer.TimerStatus
import com.focusflow.app.timer.TimerEvent
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.FocusRing
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.PomodoroDots
import com.focusflow.app.ui.components.PrimaryButton
import com.focusflow.app.ui.components.SecondaryButton
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast

@Composable
fun TimerScreen(
    timerState: TimerState?,
    tick: Long,
    taskInfo: TimerTaskInfo,
    events: Flow<TimerEvent>,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onToggle: () -> Unit,
    onEnd: () -> Unit,
    onSkipBreak: () -> Unit,
    onLogDistraction: (String, Int, String?, Boolean) -> Unit,
    onOpenTasks: () -> Unit,
) {
    val colors = LocalFocusColors.current
    val context = LocalContext.current
    var showEndConfirm by remember { mutableStateOf(false) }
    var showDistraction by remember { mutableStateOf(false) }

    LaunchedEffect(events) {
        events.collectLatest { event ->
            val message = when (event) {
                is TimerEvent.FocusCompleted -> "Focus session complete. Time for a break."
                is TimerEvent.BreakCompleted -> "Break finished. Ready to focus again?"
                is TimerEvent.BreakStarted -> "Break started. Step away for a moment."
                is TimerEvent.SessionEnded -> "Session ended."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = colors.muted)
            }
            Text(
                text = "Focus",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick = onOpenTasks,
                shape = RoundedCornerShape(50),
                color = colors.card,
            ) {
                Text(
                    text = taskInfo.task?.name ?: "No task",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.muted,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        GradientCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = when (timerState?.sessionType) {
                    SessionType.SHORT_BREAK -> "Short break"
                    SessionType.LONG_BREAK -> "Long break"
                    SessionType.FOCUS, null -> "Deep focus"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = taskInfo.task?.name ?: "Open focus session",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(22.dp))
            val remaining = timerState?.let {
                TimerMath.remainingSeconds(it, tick, System.currentTimeMillis())
            } ?: (taskInfo.config.focusMinutes * 60L)
            val planned = timerState?.plannedSeconds?.toLong()
                ?: (taskInfo.config.focusMinutes * 60L)
            val progress = if (planned > 0) {
                ((planned - remaining).toFloat() / planned.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center,
            ) {
                FocusRing(
                    progress = progress,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    strokeWidth = 16f,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.6f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTimer(remaining),
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 54.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = when {
                                timerState == null -> "Ready when you are"
                                timerState.status == TimerStatus.PAUSED -> "Paused"
                                else -> "Stay with it"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.muted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            if (timerState == null) {
                PrimaryButton(
                    text = "Start focus",
                    icon = Icons.Rounded.PlayArrow,
                    onClick = onStart,
                )
            } else {
                PomodoroDots(
                    completed = timerState.cycleCount,
                    total = timerState.config.sessionsBeforeLongBreak,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = { showEndConfirm = true },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(52.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "End session",
                                tint = colors.muted,
                            )
                        }
                    }
                    Surface(
                        onClick = onToggle,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(68.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (timerState.status == TimerStatus.RUNNING) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                                contentDescription = if (timerState.status == TimerStatus.RUNNING) {
                                    "Pause"
                                } else {
                                    "Resume"
                                },
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                    Surface(
                        onClick = if (timerState.isBreak) onSkipBreak else onToggle,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(52.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.SkipNext,
                                contentDescription = if (timerState.isBreak) "Skip break" else "Skip",
                                tint = colors.muted,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Session setup", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${taskInfo.config.focusMinutes}m focus · " +
                            "${taskInfo.config.shortBreakMinutes}m short · " +
                            "${taskInfo.config.longBreakMinutes}m long",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                Surface(
                    onClick = onOpenTasks,
                    shape = RoundedCornerShape(50),
                    color = colors.lavender,
                ) {
                    Text(
                        "Change",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SecondaryButton(
            text = "Log a distraction",
            icon = Icons.Rounded.Lightbulb,
            onClick = { showDistraction = true },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(18.dp))
        SectionHeader(title = "How it works")
        Spacer(Modifier.height(6.dp))
        Text(
            "Focus runs in a foreground service, so the countdown continues while you use other apps. " +
                "Each completed focus adds a dot; every ${taskInfo.config.sessionsBeforeLongBreak} " +
                "sessions earns a longer break.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
        )
        Spacer(Modifier.height(24.dp))
    }

    ConfirmDialog(
        visible = showEndConfirm,
        title = "End this session?",
        message = "Sessions shorter than a minute are discarded. Longer sessions are saved as interrupted.",
        confirmLabel = "End session",
        onConfirm = {
            showEndConfirm = false
            onEnd()
        },
        onDismiss = { showEndConfirm = false },
    )
    if (showDistraction) {
        DistractionDialog(
            onDismiss = { showDistraction = false },
            onLog = { activity, minutes, note, pause ->
                showDistraction = false
                onLogDistraction(activity, minutes, note, pause)
            },
        )
    }
}

@Composable
private fun DistractionDialog(
    onDismiss: () -> Unit,
    onLog: (String, Int, String?, Boolean) -> Unit,
) {
    var activity by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf(5) }
    var note by remember { mutableStateOf("") }
    var pause by remember { mutableStateOf(false) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a distraction") },
        text = {
            Column {
                OutlinedTextField(
                    value = activity,
                    onValueChange = { activity = it.take(80) },
                    label = { Text("What pulled you away?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Minutes", modifier = Modifier.weight(1f))
                    androidx.compose.material3.OutlinedButton(
                        onClick = { minutes = (minutes - 1).coerceAtLeast(1) },
                    ) { Text("−") }
                    Text(
                        "$minutes",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                    androidx.compose.material3.OutlinedButton(
                        onClick = { minutes = (minutes + 1).coerceAtMost(120) },
                    ) { Text("+") }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(200) },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pause the timer", modifier = Modifier.weight(1f))
                    Switch(checked = pause, onCheckedChange = { pause = it })
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    if (activity.isNotBlank()) {
                        onLog(activity.trim(), minutes, note.trim().ifBlank { null }, pause)
                    }
                },
                enabled = activity.isNotBlank(),
            ) { Text("Log") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

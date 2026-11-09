package com.focusflow.app.ui.screens.timer

import android.app.Activity
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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

    val view = LocalView.current
    DisposableEffect(Unit) {
        val controller = (view.context as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.let { c ->
                previousStatus?.let { c.isAppearanceLightStatusBars = it }
                previousNav?.let { c.isAppearanceLightNavigationBars = it }
            }
        }
    }

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
    val onDark = Color.White
    val onDarkMuted = Color.White.copy(alpha = 0.7f)
    val glass = Color.White.copy(alpha = 0.09f)
    val accent = Color(0xFFD8CCFA)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF473C8F), Color(0xFF2C2362)))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = glass,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Close",
                            tint = onDark,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))

            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                tint = Color(0xFF7BE3B4),
                modifier = Modifier.size((34 + 22 * progress).dp),
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier.size(272.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                )
                FocusRing(
                    progress = progress,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    strokeWidth = 14f,
                    color = accent,
                    trackColor = Color.White.copy(alpha = 0.14f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTimer(remaining),
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 54.sp,
                            color = onDark,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = when {
                                timerState?.sessionType == SessionType.SHORT_BREAK -> "Short Break"
                                timerState?.sessionType == SessionType.LONG_BREAK -> "Long Break"
                                timerState?.status == TimerStatus.PAUSED -> "Paused"
                                else -> "Focus Time"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = onDarkMuted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(22.dp))

            Surface(
                onClick = onOpenTasks,
                shape = RoundedCornerShape(18.dp),
                color = glass,
                modifier = Modifier.fillMaxWidth(0.86f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.Code, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = taskInfo.task?.name ?: "No task selected",
                        style = MaterialTheme.typography.labelLarge,
                        color = onDark,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Change task",
                        tint = onDarkMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(22.dp))

            if (timerState == null) {
                Surface(
                    onClick = onStart,
                    shape = RoundedCornerShape(50),
                    color = accent,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF2E2459))
                        Spacer(Modifier.width(8.dp))
                        Text("Start focus", style = MaterialTheme.typography.labelLarge, color = Color(0xFF2E2459))
                    }
                }
            } else {
                PomodoroDots(
                    completed = timerState.cycleCount,
                    total = timerState.config.sessionsBeforeLongBreak,
                    activeColor = accent,
                    inactiveColor = Color.White.copy(alpha = 0.22f),
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onToggle,
                        shape = RoundedCornerShape(50),
                        color = accent,
                        modifier = Modifier.width(100.dp).height(56.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (timerState.status == TimerStatus.RUNNING) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                                contentDescription = if (timerState.status == TimerStatus.RUNNING) "Pause" else "Resume",
                                tint = Color(0xFF2E2459),
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                    Surface(
                        onClick = { showEndConfirm = true },
                        shape = RoundedCornerShape(18.dp),
                        color = glass,
                        modifier = Modifier.size(56.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Stop,
                                contentDescription = "End session",
                                tint = onDark,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    if (timerState.isBreak) {
                        Surface(
                            onClick = onSkipBreak,
                            shape = RoundedCornerShape(18.dp),
                            color = glass,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.SkipNext,
                                    contentDescription = "Skip break",
                                    tint = onDark,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = glass,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "Small steps\nlead to big results.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onDark,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = glass,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Session setup", style = MaterialTheme.typography.titleSmall, color = onDark)
                        Text(
                            "${taskInfo.config.focusMinutes}m focus · " +
                                "${taskInfo.config.shortBreakMinutes}m short · " +
                                "${taskInfo.config.longBreakMinutes}m long",
                            style = MaterialTheme.typography.bodySmall,
                            color = onDarkMuted,
                        )
                    }
                    Surface(
                        onClick = onOpenTasks,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.14f),
                    ) {
                        Text(
                            "Change",
                            style = MaterialTheme.typography.labelMedium,
                            color = onDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.OutlinedButton(
                onClick = { showDistraction = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = onDark),
            ) {
                Icon(Icons.Rounded.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Log a distraction", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
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

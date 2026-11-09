package com.focusflow.app.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.focusflow.app.domain.model.AvatarStyle
import com.focusflow.app.domain.model.ThemeMode
import com.focusflow.app.domain.model.WeekStart
import com.focusflow.app.domain.time.formatDurationFromMinutes
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.PrimaryButton
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors
import kotlinx.coroutines.flow.Flow

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    busy: Boolean,
    messages: Flow<String>,
    onBack: (() -> Unit)?,
    onMoreTools: () -> Unit,
    onNameChange: (String) -> Unit,
    onAvatarChange: (AvatarStyle) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
    onDailyTargetChange: (Int) -> Unit,
    onPomodoroChange: (Int, Int, Int, Int) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onExportJson: (Uri) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onImportJson: (Uri) -> Unit,
    onClearData: () -> Unit,
) {
    val colors = LocalFocusColors.current
    val context = LocalContext.current
    val prefs = state.prefs
    var showClear by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var name by remember(prefs.userName) { mutableStateOf(prefs.userName) }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> onNotificationsChange(granted) }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(onExportJson) }
    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let(onExportCsv) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> pendingImport = uri }

    LaunchedEffect(messages) {
        messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = "More",
            subtitle = "Make space for what matters",
            onBack = onBack,
            actions = {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradientCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(
                            icon = IconCatalog.icon(prefs.avatarStyle.name.lowercase()),
                            tint = MaterialTheme.colorScheme.primary,
                            background = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            size = 58.dp,
                            iconSize = 28.dp,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                prefs.userName.ifBlank { "Friend" },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                "Daily goal · ${formatDurationFromMinutes(prefs.dailyFocusTargetMinutes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                            )
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "Profile")
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it.take(40)
                            onNameChange(name)
                        },
                        label = { Text("Your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Choose your avatar", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(IconCatalog.avatarChoices) { key ->
                            val selected = prefs.avatarStyle.name.lowercase() == key
                            Surface(
                                onClick = {
                                    AvatarStyle.entries.firstOrNull {
                                        it.name.equals(key, ignoreCase = true)
                                    }?.let(onAvatarChange)
                                },
                                shape = CircleShape,
                                color = if (selected) colors.lavender else colors.background,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else colors.grid,
                                ),
                                modifier = Modifier.size(46.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        IconCatalog.icon(key),
                                        contentDescription = key,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else colors.muted,
                                        modifier = Modifier.size(21.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "Appearance")
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Theme", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            ChoiceChip(
                                label = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                selected = prefs.themeMode == mode,
                                onClick = { onThemeChange(mode) },
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Week starts on", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WeekStart.entries.forEach { weekStart ->
                            ChoiceChip(
                                label = weekStart.name.lowercase().replaceFirstChar { it.uppercase() },
                                selected = prefs.startOfWeek == weekStart,
                                onClick = { onWeekStartChange(weekStart) },
                            )
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "Focus")
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Daily focus target", style = MaterialTheme.typography.titleSmall)
                            Text(
                                formatDurationFromMinutes(prefs.dailyFocusTargetMinutes),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Text(
                            "${prefs.dailyFocusTargetMinutes}m",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Slider(
                        value = prefs.dailyFocusTargetMinutes.toFloat(),
                        onValueChange = { onDailyTargetChange((it / 15).toInt() * 15) },
                        valueRange = 15f..480f,
                        steps = 30,
                    )
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Default Pomodoro", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    NumberRow(
                        label = "Focus",
                        value = prefs.defaultFocusMinutes,
                        onChange = {
                            onPomodoroChange(
                                it,
                                prefs.defaultShortBreakMinutes,
                                prefs.defaultLongBreakMinutes,
                                prefs.defaultSessionsBeforeLongBreak,
                            )
                        },
                    )
                    NumberRow(
                        label = "Short break",
                        value = prefs.defaultShortBreakMinutes,
                        onChange = {
                            onPomodoroChange(
                                prefs.defaultFocusMinutes,
                                it,
                                prefs.defaultLongBreakMinutes,
                                prefs.defaultSessionsBeforeLongBreak,
                            )
                        },
                    )
                    NumberRow(
                        label = "Long break",
                        value = prefs.defaultLongBreakMinutes,
                        onChange = {
                            onPomodoroChange(
                                prefs.defaultFocusMinutes,
                                prefs.defaultShortBreakMinutes,
                                it,
                                prefs.defaultSessionsBeforeLongBreak,
                            )
                        },
                    )
                    NumberRow(
                        label = "Sessions before long break",
                        value = prefs.defaultSessionsBeforeLongBreak,
                        onChange = {
                            onPomodoroChange(
                                prefs.defaultFocusMinutes,
                                prefs.defaultShortBreakMinutes,
                                prefs.defaultLongBreakMinutes,
                                it,
                            )
                        },
                    )
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Notifications", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Session alerts and scheduled reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Switch(
                            checked = prefs.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                val needsPermission = enabled &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) != PackageManager.PERMISSION_GRANTED
                                if (needsPermission) {
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onNotificationsChange(enabled)
                                }
                            },
                        )
                    }
                }
            }
            item {
                SectionHeader(title = "Data & privacy")
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Backups include tasks, habits, sessions, categories, reminders, and reflections.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                exportJsonLauncher.launch("focusflow-backup.json")
                            },
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Backup")
                        }
                        OutlinedButton(
                            onClick = {
                                exportCsvLauncher.launch("focusflow-sessions.csv")
                            },
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("CSV")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Restore from backup")
                    }
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Danger zone", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Permanently deletes every task, habit, session, and setting on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showClear = true },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            Icons.Rounded.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Clear all data", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = 6.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onMoreTools)
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconBubble(
                            icon = Icons.Rounded.Settings,
                            tint = MaterialTheme.colorScheme.primary,
                            background = colors.lavender,
                            size = 38.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("More tools", style = MaterialTheme.typography.titleSmall)
                            Text("Categories, reminders, archive and weekly review", style = MaterialTheme.typography.labelSmall, color = colors.muted)
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.muted)
                    }
                }
            }
            item {
                Text(
                    "FocusFlow ${state.appVersion} · everything stays on this device",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        }
    }

    ConfirmDialog(
        visible = pendingImport != null,
        title = "Restore from backup?",
        message = "This replaces all tasks, habits, sessions, categories, reminders, and settings on this device.",
        confirmLabel = "Restore",
        onConfirm = {
            pendingImport?.let(onImportJson)
            pendingImport = null
        },
        onDismiss = { pendingImport = null },
    )
    ConfirmDialog(
        visible = showClear,
        title = "Clear all data?",
        message = "This cannot be undone. Export a backup first if you want to keep your history.",
        confirmLabel = "Clear everything",
        onConfirm = {
            showClear = false
            onClearData()
        },
        onDismiss = { showClear = false },
    )
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalFocusColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) colors.lavender else colors.background,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else colors.muted,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun NumberRow(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Stepper("−") { onChange((value - 1).coerceAtLeast(1)) }
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { onChange(it.filter(Char::isDigit).toIntOrNull() ?: value) },
            singleLine = true,
            modifier = Modifier.width(72.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
        )
        Stepper("+") { onChange((value + 1).coerceAtMost(180)) }
    }
}

@Composable
private fun Stepper(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = LocalFocusColors.current.lavender,
        modifier = Modifier
            .size(30.dp)
            .padding(0.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

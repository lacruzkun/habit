package com.focusflow.app.ui.screens.more

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.app.BuildConfig
import com.focusflow.app.data.prefs.UserPreferences
import com.focusflow.app.domain.time.formatDurationFromMinutes
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.SectionHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoreScreen(
    prefs: UserPreferences,
    onSettings: () -> Unit,
    onReminders: () -> Unit,
    onWeeklyReview: () -> Unit,
    onArchive: () -> Unit,
    onCategories: () -> Unit,
    onTasks: () -> Unit,
) {
    val colors = LocalFocusColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("More", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Plan, reflect, and tune your setup.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(
                        icon = IconCatalog.icon(prefs.avatarStyle.name.lowercase()),
                        tint = MaterialTheme.colorScheme.primary,
                        background = colors.lavender,
                        size = 54.dp,
                        iconSize = 26.dp,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            prefs.userName.ifBlank { "Friend" },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "Daily goal: ${formatDurationFromMinutes(prefs.dailyFocusTargetMinutes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                        Text(
                            LocalDate.now().format(
                                DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.muted,
                        )
                    }
                }
            }
        }
        item {
            SectionHeader(title = "Plan")
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = 6.dp) {
                MenuRow(
                    icon = Icons.Rounded.Checklist,
                    label = "Tasks & categories",
                    subtitle = "Goals, schedules, Pomodoro settings",
                    onClick = onTasks,
                )
                MenuRow(
                    icon = Icons.Rounded.Category,
                    label = "Categories",
                    subtitle = "Rename or remove category buckets",
                    onClick = onCategories,
                )
                MenuRow(
                    icon = Icons.Rounded.EditNote,
                    label = "Weekly review",
                    subtitle = "Reflect on the week and set the next focus",
                    onClick = onWeeklyReview,
                )
            }
        }
        item {
            SectionHeader(title = "App")
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = 6.dp) {
                MenuRow(
                    icon = Icons.Rounded.Settings,
                    label = "Settings",
                    subtitle = "Theme, targets, Pomodoro defaults, data",
                    onClick = onSettings,
                )
                MenuRow(
                    icon = Icons.Rounded.Notifications,
                    label = "Reminders",
                    subtitle = "Gentle nudges for tasks and habits",
                    onClick = onReminders,
                )
                MenuRow(
                    icon = Icons.Rounded.Archive,
                    label = "Archive",
                    subtitle = "Restore or permanently delete items",
                    onClick = onArchive,
                )
            }
        }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("FocusFlow", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Version ${BuildConfig.VERSION_NAME} · offline-first",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.muted,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.lavender,
                    ) {
                        Text(
                            "Local data",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Everything you record stays on this device unless you export it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = LocalFocusColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.background,
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(Color.Transparent),
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = colors.muted)
        }
        Box(
            modifier = Modifier.size(30.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

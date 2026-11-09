package com.focusflow.app.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.focusflow.app.domain.model.AvatarStyle
import com.focusflow.app.domain.time.formatDurationFromMinutes
import com.focusflow.app.ui.components.AppBackground
import com.focusflow.app.ui.components.GradientCard
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.PrimaryButton
import com.focusflow.app.ui.components.SecondaryButton
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.theme.LocalFocusColors
import com.focusflow.app.ui.theme.Palette

private val avatarOrder = listOf(
    AvatarStyle.MOON to "moon",
    AvatarStyle.LEAF to "leaf",
    AvatarStyle.BOLT to "bolt",
    AvatarStyle.WAVE to "wave",
    AvatarStyle.STAR to "star",
    AvatarStyle.CAT to "cat",
)

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onAvatarChange: (AvatarStyle) -> Unit,
    onTargetChange: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val colors = LocalFocusColors.current
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onFinish() }

    val requestFinish = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onFinish()
        }
    }

    AppBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "FocusFlow",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == step) 22.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index <= step) MaterialTheme.colorScheme.primary
                                    else colors.grid,
                                ),
                        )
                    }
                }
            }

            when (step) {
                0 -> WelcomeStep()
                1 -> IdentityStep(
                    state = state,
                    onNameChange = onNameChange,
                    onAvatarChange = onAvatarChange,
                )
                else -> GoalsStep(
                    state = state,
                    onTargetChange = onTargetChange,
                )
            }

            Spacer(Modifier.height(26.dp))
            PrimaryButton(
                text = when (step) {
                    0 -> "Get started"
                    1 -> "Continue"
                    else -> "Start focusing"
                },
                icon = Icons.Rounded.ArrowForward,
                enabled = !state.saving,
                onClick = {
                    when (step) {
                        0, 1 -> step++
                        else -> requestFinish()
                    }
                },
            )
            if (step > 0) {
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    text = "Back",
                    onClick = { step-- },
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun WelcomeStep() {
    val colors = LocalFocusColors.current
    val hill = Palette.Indigo
    Spacer(Modifier.height(28.dp))
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Palette.Purple.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Check,
            contentDescription = null,
            tint = Palette.White,
            modifier = Modifier.size(38.dp),
        )
    }
    Spacer(Modifier.height(16.dp))
    Text(
        text = "FocusFlow",
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = "Plan. Focus. Achieve.",
        style = MaterialTheme.typography.bodyLarge,
        color = colors.muted,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.verticalGradient(listOf(colors.lavender, Palette.Lavender)))
            .drawBehind {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.62f)
                    cubicTo(w * 0.25f, h * 0.45f, w * 0.55f, h * 0.55f, w * 0.75f, h * 0.5f)
                    cubicTo(w * 0.9f, h * 0.47f, w * 0.97f, h * 0.52f, w, h * 0.5f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, Brush.verticalGradient(listOf(Palette.PurpleDeep, hill)))
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Icon(
            imageVector = IconCatalog.icon("cat"),
            contentDescription = null,
            tint = hill,
            modifier = Modifier
                .padding(top = 56.dp)
                .size(110.dp),
        )
    }
    Spacer(Modifier.height(24.dp))
    Text(
        text = "A calm space for deep work, gentle habits, and a clear view of your progress.",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 10.dp),
    )
}

@Composable
private fun IdentityStep(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onAvatarChange: (AvatarStyle) -> Unit,
) {
    Spacer(Modifier.height(34.dp))
    IconBubble(
        icon = IconCatalog.icon("moon"),
        tint = MaterialTheme.colorScheme.primary,
        background = LocalFocusColors.current.lavender,
        size = 82.dp,
        iconSize = 36.dp,
    )
    Spacer(Modifier.height(20.dp))
    Text(
        text = "What should we\ncall you?",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(24.dp))
    OutlinedTextField(
        value = state.name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Your name") },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
    Spacer(Modifier.height(22.dp))
    Text(
        text = "Pick a companion",
        style = MaterialTheme.typography.titleSmall,
        color = LocalFocusColors.current.muted,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        avatarOrder.forEach { (style, key) ->
            val selected = state.avatar == style
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else LocalFocusColors.current.card,
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else LocalFocusColors.current.grid,
                ),
                onClick = { onAvatarChange(style) },
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = IconCatalog.icon(key),
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary
                        else LocalFocusColors.current.muted,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalsStep(
    state: OnboardingUiState,
    onTargetChange: (Int) -> Unit,
) {
    val colors = LocalFocusColors.current
    Spacer(Modifier.height(30.dp))
    Text(
        text = "How much focus\nfeels right?",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = "You can change this any time in settings.",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.muted,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(28.dp))
    SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = 22.dp) {
        Text(
            text = formatDurationFromMinutes(state.dailyTargetMinutes),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "daily focus goal",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )
        Spacer(Modifier.height(18.dp))
        Slider(
            value = state.dailyTargetMinutes.toFloat(),
            onValueChange = { onTargetChange(it.toInt()) },
            valueRange = 15f..480f,
            steps = 30,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("15m", style = MaterialTheme.typography.labelSmall, color = colors.muted)
            Text("8h", style = MaterialTheme.typography.labelSmall, color = colors.muted)
        }
    }
    Spacer(Modifier.height(16.dp))
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(
                icon = Icons.Rounded.NotificationsActive,
                tint = MaterialTheme.colorScheme.secondary,
                background = colors.peach,
                size = 44.dp,
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Gentle reminders",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "We will ask for notification permission on the next step.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
    }
}

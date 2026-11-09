package com.focusflow.app.ui.screens.categories

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.ui.components.ConfirmDialog
import com.focusflow.app.ui.components.EmptyState
import com.focusflow.app.ui.components.IconBubble
import com.focusflow.app.ui.components.IconCatalog
import com.focusflow.app.ui.components.ScreenHeader
import com.focusflow.app.ui.components.SoftCard
import com.focusflow.app.ui.theme.LocalFocusColors
import kotlinx.coroutines.flow.Flow

private val categoryColors = listOf(
    0xFF6C4CF1, 0xFF3FCB92, 0xFFFFB877, 0xFFFF7A9E, 0xFF67B7F7, 0xFFE5484D,
)

@Composable
fun CategoriesScreen(
    state: CategoriesUiState,
    messages: Flow<String>,
    onBack: () -> Unit,
    onCreate: (String, Long, String) -> Unit,
    onUpdate: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
) {
    val colors = LocalFocusColors.current
    val context = LocalContext.current
    var editor by remember { mutableStateOf<CategoryEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    LaunchedEffect(messages) {
        messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(
                title = "Categories",
                subtitle = "Group tasks without losing focus",
                onBack = onBack,
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { creating = true },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "New category",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                },
            )
        }
        if (state.categories.isEmpty() && !state.loading) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Category,
                    title = "No categories yet",
                    message = "Create a category to group related tasks and keep colors consistent.",
                    actionLabel = "Create category",
                    onAction = { creating = true },
                )
            }
        }
        items(state.categories, key = { it.category.id }) { row ->
            val category = row.category
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(
                        icon = IconCatalog.icon(category.iconKey),
                        tint = Color(category.colorArgb),
                        background = Color(category.colorArgb).copy(alpha = 0.14f),
                        size = 42.dp,
                        iconSize = 20.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(category.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${row.usageCount} task${if (row.usageCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.muted,
                        )
                    }
                    IconButton(onClick = { editor = category }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = colors.muted)
                    }
                    IconButton(onClick = { pendingDelete = category }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (creating || editor != null) {
        CategoryDialog(
            existing = editor,
            onDismiss = {
                creating = false
                editor = null
            },
            onSave = { name, color, icon ->
                val current = editor
                if (current == null) {
                    onCreate(name, color, icon)
                } else {
                    onUpdate(current.copy(name = name, colorArgb = color, iconKey = icon))
                }
                creating = false
                editor = null
            },
        )
    }

    ConfirmDialog(
        visible = pendingDelete != null,
        title = "Delete category?",
        message = "Categories that still contain tasks cannot be deleted.",
        onConfirm = {
            pendingDelete?.let(onDelete)
            pendingDelete = null
        },
        onDismiss = { pendingDelete = null },
    )
}

@Composable
private fun CategoryDialog(
    existing: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit,
) {
    val colors = LocalFocusColors.current
    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var color by remember(existing?.id) { mutableStateOf(existing?.colorArgb ?: categoryColors[0]) }
    var icon by remember(existing?.id) { mutableStateOf(existing?.iconKey ?: "category") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New category" else "Edit category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text("Color", style = MaterialTheme.typography.labelMedium, color = colors.muted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryColors.forEach { value ->
                        Surface(
                            onClick = { color = value },
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier.size(30.dp),
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier
                                        .size(if (color == value) 24.dp else 18.dp)
                                        .clip(CircleShape)
                                        .background(Color(value)),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Icon", style = MaterialTheme.typography.labelMedium, color = colors.muted)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(IconCatalog.categoryChoices) { key ->
                        val selected = icon == key
                        Surface(
                            onClick = { icon = key },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) colors.lavender else colors.background,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    IconCatalog.icon(key),
                                    contentDescription = key,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        colors.muted
                                    },
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), color, icon) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

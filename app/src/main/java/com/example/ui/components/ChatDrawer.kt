@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ChatThread
import com.example.ui.theme.Spacing

private val PillShape = RoundedCornerShape(28.dp)

@Composable
fun ChatDrawerContent(
    allThreads: List<ChatThread>,
    currentThreadId: String?,
    onThreadSelected: (String) -> Unit,
    onNewChatClicked: () -> Unit,
    onDeleteThread: (ChatThread) -> Unit,
    onRenameThread: (ChatThread, String) -> Unit,
    onSettingsClicked: () -> Unit,
    onCloseDrawer: (() -> Unit)? = null,
) {
    var threadToRename by remember { mutableStateOf<ChatThread?>(null) }
    var threadToDelete by remember { mutableStateOf<ChatThread?>(null) }

    threadToRename?.let { thread ->
        var renameText by remember { mutableStateOf(thread.title) }
        AlertDialog(
            onDismissRequest = { threadToRename = null },
            title = { Text("Rename conversation") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().testTag("rename_input_field"),
                )
            },
            confirmButton = { TextButton(onClick = { onRenameThread(thread, renameText); threadToRename = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { threadToRename = null }) { Text("Cancel") } },
        )
    }

    threadToDelete?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, null) },
            title = { Text("Delete conversation?") },
            text = { Text("This conversation will be permanently removed. This can't be undone.") },
            confirmButton = { TextButton(onClick = { onDeleteThread(thread); threadToDelete = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { threadToDelete = null }) { Text("Cancel") } },
        )
    }

    Column(
        Modifier
            .fillMaxHeight()
            .widthIn(max = 340.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = Spacing.m),
    ) {
        // Brand header — same mark as the rest of the app for cohesion.
        Row(
            Modifier.fillMaxWidth().padding(start = Spacing.s, top = Spacing.base, bottom = Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(size = 40.dp)
            Spacer(Modifier.width(Spacing.m))
            Text("AVS Chat", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }

        // Primary action — filled pill.
        Button(
            onClick = { onNewChatClicked(); onCloseDrawer?.invoke() },
            shape = PillShape,
            contentPadding = PaddingValues(vertical = Spacing.base),
            modifier = Modifier.fillMaxWidth().testTag("drawer_new_chat_button"),
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(Spacing.s))
            Text("New conversation", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(Spacing.l))
        SectionLabel("Recent")

        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            items(allThreads, key = { it.id }) { thread ->
                ThreadPill(
                    thread = thread,
                    selected = thread.id == currentThreadId,
                    onClick = { onThreadSelected(thread.id); onCloseDrawer?.invoke() },
                    onRename = { threadToRename = thread },
                    onDelete = { threadToDelete = thread },
                )
            }
        }

        HorizontalDivider(Modifier.padding(vertical = Spacing.s), color = MaterialTheme.colorScheme.outlineVariant)

        // Settings as a pill nav item.
        Surface(
            onClick = { onSettingsClicked(); onCloseDrawer?.invoke() },
            shape = PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(horizontal = Spacing.base, vertical = Spacing.base), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.base))
                Text("Settings", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(Spacing.s))
    }
}

@Composable
private fun ThreadPill(
    thread: ChatThread,
    selected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = PillShape,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(start = Spacing.base, end = Spacing.xs).heightIn(min = 52.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Chat, null, Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Spacing.m))
            Text(
                thread.title,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, "Rename", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Delete", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

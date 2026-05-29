package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatThread
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatDrawerContent(
    allThreads: List<ChatThread>,
    currentThreadId: String?,
    onThreadSelected: (String) -> Unit,
    onNewChatClicked: () -> Unit,
    onDeleteThread: (ChatThread) -> Unit,
    onRenameThread: (ChatThread, String) -> Unit,
    onSettingsClicked: () -> Unit,
    onCloseDrawer: (() -> Unit)? = null
) {
    var threadToRename by remember { mutableStateOf<ChatThread?>(null) }
    var threadToDelete by remember { mutableStateOf<ChatThread?>(null) }

    // Dialog sheets
    if (threadToRename != null) {
        RenameDialog(
            thread = threadToRename!!,
            onDismiss = { threadToRename = null },
            onConfirm = { renameTitle ->
                onRenameThread(threadToRename!!, renameTitle)
                threadToRename = null
            }
        )
    }

    if (threadToDelete != null) {
        DeleteConfirmDialog(
            thread = threadToDelete!!,
            onDismiss = { threadToDelete = null },
            onConfirm = {
                onDeleteThread(threadToDelete!!)
                threadToDelete = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
            .widthIn(max = 300.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // App header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "OpenRouter",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (onCloseDrawer != null) {
                IconButton(onClick = onCloseDrawer) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Drawer")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // New Chat Button
        Button(
            onClick = {
                onNewChatClicked()
                onCloseDrawer?.invoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("drawer_new_chat_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "New Conversation", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Recents",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Threads Scroll list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (allThreads.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved histories yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            items(allThreads, key = { it.id }) { thread ->
                val isSelected = thread.id == currentThreadId
                
                ListItem(
                    headlineContent = {
                        Text(
                            text = thread.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    supportingContent = {
                        Text(
                            text = formatTimestamp(thread.updatedAt),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = { threadToRename = thread },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { threadToDelete = thread },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        headlineColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                        supportingColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = {
                                onThreadSelected(thread.id)
                                onCloseDrawer?.invoke()
                            },
                            onLongClick = {
                                threadToRename = thread
                            }
                        )
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Nav Settings click
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    onSettingsClicked()
                    onCloseDrawer?.invoke()
                }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Addons & Settings",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun RenameDialog(
    thread: ChatThread,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var titleText by remember { mutableStateOf(thread.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Rename Conversation") },
        text = {
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text(text = "Chat Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_input_field")
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(titleText) },
                enabled = titleText.trim().isNotEmpty()
            ) {
                Text(text = "Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    thread: ChatThread,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Conversation") },
        text = {
            Text(text = "Are you absolutely sure you want to delete \"${thread.title}\"? This operation is local and permanent.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Delete", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Just now"
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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

    if (threadToRename != null) {
        var renameText by remember { mutableStateOf(threadToRename!!.title) }
        AlertDialog(
            onDismissRequest = { threadToRename = null },
            title = { Text("Rename conversation") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rename_input_field")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameThread(threadToRename!!, renameText)
                        threadToRename = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { threadToRename = null }) { Text("Cancel") }
            }
        )
    }

    if (threadToDelete != null) {
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            title = { Text("Delete conversation") },
            text = { Text("Are you sure you want to delete this conversation? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteThread(threadToDelete!!)
                        threadToDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { threadToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .widthIn(max = 280.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // App header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Spa, null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("AVS Chat", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Chat Button
        Surface(
            onClick = {
                onNewChatClicked()
                onCloseDrawer?.invoke()
            },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth().testTag("drawer_new_chat_button")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("New Conversation", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Conversations",
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(allThreads, key = { it.id }) { thread ->
                val isSelected = thread.id == currentThreadId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onThreadSelected(thread.id)
                            onCloseDrawer?.invoke()
                        }
                        .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline, 
                        contentDescription = null, 
                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = thread.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (isSelected) {
                        IconButton(onClick = { threadToRename = thread }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.7f))
                        }
                        IconButton(onClick = { threadToDelete = thread }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Settings 
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onSettingsClicked()
                    onCloseDrawer?.invoke()
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Settings", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
    }
}

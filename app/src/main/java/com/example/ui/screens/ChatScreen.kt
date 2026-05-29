package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChatMessage
import com.example.ui.ChatViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.components.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    onMenuClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Chat viewmodel updates
    val allThreads by chatViewModel.allThreads.collectAsState()
    val currentThreadId by chatViewModel.currentChatThreadId.collectAsState()
    val messages by chatViewModel.currentMessages.collectAsState()
    val isStreaming by chatViewModel.isStreaming.collectAsState()
    val streamingBuffer by chatViewModel.activeStreamingBuffer.collectAsState()
    val progressLoading by chatViewModel.apiProgressLoading.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()

    // File attachments states
    val pendingUri by chatViewModel.pendingAttachmentUri.collectAsState()
    val pendingName by chatViewModel.pendingAttachmentName.collectAsState()

    // Settings / Models states
    val selectedModelID by settingsViewModel.selectedModel.collectAsState()
    val customModelsList by settingsViewModel.customModels.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Quick selection model menu dropdown
    var showModelMenu by remember { mutableStateOf(false) }

    val standardModels = listOf(
        "google/gemini-2.0-flash" to "Gemini 2.0 Flash",
        "google/gemini-2.5-pro" to "Gemini 2.5 Pro",
        "openai/gpt-4o-mini" to "GPT-4o Mini",
        "openai/gpt-4o" to "GPT-4o",
        "anthropic/claude-3.5-sonnet" to "Claude 3.5 Sonnet",
        "meta-llama/llama-3.3-70b-instruct" to "Llama 3.3 70B",
        "deepseek/deepseek-chat" to "DeepSeek Chat"
    )

    val activeModelList = remember(customModelsList) {
        val list = standardModels.toMutableList()
        customModelsList.forEach { custom ->
            if (list.none { it.first == custom.id }) {
                list.add(custom.id to custom.name)
            }
        }
        list
    }

    // Determine title display
    val currentThreadTitle = allThreads.firstOrNull { it.id == currentThreadId }?.title ?: "OpenRouter Chat"

    // Photo picker setup
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                chatViewModel.setPendingAttachment(uri)
            }
        }
    )

    // AutoScroll to bottom on new messages
    LaunchedEffect(messages.size, streamingBuffer.length, progressLoading) {
        val totalItems = messages.size + (if (isStreaming || progressLoading) 1 else 0)
        if (totalItems > 0) {
            scope.launch {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { showModelMenu = true },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentThreadTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Selected Model ID subtext
                        val modelShortName = activeModelList.firstOrNull { it.first == selectedModelID }?.second ?: selectedModelID
                        Text(
                            text = modelShortName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
                    }
                },
                actions = {
                    // Quick New Chat Action button
                    IconButton(onClick = { chatViewModel.startNewChat() }) {
                        Icon(imageVector = Icons.Default.AddComment, contentDescription = "New Conversation")
                    }

                    // System settings action link
                    IconButton(onClick = onSettingsClicked) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Addons & Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        // Model Picker dropdown
        if (showModelMenu) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = padding.calculateTopPadding(), start = 56.dp, end = 56.dp)
            ) {
                DropdownMenu(
                    expanded = showModelMenu,
                    onDismissRequest = { showModelMenu = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    activeModelList.forEach { (modelId, name) ->
                        val active = modelId == selectedModelID
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = name,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = modelId,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                settingsViewModel.saveSelectedModel(modelId)
                                showModelMenu = false
                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Error banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        tonalElevation = 4.dp,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { chatViewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Message Board or Empty UI Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isStreaming && !progressLoading) {
                    EmptyChatState(
                        modelName = activeModelList.firstOrNull { it.first == selectedModelID }?.second ?: selectedModelID,
                        selectedModelID = selectedModelID,
                        activeModelList = activeModelList,
                        onModelSelected = { modelId -> settingsViewModel.saveSelectedModel(modelId) },
                        onKeySuggestionClicked = onSettingsClicked,
                        onSuggestionClicked = { suggestion -> textInput = suggestion }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(
                                message = msg,
                                onCopyText = {
                                    clipboardManager.setText(AnnotatedString(msg.content))
                                }
                            )
                        }

                        // API loading state
                        if (progressLoading && streamingBuffer.isEmpty()) {
                            item {
                                AssistantLoadingBubble()
                            }
                        }

                        // Accumulating live streaming state
                        if (streamingBuffer.isNotEmpty()) {
                            item {
                                MessageBubble(
                                    message = ChatMessage(
                                        id = "streaming",
                                        chatId = "temp",
                                        role = "assistant",
                                        content = streamingBuffer,
                                        createdAt = System.currentTimeMillis()
                                    ),
                                    onCopyText = {
                                        clipboardManager.setText(AnnotatedString(streamingBuffer))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom compose controllers: Pending File preview and Input Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Pending Attachment Preview Banner
                AnimatedVisibility(
                    visible = pendingUri != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    if (pendingUri != null) {
                        Surface(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(0.85f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = pendingUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pendingName ?: "Attached Image",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Base64 payload supported",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                IconButton(onClick = { chatViewModel.clearPendingAttachment() }) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Cancel attachment",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Message Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Image Select Button
                            IconButton(
                                onClick = {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .padding(bottom = 2.dp)
                                    .testTag("attach_file_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Attach image",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Input Box text field
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("Message...") },
                                maxLines = 6,
                                singleLine = false,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field")
                            )
                        }
                    }

                    // Send Button
                    FloatingActionButton(
                        onClick = {
                            if ((textInput.trim().isNotEmpty() || pendingUri != null) && !isStreaming) {
                                val toSend = textInput
                                textInput = ""
                                chatViewModel.sendMessage(toSend)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_message_button"),
                        containerColor = if ((textInput.trim().isNotEmpty() || pendingUri != null) && !isStreaming) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if ((textInput.trim().isNotEmpty() || pendingUri != null) && !isStreaming) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatState(
    modelName: String,
    selectedModelID: String,
    activeModelList: List<Pair<String, String>>,
    onModelSelected: (String) -> Unit,
    onKeySuggestionClicked: () -> Unit,
    onSuggestionClicked: (String) -> Unit
) {
    val context = LocalContext.current
    val dynamicPrimary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Expressive Glowing Logo Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                dynamicPrimary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "What can I help you create?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = "A flexible companion powered by $modelName",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Quick Model Selector Chips Row using AssistChip
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show a maximum of 6 active models for rapid chip picking
                    val quickModels = activeModelList.take(6)
                    quickModels.forEach { (modelId, name) ->
                        val isSelected = modelId == selectedModelID
                        val iconVector = when {
                            modelId.contains("gemini") -> Icons.Default.AutoAwesome
                            modelId.contains("gpt") -> Icons.Default.Bolt
                            else -> Icons.Default.Face
                        }

                        AssistChip(
                            onClick = { onModelSelected(modelId) },
                            label = { 
                                Text(
                                    text = name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                leadingIconContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }
            }

            // Quick Starter Action Prompts
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                val suggestions = listOf(
                    Triple("💡 Brainstorm ideas", "Give me 5 creative name ideas for a personal trainer app", MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    Triple("📝 Draft an email", "Write a polite business note asking for feedback on a design", MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
                    Triple("💻 Kotlin helper", "Detail how coroutines and flows work in Android Compose", MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)),
                    Triple("🎨 Design palette", "Produce a cozy autumn color scheme with matching hex coordinates", MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                )

                suggestions.forEach { (label, value, tintColor) ->
                    ElevatedCard(
                        onClick = { onSuggestionClicked(value) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tintColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.take(2),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label.substring(2),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = value,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Use prompt",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .clickable { onKeySuggestionClicked() }
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify OpenRouter API Key",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onCopyText: () -> Unit
) {
    val isUser = message.role == "user"
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val bubbleTextColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp, top = 2.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                contentColor = bubbleTextColor,
                tonalElevation = if (isUser) 0.dp else 2.dp,
                shadowElevation = if (isUser) 1.dp else 0.dp,
                border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Render image inside bubble if any
                    message.localAttachmentUri?.let { uri ->
                        AsyncImage(
                            model = Uri.parse(uri),
                            contentDescription = "Attached media",
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Render dynamic markdown text
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge.copy(color = bubbleTextColor)
                        )
                    } else {
                        MarkdownText(
                            text = message.content,
                            textColor = bubbleTextColor
                        )
                    }
                }
            }

            // Simple micro-operations footer for Assistant bubbles
            if (!isUser && message.content.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopyText,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssistantLoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(end = 10.dp, top = 2.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Connecting to OpenRouter...",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    onMenuClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    
    val allThreads by chatViewModel.allThreads.collectAsState()
    val currentThreadId by chatViewModel.currentChatThreadId.collectAsState()
    val messages by chatViewModel.currentMessages.collectAsState()
    val isStreaming by chatViewModel.isStreaming.collectAsState()
    val streamingBuffer by chatViewModel.activeStreamingBuffer.collectAsState()
    val progressLoading by chatViewModel.apiProgressLoading.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()
    
    val pendingUri by chatViewModel.pendingAttachmentUri.collectAsState()
    val pendingName by chatViewModel.pendingAttachmentName.collectAsState()
    val selectedModelID by settingsViewModel.selectedModel.collectAsState()
    val customModelsList by settingsViewModel.customModels.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var modelSearchQuery by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    val modelShortName = activeModelList.firstOrNull { it.first == selectedModelID }?.second ?: selectedModelID

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                chatViewModel.setPendingAttachment(uri)
            }
        }
    )

    LaunchedEffect(messages.size, streamingBuffer.length, progressLoading) {
        val totalItems = messages.size + (if (isStreaming || progressLoading) 1 else 0)
        if (totalItems > 0) {
            try {
                listState.animateScrollToItem(totalItems - 1)
            } catch (e: Exception) {
                listState.scrollToItem(totalItems - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        onClick = { showModelMenu = true },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AVS Chat",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = modelShortName,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { chatViewModel.startNewChat() }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "New Conversation", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(padding)
                .padding(bottom = 16.dp) // extra padding for bottom float
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Error Message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    errorMessage?.let { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(Modifier.width(12.dp))
                                Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                                IconButton(onClick = { chatViewModel.clearError() }) {
                                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }

                // Chat Messages Space
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (messages.isEmpty() && !isStreaming && !progressLoading) {
                        CleanEmptyState(onSuggestionClicked = { textInput = it })
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                ModernMessageBubble(
                                    message = msg,
                                    onCopyText = { clipboardManager.setText(AnnotatedString(msg.content)) }
                                )
                            }
                            if (progressLoading && streamingBuffer.isEmpty()) {
                                item { LoadingDotAnimation() }
                            }
                            if (streamingBuffer.isNotEmpty()) {
                                item {
                                    ModernMessageBubble(
                                        message = ChatMessage(id = "streaming", chatId = "temp", role = "assistant", content = streamingBuffer, createdAt = System.currentTimeMillis()),
                                        onCopyText = { clipboardManager.setText(AnnotatedString(streamingBuffer)) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Input Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Attachment preview
                    AnimatedVisibility(visible = pendingUri != null) {
                        pendingUri?.let { uri ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = pendingName ?: "Attachment",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { chatViewModel.clearPendingAttachment() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Sleek Floating Text Input Box
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shadowElevation = 8.dp, // High premium drop shadow
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                                    .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attach image", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            TextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("Ask anything...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f), fontWeight = FontWeight.Medium) },
                                maxLines = 6,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f).testTag("chat_input_field")
                            )

                            val hasContent = textInput.trim().isNotEmpty() || pendingUri != null
                            
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (hasContent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = hasContent && !isStreaming) {
                                        if (hasContent && !isStreaming) {
                                            val toSend = textInput
                                            textInput = ""
                                            chatViewModel.sendMessage(toSend)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = if (hasContent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Model Picker Overlay
            if (showModelMenu) {
                ModalBottomSheet(
                    onDismissRequest = { showModelMenu = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Select Engine",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = modelSearchQuery,
                            onValueChange = { modelSearchQuery = it },
                            placeholder = { Text("Search models...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)
                            )
                        )

                        val filteredModels = activeModelList.filter { 
                            it.second.contains(modelSearchQuery, ignoreCase = true) || it.first.contains(modelSearchQuery, ignoreCase = true) 
                        }

                        LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            items(filteredModels) { (modelId, name) ->
                                val isSelected = modelId == selectedModelID
                                Surface(
                                    onClick = { 
                                        settingsViewModel.saveSelectedModel(modelId)
                                        showModelMenu = false 
                                    },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.2f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.5f)) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Memory,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            val provider = if (modelId.contains("/")) modelId.substringBefore("/").replaceFirstChar { it.uppercase() } else "Custom Model"
                                            Text(
                                                text = provider,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CleanEmptyState(onSuggestionClicked: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
            }
            Text("How can I help you today?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val suggestions = listOf(
                    "Explain quantum computing simply" to Icons.Default.Lightbulb,
                    "Write an email requesting a deadline extension" to Icons.Default.Email,
                    "Suggest a 3-day itinerary for Tokyo" to Icons.Default.FlightTakeoff,
                )
                suggestions.forEach { (text, icon) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
                        modifier = Modifier.fillMaxWidth().clickable { onSuggestionClicked(text) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernMessageBubble(message: ChatMessage, onCopyText: () -> Unit) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp, top = 4.dp)
                    .size(28.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
            }
        }
        
        Column(modifier = Modifier.weight(1f, fill=false)) {
            if (message.localAttachmentUri != null) {
                AsyncImage(
                    model = message.localAttachmentUri,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 8.dp).size(200.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            val bubbleShape = if (isUser) {
                RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp)
            } else {
                RoundedCornerShape(4.dp, 24.dp, 24.dp, 24.dp)
            }

            if (isUser) {
                Surface(
                    shape = bubbleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shadowElevation = 0.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(message.content, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    MarkdownText(text = message.content)
                }
            }
        }
    }
}

@Composable
fun LoadingDotAnimation() {
    Row(
        modifier = Modifier.padding(start = 44.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(8.dp))
        Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

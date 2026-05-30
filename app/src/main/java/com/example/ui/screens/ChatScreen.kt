@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.ChatMessage
import com.example.ui.ChatViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.components.BrandMark
import com.example.ui.components.MarkdownText
import com.example.ui.theme.BrandShapes
import com.example.ui.theme.MorphPolygonShape
import com.example.ui.theme.Spacing
import com.example.ui.theme.rememberMorph
import com.example.ui.theme.rememberMorphProgress

/** Single built-in model. Everything else the user adds in Settings. */
private val DEFAULT_MODEL = "google/gemini-2.0-flash" to "Gemini 2.0 Flash"

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    onMenuClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current

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
    var showModelMenu by remember { mutableStateOf(false) }

    // Stick-to-bottom that respects manual scrolling: only auto-follow while the user is already
    // viewing the latest message. If they scroll up, streaming stops yanking them back.
    var autoFollow by remember { mutableStateOf(true) }
    val atBottom by remember {
        derivedStateOf {
            val li = listState.layoutInfo
            val last = li.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
            // Last item is the newest AND its bottom edge has been reached.
            last.index >= li.totalItemsCount - 1 && (last.offset + last.size) <= li.viewportEndOffset + 8
        }
    }

    val activeModelList = remember(customModelsList) {
        val list = mutableListOf(DEFAULT_MODEL)
        customModelsList.forEach { custom -> if (list.none { it.first == custom.id }) list.add(custom.id to custom.name) }
        list
    }
    val modelShortName = activeModelList.firstOrNull { it.first == selectedModelID }?.second
        ?: customModelsList.firstOrNull { it.id == selectedModelID }?.name
        ?: selectedModelID

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) chatViewModel.setPendingAttachment(uri) },
    )

    // Update the follow flag only from user-driven scrolling.
    LaunchedEffect(atBottom, listState.isScrollInProgress) {
        if (listState.isScrollInProgress) autoFollow = atBottom
    }
    // Snap (not animate) to the very bottom on new content while following — smooth, no fighting.
    LaunchedEffect(messages.size, streamingBuffer, progressLoading) {
        if (autoFollow) {
            val idx = listState.layoutInfo.totalItemsCount - 1
            if (idx >= 0) runCatching { listState.scrollToItem(idx, Int.MAX_VALUE) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        // Let the input toolbar handle the bottom (ime/nav-bar) inset itself so there is no dead
        // gap between the field and the keyboard.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ChatTopBar(
                modelName = modelShortName,
                onMenu = onMenuClicked,
                onModel = { showModelMenu = true },
                onNewChat = { chatViewModel.startNewChat() },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                errorMessage?.let { ErrorBanner(it) { chatViewModel.clearError() } }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty() && !isStreaming && !progressLoading) {
                    EmptyState { textInput = it }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.l),
                        contentPadding = PaddingValues(horizontal = Spacing.base, vertical = Spacing.l),
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(msg) { clipboard.setText(AnnotatedString(msg.content)) }
                        }
                        if (progressLoading && streamingBuffer.isEmpty()) item { ThinkingRow() }
                        if (streamingBuffer.isNotEmpty()) item(key = "streaming") {
                            MessageBubble(
                                ChatMessage("streaming", "temp", "assistant", streamingBuffer, System.currentTimeMillis()),
                                streaming = true,
                            ) { clipboard.setText(AnnotatedString(streamingBuffer)) }
                        }
                    }
                }
            }

            InputToolbar(
                text = textInput,
                onText = { textInput = it },
                pendingUri = pendingUri?.toString(),
                pendingName = pendingName,
                onClearAttachment = { chatViewModel.clearPendingAttachment() },
                onAttach = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                isStreaming = isStreaming,
                onSend = { val t = textInput; textInput = ""; chatViewModel.sendMessage(t) },
            )
        }
    }

    if (showModelMenu) {
        ModelPickerSheet(
            models = activeModelList,
            selectedId = selectedModelID,
            onSelect = { settingsViewModel.saveSelectedModel(it); showModelMenu = false },
            onManage = { showModelMenu = false; onSettingsClicked() },
            onDismiss = { showModelMenu = false },
        )
    }
}

@Composable
private fun ChatTopBar(modelName: String, onMenu: () -> Unit, onModel: () -> Unit, onNewChat: () -> Unit) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            FilledTonalIconButton(onClick = onMenu) { Icon(Icons.Default.Menu, "Open conversations") }
        },
        title = {
            // Themed model selector pill — uses secondaryContainer so dynamic color shows here too.
            Surface(onClick = onModel, shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(
                    Modifier.padding(start = Spacing.base, end = Spacing.m, top = Spacing.s, bottom = Spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modelName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 170.dp),
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        },
        actions = {
            FilledTonalIconButton(onClick = onNewChat) { Icon(Icons.Default.Add, "New conversation") }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.base, vertical = Spacing.s),
    ) {
        Row(Modifier.padding(Spacing.base), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(Spacing.m))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer) }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, modifier: Modifier = Modifier, streaming: Boolean = false, onCopy: () -> Unit) {
    val isUser = message.role == "user"
    if (isUser) {
        Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(max = 320.dp)) {
                message.localAttachmentUri?.let {
                    AsyncImage(it, null, Modifier.padding(bottom = Spacing.s).size(200.dp).clip(MaterialTheme.shapes.large), contentScale = ContentScale.Crop)
                }
                Surface(
                    shape = RoundedCornerShape(26.dp, 26.dp, 8.dp, 26.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(message.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 18.dp, vertical = Spacing.m))
                }
            }
        }
    } else {
        // ChatGPT / Claude style: no bubble, full content width.
        Column(modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(size = 26.dp, animated = streaming)
                Spacer(Modifier.width(Spacing.s))
                Text("AVS Chat", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(Spacing.s))
            message.localAttachmentUri?.let {
                AsyncImage(it, null, Modifier.padding(bottom = Spacing.s).size(200.dp).clip(MaterialTheme.shapes.large), contentScale = ContentScale.Crop)
            }
            if (streaming) {
                // While generating, render plain stable text so existing words never re-flow
                // (no per-token markdown re-parse). Full markdown renders once the message lands.
                SelectionContainer {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                MarkdownText(text = message.content, modifier = Modifier.fillMaxWidth())
            }
            if (!streaming) {
                Spacer(Modifier.height(Spacing.xs))
                FilledTonalIconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) { Icon(Icons.Default.ContentCopy, "Copy", Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
private fun ThinkingRow(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        BrandMark(modifier = Modifier.padding(end = Spacing.m), size = 32.dp, animated = true)
        LoadingIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(Spacing.s))
        Text("Thinking…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState(onSuggestion: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Haloed, morphing hero — a strong colored focal point.
        Box(contentAlignment = Alignment.Center) {
            val morph = rememberMorph(BrandShapes.heroStart, BrandShapes.heroEnd)
            val progress by rememberMorphProgress(3400)
            Box(
                Modifier.size(150.dp).clip(MorphPolygonShape(morph, progress)).background(MaterialTheme.colorScheme.primaryContainer),
            )
            BrandMark(size = 84.dp, animated = true, iconScale = 0.42f)
        }
        Spacer(Modifier.height(Spacing.xl))
        Text(
            "How can I help\nyou today?",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xl))

        data class Sug(val icon: ImageVector, val label: String, val prompt: String, val container: Color, val onContainer: Color)
        val cs = MaterialTheme.colorScheme
        val suggestions = listOf(
            Sug(Icons.Default.Lightbulb, "Explain", "Explain quantum computing in simple terms", cs.primaryContainer, cs.onPrimaryContainer),
            Sug(Icons.Default.Edit, "Write", "Write an email asking for a deadline extension", cs.secondaryContainer, cs.onSecondaryContainer),
            Sug(Icons.Default.Map, "Plan", "Plan a 3-day itinerary for Tokyo", cs.tertiaryContainer, cs.onTertiaryContainer),
            Sug(Icons.Default.Code, "Code", "Write a Python script to rename files in a folder", cs.surfaceContainerHigh, cs.onSurface),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier.fillMaxWidth(),
        ) {
            suggestions.forEach { s ->
                AssistPill(s.icon, s.label, s.container, s.onContainer) { onSuggestion(s.prompt) }
            }
        }
    }
}

@Composable
private fun AssistPill(icon: ImageVector, label: String, container: Color, onContainer: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = container) {
        Row(
            Modifier.padding(start = 14.dp, end = 18.dp, top = Spacing.m, bottom = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = onContainer)
            Spacer(Modifier.width(Spacing.s))
            Text(label, style = MaterialTheme.typography.labelLarge, color = onContainer)
        }
    }
}

@Composable
private fun InputToolbar(
    text: String,
    onText: (String) -> Unit,
    pendingUri: String?,
    pendingName: String?,
    onClearAttachment: () -> Unit,
    onAttach: () -> Unit,
    isStreaming: Boolean,
    onSend: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            .padding(horizontal = Spacing.base, vertical = Spacing.m),
    ) {
        AnimatedVisibility(visible = pendingUri != null) {
            pendingUri?.let { uri ->
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(bottom = Spacing.s),
                ) {
                    Row(Modifier.padding(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(uri, null, Modifier.size(40.dp).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(Spacing.m))
                        Text(pendingName ?: "Attachment", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = onClearAttachment, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Close, "Remove", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) }
                    }
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(
                    onClick = onAttach,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) { Icon(Icons.Outlined.AddPhotoAlternate, "Attach image", Modifier.size(20.dp)) }

                TextField(
                    value = text,
                    onValueChange = onText,
                    placeholder = { Text("Ask anything…") },
                    maxLines = 6,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f).testTag("chat_input_field"),
                )

                val hasContent = text.trim().isNotEmpty() || pendingUri != null
                SendButton(enabled = hasContent && !isStreaming, isStreaming = isStreaming) {
                    if (hasContent && !isStreaming) onSend()
                }
            }
        }
    }
}

@Composable
private fun SendButton(enabled: Boolean, isStreaming: Boolean, onClick: () -> Unit) {
    val container by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "send-color",
    )
    val scale by animateFloatAsState(if (enabled) 1f else 0.85f, label = "send-scale")
    Box(
        Modifier.size(48.dp).scale(scale).clip(CircleShape).background(container),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isStreaming -> LoadingIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            else -> IconButton(onClick = onClick, enabled = enabled) {
                Icon(
                    Icons.AutoMirrored.Filled.Send, "Send", Modifier.size(20.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ModelPickerSheet(
    models: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onManage: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.base)) {
                Text("Choose a model", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                TextButton(onClick = onManage) {
                    Icon(Icons.Default.Tune, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.s))
                    Text("Manage")
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search models") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.m))
            val filtered = models.filter { it.second.contains(query, true) || it.first.contains(query, true) }
            if (filtered.isEmpty()) {
                Text(
                    "No models. Tap Manage to add one in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.xl),
                )
            }
            LazyColumn(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                contentPadding = PaddingValues(bottom = Spacing.xl),
            ) {
                items(filtered, key = { it.first }) { (id, name) ->
                    ModelRow(name, id, id == selectedId) { onSelect(id) }
                }
            }
        }
    }
}

@Composable
private fun ModelRow(name: String, modelId: String, selected: Boolean, onClick: () -> Unit) {
    val provider = if (modelId.contains("/")) modelId.substringBefore("/").replaceFirstChar { it.uppercase() } else "Custom"
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.base), verticalAlignment = Alignment.CenterVertically) {
            BrandMark(size = 40.dp)
            Spacer(Modifier.width(Spacing.base))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                Text(provider, style = MaterialTheme.typography.bodySmall, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

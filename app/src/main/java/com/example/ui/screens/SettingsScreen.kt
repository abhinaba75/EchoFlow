package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClicked: () -> Unit
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val selectedModelID by viewModel.selectedModel.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val darkModeSec by viewModel.darkMode.collectAsState()
    val customModelsList by viewModel.customModels.collectAsState()

    // Inputs States
    var keyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var keyVisible by remember { mutableStateOf(false) }

    var modelIdInput by remember { mutableStateOf("") }
    var modelNameInput by remember { mutableStateOf("") }

    // Merge standard models and custom models for selecting default picker
    val standardModels = listOf(
        "google/gemini-2.0-flash" to "Gemini 2.0 Flash (Default)",
        "google/gemini-2.5-pro" to "Gemini 2.5 Pro",
        "openai/gpt-4o-mini" to "GPT-4o Mini",
        "openai/gpt-4o" to "GPT-4o",
        "anthropic/claude-3.5-sonnet" to "Claude 3.5 Sonnet",
        "meta-llama/llama-3.3-70b-instruct" to "Llama 3.3 70B",
        "deepseek/deepseek-chat" to "DeepSeek Chat"
    )

    val mergedModels = remember(customModelsList) {
        val list = standardModels.toMutableList()
        customModelsList.forEach { custom ->
            if (list.none { it.first == custom.id }) {
                list.add(custom.id to custom.name)
            }
        }
        list
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Addons & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return Chat")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: OpenRouter API key setting
            item {
                Text(
                    text = "API Configuration",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OpenRouter Token",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To access cloud models, retrieve a key from openrouter.ai",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = { Text("API Key") },
                            placeholder = { Text("sk-or-v1-...") },
                            singleLine = true,
                            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(
                                        imageVector = if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle Key View"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    keyInput = ""
                                    viewModel.saveApiKey("")
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Key")
                            }

                            FilledTonalButton(
                                onClick = { viewModel.saveApiKey(keyInput.trim()) },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Key")
                            }
                        }
                    }
                }
            }

            // Section 2: Custom Model IDs
            item {
                Text(
                    text = "Model Management",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Add OpenRouter Model",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Manually declare any active OpenRouter model id.",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = modelIdInput,
                            onValueChange = { modelIdInput = it },
                            label = { Text("Model ID") },
                            placeholder = { Text("meta-llama/llama-3.1-70b-instruct") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = modelNameInput,
                            onValueChange = { modelNameInput = it },
                            label = { Text("Model Display Name") },
                            placeholder = { Text("Llama 3.1 70B") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (modelIdInput.trim().isNotEmpty()) {
                                    viewModel.addCustomModel(modelIdInput.trim(), modelNameInput.trim())
                                    modelIdInput = ""
                                    modelNameInput = ""
                                }
                            },
                            enabled = modelIdInput.trim().isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(imageVector = Icons.Default.AddHome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Custom Model")
                        }
                    }
                }
            }

            // List of Added Models
            if (customModelsList.isNotEmpty()) {
                item {
                    Text(
                        text = "Dynamic Custom Catalog",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(customModelsList) { customModel ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = {
                                Text(
                                    text = customModel.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = customModel.id,
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteCustomModel(customModel.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove model",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Section 3: Chosen Theme styling
            item {
                Text(
                    text = "Aesthetics & Interface",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Material You Theme Engine",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic wallpaper matching item
                        val hasDynamicSupport = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (themeColor == "dynamic") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(
                                width = if (themeColor == "dynamic") 2.dp else 1.dp,
                                color = if (themeColor == "dynamic") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.saveThemeColor("dynamic") }
                                .testTag("theme_color_dynamic")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFF4285F4),
                                                    Color(0xFF34A853),
                                                    Color(0xFFFBBC05),
                                                    Color(0xFFEA4335),
                                                    Color(0xFF4285F4)
                                                )
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Adaptive Wallpaper Match",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (hasDynamicSupport) "Harmonizes live with system colors" else "Requires Android 12+ (Falls back to Blue)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (themeColor == "dynamic") {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Preset Color Harmonies",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Theme selector buttons
                        val colorsMap = listOf(
                            "blue" to Color(0xFF0F5CC0),
                            "purple" to Color(0xFF8634B5),
                            "green" to Color(0xFF046D38),
                            "orange" to Color(0xFF904E00),
                            "pink" to Color(0xFF9E2A5D),
                            "neutral" to Color(0xFF4F5F6B)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colorsMap.forEach { (colorName, value) ->
                                val active = themeColor == colorName
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(value)
                                        .border(
                                            width = if (active) 3.dp else 1.dp,
                                            color = if (active) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.saveThemeColor(colorName) }
                                        .testTag("theme_color_${colorName}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (active) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Light/Dark Contrast Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val modes = listOf(
                            "system" to "System",
                            "light" to "Light",
                            "dark" to "Dark"
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            modes.forEachIndexed { index, (modeValue, modeLabel) ->
                                SegmentedButton(
                                    selected = darkModeSec == modeValue,
                                    onClick = { viewModel.saveDarkMode(modeValue) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                                ) {
                                    Text(text = modeLabel)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Default Model Selector
                        Text(
                            text = "Default Startup Model",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New chat instances will trigger this OpenRouter target profile.",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        var dropdownExpanded by remember { mutableStateOf(false) }
                        val currentLabel = mergedModels.firstOrNull { it.first == selectedModelID }?.second ?: selectedModelID

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = currentLabel,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = selectedModelID,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                mergedModels.forEach { (modelId, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = name, fontWeight = FontWeight.SemiBold)
                                                Text(text = modelId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            viewModel.saveSelectedModel(modelId)
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

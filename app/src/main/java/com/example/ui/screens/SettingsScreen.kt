package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClicked: () -> Unit
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val darkModeSec by viewModel.darkMode.collectAsState()
    val customModelsList by viewModel.customModels.collectAsState()

    var keyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var modelIdInput by remember { mutableStateOf("") }
    var modelNameInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return Chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
            // Appearance Section
            item {
                SectionHeader("Appearance")
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Light / Dark Theme", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(16.dp))
                        
                        val modes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            modes.forEachIndexed { index, (modeValue, modeLabel) ->
                                SegmentedButton(
                                    selected = darkModeSec == modeValue,
                                    onClick = { viewModel.saveDarkMode(modeValue) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                                ) {
                                    Text(modeLabel)
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Accent Color", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(16.dp))
                        
                        val currentTheme = viewModel.themeColor.collectAsState().value
                        var expandedThemeMenu by remember { mutableStateOf(false) }
                        val themeOptions = listOf(
                            "monochrome" to "Monochrome (Space)",
                            "ocean" to "Ocean (Blue)",
                            "forest" to "Forest (Green)",
                            "sunset" to "Sunset (Orange)",
                            "lavender" to "Lavender (Purple)",
                            "rose" to "Rose (Pink)"
                        )
                        val selectedThemeLabel = themeOptions.firstOrNull { it.first == currentTheme }?.second ?: "Monochrome"
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedThemeMenu,
                            onExpandedChange = { expandedThemeMenu = !expandedThemeMenu }
                        ) {
                            OutlinedTextField(
                                value = selectedThemeLabel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedThemeMenu) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedThemeMenu,
                                onDismissRequest = { expandedThemeMenu = false }
                            ) {
                                themeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.second) },
                                        onClick = {
                                            viewModel.saveThemeColor(option.first)
                                            expandedThemeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // API Section
            item {
                SectionHeader("OpenRouter API")
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("API Key", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            placeholder = { Text("sk-or-v1-...") },
                            singleLine = true,
                            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.saveApiKey(keyInput.trim()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }

            // Custom Models Section
            item {
                SectionHeader("Models")
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Custom Model", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = modelIdInput,
                            onValueChange = { modelIdInput = it },
                            label = { Text("Model ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = modelNameInput,
                            onValueChange = { modelNameInput = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (modelIdInput.trim().isNotEmpty()) {
                                    viewModel.addCustomModel(modelIdInput.trim(), modelNameInput.trim())
                                    modelIdInput = ""
                                    modelNameInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add Custom Model")
                        }
                    }
                }
            }

            if (customModelsList.isNotEmpty()) {
                items(customModelsList) { customModel ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(customModel.name, fontWeight = FontWeight.Bold)
                                Text(customModel.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteCustomModel(customModel.id) }) {
                                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
    )
}

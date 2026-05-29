package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.filled.Settings
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.ui.ChatViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.components.ChatDrawerContent
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initializing Local Repositories and SQLite Database
        val database = AppDatabase.getDatabase(application)
        val settingsRepo = SettingsRepository(applicationContext)

        setContent {
            // 2. Fetch ViewModels using our custom factory providers
            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(
                    settingsRepo,
                    database.customModelDao()
                )
            )

            val chatVm: ChatViewModel = viewModel(
                factory = ChatViewModel.provideFactory(
                    application,
                    database.chatDao(),
                    database.messageDao(),
                    settingsRepo
                )
            )

            // 3. Reacting to global themes selections
            val userThemeColor by settingsVm.themeColor.collectAsState()
            val userDarkModeId by settingsVm.darkMode.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val themeActiveDark = when (userDarkModeId) {
                "light" -> false
                "dark" -> true
                else -> isSystemDark
            }

            MyApplicationTheme(
                darkTheme = themeActiveDark,
                themeName = userThemeColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigationHub(chatVm, settingsVm)
                }
            }
        }
    }
}

@Composable
fun MainNavigationHub(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel
) {
    var activeTab by remember { mutableStateOf("chat") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (activeTab == "settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClicked = { activeTab = "chat" }
            )
        } else {
            AdaptiveChatWorkspace(
                chatViewModel = chatViewModel,
                settingsViewModel = settingsViewModel,
                onSettingsClicked = { activeTab = "settings" }
            )
        }
    }
}

@Composable
fun AdaptiveChatWorkspace(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    onSettingsClicked: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val allConversations by chatViewModel.allThreads.collectAsState()
    val selectedThreadId by chatViewModel.currentChatThreadId.collectAsState()

    // Inspect screen boundaries for Tablet orientation check
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useSplitLayout = maxWidth >= 600.dp

        if (useSplitLayout) {
            // Dual-Pane Slate: Side-by-Side persistent layout for Expanded screens
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                ) {
                    ChatDrawerContent(
                        allThreads = allConversations,
                        currentThreadId = selectedThreadId,
                        onThreadSelected = { id -> chatViewModel.selectThread(id) },
                        onNewChatClicked = { chatViewModel.startNewChat() },
                        onDeleteThread = { t -> chatViewModel.deleteThread(t) },
                        onRenameThread = { t, name -> chatViewModel.renameThread(t, name) },
                        onSettingsClicked = onSettingsClicked
                    )
                }

                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Box(modifier = Modifier.weight(1f)) {
                    ChatScreen(
                        chatViewModel = chatViewModel,
                        settingsViewModel = settingsViewModel,
                        onMenuClicked = { /* Drawer is persistent, no trigger required */ },
                        onSettingsClicked = onSettingsClicked
                    )
                }
            }
        } else {
            // Mobile Sliding Layer: Modal sliding drawers for compact display
            val mobileDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = mobileDrawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color.Transparent,
                        drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                        modifier = Modifier.widthIn(max = 290.dp).padding(end = 0.dp)
                    ) {
                        ChatDrawerContent(
                            allThreads = allConversations,
                            currentThreadId = selectedThreadId,
                            onThreadSelected = { id -> chatViewModel.selectThread(id) },
                            onNewChatClicked = { chatViewModel.startNewChat() },
                            onDeleteThread = { t -> chatViewModel.deleteThread(t) },
                            onRenameThread = { t, name -> chatViewModel.renameThread(t, name) },
                            onSettingsClicked = onSettingsClicked,
                            onCloseDrawer = { scope.launch { mobileDrawerState.close() } }
                        )
                    }
                }
            ) {
                ChatScreen(
                    chatViewModel = chatViewModel,
                    settingsViewModel = settingsViewModel,
                    onMenuClicked = { scope.launch { mobileDrawerState.open() } },
                    onSettingsClicked = onSettingsClicked
                )
            }
        }
    }
}

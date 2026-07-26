package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.YuktaViewModel
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: YuktaViewModel = viewModel()
        YuktaAppMainScreen(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun YuktaAppMainScreen(viewModel: YuktaViewModel) {
  val activeTab by viewModel.activeTab.collectAsState()
  val selectedChatId by viewModel.selectedChatId.collectAsState()
  val activeCall by viewModel.activeCall.collectAsState()
  val viewingStoryUser by viewModel.viewingStoryUser.collectAsState()
  val stories by viewModel.stories.collectAsState()

  val configuration = LocalConfiguration.current
  val isDesktop = configuration.screenWidthDp >= 600

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      // Show bottom navigation bar only for mobile and when NO conversation is selected
      if (!isDesktop && selectedChatId == null) {
        NavigationBar(
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = activeTab == "chats",
            onClick = { viewModel.selectTab("chats") },
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chats") },
            label = { Text("Chats") },
            modifier = Modifier.testTag("tab_chats")
          )
          NavigationBarItem(
            selected = activeTab == "stories",
            onClick = { viewModel.selectTab("stories") },
            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Stories") },
            label = { Text("Stories") },
            modifier = Modifier.testTag("tab_stories")
          )
          NavigationBarItem(
            selected = activeTab == "calls",
            onClick = { viewModel.selectTab("calls") },
            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
            label = { Text("Calls") },
            modifier = Modifier.testTag("tab_calls")
          )
          NavigationBarItem(
            selected = activeTab == "contacts",
            onClick = { viewModel.selectTab("contacts") },
            icon = { Icon(Icons.Default.Groups, contentDescription = "Contacts") },
            label = { Text("Contacts") },
            modifier = Modifier.testTag("tab_contacts")
          )
          NavigationBarItem(
            selected = activeTab == "settings",
            onClick = { viewModel.selectTab("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            modifier = Modifier.testTag("tab_settings")
          )
        }
      }
    }
  ) { innerPadding ->
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          top = innerPadding.calculateTopPadding(),
          bottom = if (isDesktop) innerPadding.calculateBottomPadding() else 0.dp
        )
    ) {
      // Desktop left Navigation Rail
      if (isDesktop) {
        NavigationRail(
          modifier = Modifier
            .fillMaxHeight()
            .testTag("desktop_nav_rail"),
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "YT",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
          )

          NavigationRailItem(
            selected = activeTab == "chats",
            onClick = { viewModel.selectTab("chats") },
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chats") },
            label = { Text("Chats") },
            modifier = Modifier.testTag("rail_chats")
          )
          NavigationRailItem(
            selected = activeTab == "stories",
            onClick = { viewModel.selectTab("stories") },
            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Stories") },
            label = { Text("Stories") },
            modifier = Modifier.testTag("rail_stories")
          )
          NavigationRailItem(
            selected = activeTab == "calls",
            onClick = { viewModel.selectTab("calls") },
            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
            label = { Text("Calls") },
            modifier = Modifier.testTag("rail_calls")
          )
          NavigationRailItem(
            selected = activeTab == "contacts",
            onClick = { viewModel.selectTab("contacts") },
            icon = { Icon(Icons.Default.Groups, contentDescription = "Contacts") },
            label = { Text("Contacts") },
            modifier = Modifier.testTag("rail_contacts")
          )
          NavigationRailItem(
            selected = activeTab == "settings",
            onClick = { viewModel.selectTab("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            modifier = Modifier.testTag("rail_settings")
          )
        }
      }

      // Main content switcher
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(bottom = if (!isDesktop && selectedChatId != null) 0.dp else innerPadding.calculateBottomPadding())
      ) {
        when (activeTab) {
          "chats" -> ChatsScreen(viewModel = viewModel, isDesktop = isDesktop)
          "stories" -> StoriesScreen(viewModel = viewModel)
          "calls" -> CallsScreen(viewModel = viewModel)
          "contacts" -> ContactsScreen(viewModel = viewModel)
          "settings" -> SettingsScreen(viewModel = viewModel)
        }
      }
    }
  }

  // Active Calling overlay trigger
  activeCall?.let { call ->
    CallOverlayScreen(
      call = call,
      onEndCall = { viewModel.endCall() }
    )
  }

  // Active Stories slider overlay trigger
  viewingStoryUser?.let { userId ->
    val userStories = stories.filter { it.userId == userId }
    if (userStories.isNotEmpty()) {
      StoryViewerOverlay(
        viewModel = viewModel,
        stories = userStories,
        onClose = { viewModel.openStoryViewer(null) }
      )
    }
  }
}

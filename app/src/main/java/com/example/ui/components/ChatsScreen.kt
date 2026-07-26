package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatEntity
import com.example.data.MessageEntity
import com.example.data.StoryEntity
import com.example.ui.YuktaViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatsScreen(
    viewModel: YuktaViewModel,
    isDesktop: Boolean
) {
    val selectedChatId by viewModel.selectedChatId.collectAsState()

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left pane - Chats list
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                ChatListPane(viewModel = viewModel)
            }

            // Middle pane - Active conversation
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
            ) {
                if (selectedChatId != null) {
                    ConversationPane(viewModel = viewModel)
                } else {
                    EmptyConversationState()
                }
            }

            // Right pane - Chat details / Info panel
            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                if (selectedChatId != null) {
                    ChatDetailsPane(viewModel = viewModel)
                } else {
                    EmptyDetailsPane()
                }
            }
        }
    } else {
        // Mobile single pane layout
        if (selectedChatId != null) {
            ConversationPane(viewModel = viewModel)
        } else {
            ChatListPane(viewModel = viewModel)
        }
    }
}

@Composable
fun ChatListPane(viewModel: YuktaViewModel) {
    val chats by viewModel.chats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val stories by viewModel.stories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchChats(it) },
            placeholder = { Text("Search chats, groups, channels...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("chat_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Stories bar at the top of the chat list (Snapchat/WhatsApp style)
        if (stories.isNotEmpty()) {
            Text(
                text = "Recent Stories 🔥",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Group stories by userId so we only show one bubble per user
                val uniqueUserStories = stories.associateBy { it.userId }.values.toList()
                items(uniqueUserStories) { story ->
                    StoryBubble(story = story, onClick = { viewModel.openStoryViewer(story.userId) })
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        // Chat list items
        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No active conversations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Go to the Contacts tab to start chatting!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(chats) { chat ->
                    ChatItemRow(chat = chat, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun StoryBubble(
    story: StoryEntity,
    onClick: () -> Unit
) {
    val borderColor = if (story.isViewed) Color.Gray else YuktaPink
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("story_bubble_${story.userId}")
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .border(2.5.dp, borderColor, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            GradientAvatar(
                name = story.userName,
                modifier = Modifier.fillMaxSize(),
                avatarKey = story.userAvatar
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.userName.split(" ").firstOrNull() ?: "",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItemRow(
    chat: ChatEntity,
    viewModel: YuktaViewModel
) {
    val selectedChatId by viewModel.selectedChatId.collectAsState()
    val isSelected = selectedChatId == chat.id
    var showMenu by remember { mutableStateOf(false) }

    val formatTime = { timestamp: Long ->
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.background
                )
                .combinedClickable(
                    onClick = { viewModel.selectChat(chat.id) },
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("chat_item_${chat.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(54.dp)) {
                GradientAvatar(
                    name = chat.title,
                    modifier = Modifier.fillMaxSize(),
                    avatarKey = chat.avatarUrl,
                    shape = RoundedCornerShape(14.dp)
                )
                // Small indicator for pin status
                if (chat.pinned) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTime(chat.lastMessageTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessageText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (chat.pinned) "Unpin Chat" else "Pin Chat") },
                onClick = {
                    viewModel.togglePinChat(chat.id)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Delete Conversation") },
                onClick = {
                    viewModel.deleteChat(chat.id)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}

@Composable
fun ConversationPane(viewModel: YuktaViewModel) {
    val selectedChatId by viewModel.selectedChatId.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val messages by viewModel.filteredMessages.collectAsState()
    val chat = chats.find { it.id == selectedChatId } ?: return

    val chatSearchQuery by viewModel.chatSearchQuery.collectAsState()
    var isSearchingMsg by remember { mutableStateOf(false) }

    var textInput by remember { mutableStateOf("") }
    var sendAsViewOnce by remember { mutableStateOf(false) }

    val smartReplies by viewModel.smartReplies.collectAsState()

    // Screen Overlay for viewing disappearing snaps
    var activeViewingSnap by remember { mutableStateOf<MessageEntity?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll to bottom when message size increases
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF1F5F9)
            )
    ) {
        // Conversation Header
        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectChat(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.width(4.dp))

                GradientAvatar(
                    name = chat.title,
                    modifier = Modifier.size(42.dp),
                    avatarKey = chat.avatarUrl
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chat.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (chat.isChannel) "Broadcast Channel" else if (chat.isGroup) "Group" else "Online",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Call Controls
                if (!chat.isChannel) {
                    IconButton(onClick = { viewModel.startCall(chat.title, chat.avatarUrl, isVideo = false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call")
                    }
                    IconButton(onClick = { viewModel.startCall(chat.title, chat.avatarUrl, isVideo = true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                }

                // Search in Chat toggle
                IconButton(onClick = {
                    isSearchingMsg = !isSearchingMsg
                    if (!isSearchingMsg) viewModel.searchInChat("")
                }) {
                    Icon(
                        imageVector = if (isSearchingMsg) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search messages"
                    )
                }
            }
        }

        // Expanded message search bar
        AnimatedVisibility(visible = isSearchingMsg) {
            OutlinedTextField(
                value = chatSearchQuery,
                onValueChange = { viewModel.searchInChat(it) },
                placeholder = { Text("Search message history...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Messages List Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message ->
                    MessageBubbleRow(
                        message = message,
                        onOpenSnap = { snap ->
                            viewModel.openSnap(snap.id)
                            activeViewingSnap = snap
                        }
                    )
                }
            }
        }

        // Smart Replies bar
        if (smartReplies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                smartReplies.forEach { option ->
                    SuggestionChip(
                        onClick = {
                            viewModel.sendMessage(option)
                        },
                        label = { Text(option) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }

        // Composer / Input Bar
        Surface(
            tonalElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snapchat disappearing toggle icon
                IconButton(
                    onClick = { sendAsViewOnce = !sendAsViewOnce },
                    modifier = Modifier.testTag("snap_toggle")
                ) {
                    Icon(
                        imageVector = if (sendAsViewOnce) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "View once snap mode",
                        tint = if (sendAsViewOnce) YuktaPink else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = if (sendAsViewOnce) "Send disappearing snap..." else "Write a message..."
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_box"),
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = {
                        if (sendAsViewOnce) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Mode", tint = YuktaPink)
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendMessage(
                                content = textInput,
                                type = if (sendAsViewOnce) "snap" else "text",
                                viewOnce = sendAsViewOnce
                            )
                            textInput = ""
                            sendAsViewOnce = false
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_msg_button"),
                    shape = CircleShape,
                    containerColor = if (sendAsViewOnce) YuktaPink else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Active disappearing Snap viewer
    activeViewingSnap?.let { snap ->
        DisappearingSnapViewer(
            snap = snap,
            onClose = { activeViewingSnap = null }
        )
    }
}

@Composable
fun MessageBubbleRow(
    message: MessageEntity,
    onOpenSnap: (MessageEntity) -> Unit
) {
    val isSelf = message.senderId == "self_user"
    val alignment = if (isSelf) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleBg = if (isSelf) {
        if (isSystemInDarkTheme()) MessageSentDark else MessageSentLight
    } else {
        if (isSystemInDarkTheme()) MessageReceivedDark else MessageReceivedLight
    }

    val textColor = if (isSelf) {
        if (isSystemInDarkTheme()) Color.White else YuktaBlueDark
    } else {
        if (isSystemInDarkTheme()) Color.White else Color(0xFF1A1B1F)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
        ) {
            // Sender name (only for groups)
            if (!isSelf) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            // Message Body Card
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isSelf) 16.dp else 0.dp,
                    bottomEnd = if (isSelf) 0.dp else 16.dp
                ),
                color = bubbleBg,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.type == "snap") {
                        // Snapchat disappearing click-once item
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(enabled = !message.isOpened) { onOpenSnap(message) }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (message.isOpened) Icons.Default.Drafts else Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = if (message.isOpened) Color.Gray else YuktaPink,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (message.isOpened) "Snap opened" else "🔥 Disappearing Snap",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (message.isOpened) Color.Gray else YuktaPink
                                )
                                Text(
                                    text = if (message.isOpened) "Double-tap to view is expired" else "Tap once to view!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (message.isOpened) Color.Gray else textColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        // Regular Text Message
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DisappearingSnapViewer(
    snap: MessageEntity,
    onClose: () -> Unit
) {
    var timerSeconds by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
        onClose()
    }

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Custom drawn beautiful snap content with glowing gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF8A2BE2), Color(0xFFFF007F))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = snap.content,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sender: ${snap.senderName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Custom Timer visual progress
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timerSeconds.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ChatDetailsPane(viewModel: YuktaViewModel) {
    val selectedChatId by viewModel.selectedChatId.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val chat = chats.find { it.id == selectedChatId } ?: return

    val summaryText by viewModel.summaryText.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GradientAvatar(
            name = chat.title,
            modifier = Modifier.size(80.dp),
            avatarKey = chat.avatarUrl
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = chat.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (chat.isChannel) "Yukta Broadcast Channel" else if (chat.isGroup) "Group Thread" else "Direct Chat",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // AI Powered summarize group chats ("Catch me up")
        if (chat.isGroup || chat.title.contains("Gemini")) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = YuktaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Catch Me Up! ✨",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = YuktaBlue
                            )
                        }

                        if (isAILoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Button(
                                onClick = { viewModel.summarizeActiveChat() },
                                colors = ButtonDefaults.buttonColors(containerColor = YuktaBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("catch_up_button")
                            ) {
                                Text("Summarize", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    summaryText?.let { summary ->
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.clearSummary() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Dismiss Summary", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    } ?: run {
                        Text(
                            text = "Catch up on the latest messages instantly using Gemini AI summary.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Profile Info or settings list for 1:1 chats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shared Media",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Empty media placeholders
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyConversationState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = null,
                modifier = Modifier.size(90.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to YuktaTele",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select an existing chat on the left or search contacts to send disappearing snaps, create group channels, and engage with our friendly Gemini Assistant!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.widthIn(max = 400.dp)
            )
        }
    }
}

@Composable
fun EmptyDetailsPane() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Select a conversation to see details",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

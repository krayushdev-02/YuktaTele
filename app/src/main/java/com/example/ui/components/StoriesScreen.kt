package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.StoryEntity
import com.example.ui.YuktaViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StoriesScreen(viewModel: YuktaViewModel) {
    val stories by viewModel.stories.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()

    var showCreateStoryDialog by remember { mutableStateOf(false) }

    val gradients = listOf(
        "Sunset Sunset" to listOf(Color(0xFFF43F5E), Color(0xFFD946EF)),
        "Ocean Breeze" to listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)),
        "Forest Mint" to listOf(Color(0xFF10B981), Color(0xFF059669)),
        "Solar Flare" to listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        "Neon Laser" to listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Stories Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Yukta Stories",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Disappearing snaps and stories that expire in 24 hours 🔥",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FloatingActionButton(
                onClick = { showCreateStoryDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("add_story_fab")
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Add Story")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stories Grid Layout
        if (stories.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No active stories yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Be the first to post a snappy story!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(stories) { story ->
                    StoryCardItem(story = story, onClick = { viewModel.openStoryViewer(story.userId) })
                }
            }
        }
    }

    // Story Creation Dialog
    if (showCreateStoryDialog) {
        var selectedGradientIdx by remember { mutableStateOf(0) }
        var captionText by remember { mutableStateOf("") }
        var aiTopicInput by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateStoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Yukta Story 🎨",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Camera Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(gradients[selectedGradientIdx].second)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (captionText.isBlank()) "Choose gradient & write text!" else captionText,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gradient chooser row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gradients.forEachIndexed { idx, item ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(item.second))
                                    .clickable { selectedGradientIdx = idx }
                                    .border(
                                        2.dp,
                                        if (selectedGradientIdx == idx) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Caption text input
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        placeholder = { Text("Write story caption...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gemini AI generator block
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = YuktaPink, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gemini Story Writer", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }

                                if (isAILoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
                                } else {
                                    TextButton(
                                        onClick = {
                                            if (aiTopicInput.isNotBlank()) {
                                                viewModel.generateAIMediaCaption(aiTopicInput) { text ->
                                                    captionText = text
                                                }
                                            }
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Generate", style = MaterialTheme.typography.labelSmall, color = YuktaPink)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = aiTopicInput,
                                onValueChange = { aiTopicInput = it },
                                placeholder = { Text("AI topic e.g. 'Coffee morning vibe'") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateStoryDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (captionText.isNotBlank()) {
                                    viewModel.addStory(
                                        caption = captionText,
                                        gradient = gradients[selectedGradientIdx].first
                                    )
                                    showCreateStoryDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YuktaPink),
                            modifier = Modifier.testTag("submit_story_button")
                        ) {
                            Text("Post Story")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCardItem(
    story: StoryEntity,
    onClick: () -> Unit
) {
    val gradients = mapOf(
        "Sunset Sunset" to listOf(Color(0xFFF43F5E), Color(0xFFD946EF)),
        "Ocean Breeze" to listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)),
        "Forest Mint" to listOf(Color(0xFF10B981), Color(0xFF059669)),
        "Solar Flare" to listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        "Neon Laser" to listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    )

    val gradientColors = gradients[story.mediaUrl] ?: listOf(Color(0xFF334155), Color(0xFF1E293B))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick)
            .testTag("story_card_${story.userId}"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top user info block
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GradientAvatar(
                        name = story.userName,
                        modifier = Modifier.size(32.dp),
                        avatarKey = story.userAvatar
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = story.userName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                // Middle caption display
                Text(
                    text = story.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 4,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // Bottom time status
                Text(
                    text = "Expires soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StoryViewerOverlay(
    viewModel: YuktaViewModel,
    stories: List<StoryEntity>,
    onClose: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }

    val currentStory = stories.getOrNull(currentIndex) ?: return

    val gradients = mapOf(
        "Sunset Sunset" to listOf(Color(0xFFF43F5E), Color(0xFFD946EF)),
        "Ocean Breeze" to listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)),
        "Forest Mint" to listOf(Color(0xFF10B981), Color(0xFF059669)),
        "Solar Flare" to listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        "Neon Laser" to listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    )

    val gradientColors = gradients[currentStory.mediaUrl] ?: listOf(Color(0xFF334155), Color(0xFF1E293B))

    // Automatically mark the current story viewed
    LaunchedEffect(currentIndex) {
        viewModel.markStoryAsViewed(currentStory.id)
    }

    LaunchedEffect(currentIndex) {
        progress = 0f
        while (progress < 1f) {
            delay(50)
            progress += 0.01f
        }
        if (currentIndex < stories.size - 1) {
            currentIndex++
        } else {
            onClose()
        }
    }

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors))
                    .clickable {
                        if (currentIndex < stories.size - 1) currentIndex++
                        else onClose()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Progress Slider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        stories.forEachIndexed { idx, _ ->
                            val currentProgress = when {
                                idx < currentIndex -> 1f
                                idx == currentIndex -> progress
                                else -> 0f
                            }
                            LinearProgressIndicator(
                                progress = { currentProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.35f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User identity header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GradientAvatar(
                                name = currentStory.userName,
                                modifier = Modifier.size(40.dp),
                                avatarKey = currentStory.userAvatar
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentStory.userName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "24h Expiring",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Content center
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentStory.caption,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                    // Reply message to story bar
                    var storyReplyInput by remember { mutableStateOf("") }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = storyReplyInput,
                            onValueChange = { storyReplyInput = it },
                            placeholder = { Text("Reply to ${currentStory.userName}...", color = Color.White.copy(alpha = 0.6f)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (storyReplyInput.isNotBlank()) {
                                    // Normally sends DM
                                    storyReplyInput = ""
                                    onClose()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

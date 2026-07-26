package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.CallEntity
import kotlinx.coroutines.delay

@Composable
fun GradientAvatar(
    name: String,
    modifier: Modifier = Modifier,
    avatarKey: String = "",
    shape: androidx.compose.ui.graphics.Shape = CircleShape
) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }

    val gradients = listOf(
        listOf(Color(0xFFF43F5E), Color(0xFFD946EF)), // Pink - Purple
        listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)), // Blue - Cyan
        listOf(Color(0xFF10B981), Color(0xFF059669)), // Emerald
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444)), // Orange - Red
        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))  // Violet - Pink
    )

    val index = kotlin.math.abs(name.hashCode()) % gradients.size
    val gradient = gradients[index]

    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarKey == "gemini") {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.55f)
            )
        } else if (avatarKey == "group") {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.55f)
            )
        } else if (avatarKey == "channel") {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.55f)
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CallOverlayScreen(
    call: CallEntity,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var callDurationSec by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSec++
        }
    }

    val formatDuration = { sec: Int ->
        val m = sec / 60
        val s = sec % 60
        String.format("%02d:%02d", m, s)
    }

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B132B)),
            contentAlignment = Alignment.Center
        ) {
            // Simulated video background camera or visualizer
            if (call.type == "video" && !isCameraOff) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1C2541), Color(0xFF0F172A))
                            )
                        )
                ) {
                    Text(
                        text = "[ Video Stream Active ]",
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    GradientAvatar(
                        name = call.contactName,
                        modifier = Modifier.size(110.dp),
                        avatarKey = call.contactAvatar
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = call.contactName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (call.type == "video") "Yukta Video Call..." else "Yukta Voice Call...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDuration(callDurationSec),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isMuted) Color.Red else Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.Red, shape = CircleShape)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    if (call.type == "video") {
                        IconButton(
                            onClick = { isCameraOff = !isCameraOff },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isCameraOff) Color.Red else Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                contentDescription = "Camera Toggle",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CallEntity
import com.example.ui.YuktaViewModel

@Composable
fun CallsScreen(viewModel: YuktaViewModel) {
    val callLogs by viewModel.callLogs.collectAsState()

    val formatTime = { timestamp: Long ->
        val sdf = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Calls History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Peer-to-peer secure audio & video calling 🔒",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recent calls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Call logs will appear here after you call a friend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(callLogs) { call ->
                    CallLogRow(call = call, formatTime = formatTime, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CallLogRow(
    call: CallEntity,
    formatTime: (Long) -> String,
    viewModel: YuktaViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("call_log_row_${call.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(
                    name = call.contactName,
                    modifier = Modifier.size(46.dp),
                    avatarKey = call.contactAvatar,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = call.contactName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (call.isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                            contentDescription = null,
                            tint = if (call.status == "missed") Color.Red else Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (call.status == "missed") "Missed call • ${formatTime(call.timestamp)}"
                            else "${call.duration} • ${formatTime(call.timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    viewModel.startCall(
                        contactName = call.contactName,
                        avatar = call.contactAvatar,
                        isVideo = call.type == "video"
                    )
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                    .testTag("redial_button_${call.id}")
            ) {
                Icon(
                    imageVector = if (call.type == "video") Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Redial",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

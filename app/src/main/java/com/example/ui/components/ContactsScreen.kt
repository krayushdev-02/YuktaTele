package com.example.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.UserEntity
import com.example.ui.YuktaViewModel
import com.example.ui.theme.YuktaPink

@Composable
fun ContactsScreen(viewModel: YuktaViewModel) {
    val allUsers by viewModel.allUsers.collectAsState()
    val filteredUsers = allUsers.filter { !it.isSelf }

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }
    var showSearchUserDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Contacts & Spaces",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Create secure spaces or look up members by username 📱",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showCreateGroupDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("create_group_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Groups, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Group", style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = { showCreateChannelDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("create_channel_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Channel", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showSearchUserDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("username_lookup_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Find User by Username")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Contacts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No contacts found", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers) { user ->
                    ContactRow(user = user, onClick = { viewModel.startNewChat(user) })
                }
            }
        }
    }

    // New Group Dialog
    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var groupDesc by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateGroupDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Create New Group 👥", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateGroupDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    viewModel.createGroupChat(groupName, emptyList())
                                    showCreateGroupDialog = false
                                }
                            },
                            modifier = Modifier.testTag("submit_group_btn")
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }

    // New Channel Dialog
    if (showCreateChannelDialog) {
        var channelName by remember { mutableStateOf("") }
        var channelDesc by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateChannelDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Create Broadcast Channel 📣", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = { Text("Channel Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = channelDesc,
                        onValueChange = { channelDesc = it },
                        label = { Text("Topic / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateChannelDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (channelName.isNotBlank()) {
                                    viewModel.createChannel(channelName, channelDesc)
                                    showCreateChannelDialog = false
                                }
                            },
                            modifier = Modifier.testTag("submit_channel_btn")
                        ) {
                            Text("Create Channel")
                        }
                    }
                }
            }
        }
    }

    // Find User by Username Dialog
    if (showSearchUserDialog) {
        var queryUsername by remember { mutableStateOf("") }
        var searchResult by remember { mutableStateOf<UserEntity?>(null) }
        var searched by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showSearchUserDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Search Username 🔍", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = queryUsername,
                        onValueChange = { queryUsername = it },
                        placeholder = { Text("e.g. sneha_r, gemini_ai") },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            searchResult = filteredUsers.find { it.username.equals(queryUsername.trim(), ignoreCase = true) }
                            searched = true
                        },
                        modifier = Modifier.fillMaxWidth().testTag("search_username_submit")
                    ) {
                        Text("Search")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (searched) {
                        searchResult?.let { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp).clickable {
                                        viewModel.startNewChat(user)
                                        showSearchUserDialog = false
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GradientAvatar(name = user.name, modifier = Modifier.size(40.dp), avatarKey = if (user.id == "gemini_bot") "gemini" else "")
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(user.name, fontWeight = FontWeight.Bold)
                                        Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        } ?: run {
                            Text("No user found with @$queryUsername", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSearchUserDialog = false }) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactRow(
    user: UserEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_row_${user.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GradientAvatar(
                name = user.name,
                modifier = Modifier.size(46.dp),
                avatarKey = if (user.id == "gemini_bot") "gemini" else "",
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${user.username} • ${user.bio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            if (user.streakCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = YuktaPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = user.streakCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = YuktaPink
                    )
                }
            }
        }
    }
}

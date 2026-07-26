package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.YuktaViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(viewModel: YuktaViewModel) {
    val selfUser by viewModel.selfUser.collectAsState()

    var isEditingProfile by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editUsername by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }

    // Synchronize form when selfUser is loaded
    LaunchedEffect(selfUser) {
        selfUser?.let {
            editName = it.name
            editUsername = it.username
            editBio = it.bio
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "YuktaTele Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Customize profile, theme preferences, and secure database tools 🛡️",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile details Card
        selfUser?.let { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientAvatar(name = user.name, modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isEditingProfile) {
                        Button(
                            onClick = { isEditingProfile = true },
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile")
                        }
                    } else {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Username handle") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isEditingProfile = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (editName.isNotBlank() && editUsername.isNotBlank()) {
                                        viewModel.updateSelfProfile(editName, editUsername, editBio)
                                        isEditingProfile = false
                                    }
                                },
                                modifier = Modifier.testTag("save_profile_btn")
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic System settings cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preferences & Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = YuktaBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Theme Preference", fontWeight = FontWeight.Bold)
                            Text("Follow system theme defaults", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Switch(checked = true, onCheckedChange = {})
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFBA1A1A))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Blocked Contacts", fontWeight = FontWeight.Bold)
                            Text("No contacts blocked yet", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Green)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Two-Factor Authentication", fontWeight = FontWeight.Bold)
                            Text("Secure 2FA code is enabled", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

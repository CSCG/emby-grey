package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaybackQuality
import com.example.ui.theme.AppAccent
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppDivider
import com.example.ui.theme.AppElevatedSurface
import com.example.ui.theme.AppLiveRed
import com.example.ui.theme.AppRadii
import com.example.ui.theme.AppSelectedSurface
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.AppTextTertiary
import com.example.ui.theme.AppTypography
import com.example.ui.viewmodel.EmbyViewModel

@Composable
fun SettingsScreen(
    viewModel: EmbyViewModel,
    onDisconnect: () -> Unit
) {
    val homeState by viewModel.homeState.collectAsState()
    val conn = homeState.connection
    val prefs = viewModel.serverPreferences

    var hwAccel by remember { mutableStateOf(prefs.hardwareAcceleration) }
    var defaultQuality by remember { mutableStateOf(prefs.defaultQuality) }
    var preferredSub by remember { mutableStateOf(prefs.preferredSubtitleLanguage) }

    var qualityMenuExpanded by remember { mutableStateOf(false) }
    var subMenuExpanded by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .testTag("settings_screen_content"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            ) {
                Text(
                    text = "Settings",
                    style = AppTypography.sectionTitle
                )
                Text(
                    text = "Server connection & playback preferences",
                    style = AppTypography.metadata
                )
            }
        }

        // Active Server Information Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                shape = RoundedCornerShape(AppRadii.card),
                color = AppElevatedSurface
            ) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    Text(
                        text = "Current Server",
                        style = AppTypography.cardTitle
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    Text(
                        text = conn?.serverName ?: "Emby Server",
                        style = AppTypography.body,
                        color = AppTextPrimary
                    )
                    Text(
                        text = conn?.url ?: "Not configured",
                        style = AppTypography.metadata
                    )
                    Text(
                        text = "User: ${conn?.username ?: "Guest"}",
                        style = AppTypography.metadata,
                        color = AppTextTertiary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    Button(
                        onClick = { showDisconnectDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("switch_server_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppSelectedSurface,
                            contentColor = AppLiveRed
                        ),
                        shape = RoundedCornerShape(AppRadii.button)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Server", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Playback & Transcoding Settings
        item {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                text = "Playback",
                style = AppTypography.cardTitle,
                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                shape = RoundedCornerShape(AppRadii.card),
                color = AppElevatedSurface
            ) {
                Column {
                    // Default Quality Selector
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { qualityMenuExpanded = true }
                                .padding(AppSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Default Quality", style = AppTypography.cardTitle)
                                Text("Quality when starting playback", style = AppTypography.metadata)
                            }
                            Text(
                                text = defaultQuality.label,
                                style = AppTypography.metadata,
                                color = AppAccent,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        DropdownMenu(
                            expanded = qualityMenuExpanded,
                            onDismissRequest = { qualityMenuExpanded = false },
                            modifier = Modifier
                                .background(AppElevatedSurface)
                                .clip(RoundedCornerShape(AppRadii.card))
                        ) {
                            PlaybackQuality.values().forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q.label, color = AppTextPrimary, fontSize = 14.sp) },
                                    trailingIcon = if (defaultQuality == q) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = AppAccent, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    onClick = {
                                        defaultQuality = q
                                        viewModel.updateDefaultQuality(q)
                                        qualityMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppDivider))

                    // Hardware Acceleration Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hardware Acceleration", style = AppTypography.cardTitle)
                            Text("Use GPU decoders for smoother playback", style = AppTypography.metadata)
                        }
                        Switch(
                            checked = hwAccel,
                            onCheckedChange = {
                                hwAccel = it
                                viewModel.setHardwareAcceleration(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF04191C),
                                checkedTrackColor = AppAccent,
                                uncheckedThumbColor = AppTextTertiary,
                                uncheckedTrackColor = AppSelectedSurface
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppDivider))

                    // Preferred Subtitles
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { subMenuExpanded = true }
                                .padding(AppSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Subtitle Language", style = AppTypography.cardTitle)
                                Text("Preferred language for sidecar subtitles", style = AppTypography.metadata)
                            }
                            Text(
                                text = if (preferredSub.isBlank()) "None" else preferredSub.uppercase(),
                                style = AppTypography.metadata,
                                color = AppAccent,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        DropdownMenu(
                            expanded = subMenuExpanded,
                            onDismissRequest = { subMenuExpanded = false },
                            modifier = Modifier
                                .background(AppElevatedSurface)
                                .clip(RoundedCornerShape(AppRadii.card))
                        ) {
                            listOf("" to "None", "eng" to "English", "spa" to "Spanish", "fra" to "French", "deu" to "German", "jpn" to "Japanese").forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, color = AppTextPrimary, fontSize = 14.sp) },
                                    trailingIcon = if (preferredSub == code) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = AppAccent, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    onClick = {
                                        preferredSub = code
                                        viewModel.updatePreferredSubtitle(code)
                                        subMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Disconnect Dialog
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect Server", style = AppTypography.cardTitle) },
            text = { Text("Disconnect from \"${conn?.serverName ?: "Emby Server"}\" and return to server setup?", style = AppTypography.body) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDisconnectDialog = false
                        viewModel.disconnect()
                        onDisconnect()
                    }
                ) {
                    Text("Disconnect", color = AppLiveRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancel", color = AppTextSecondary)
                }
            },
            containerColor = AppElevatedSurface,
            shape = RoundedCornerShape(AppRadii.modal)
        )
    }
}

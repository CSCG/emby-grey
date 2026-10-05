package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaybackQuality
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaCardBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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
            .background(CinemaBlack)
            .statusBarsPadding()
            .testTag("settings_screen_content"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AccentCyan, AccentPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CinemaBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Settings & Transcoding",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize playback, hardware encoding, & server",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Active Server Information Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, CinemaCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = CinemaDarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = "Server", tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current Emby Server",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentEmerald.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = AccentEmerald, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (conn?.isDemo == true) "DEMO MODE" else "CONNECTED",
                                    color = AccentEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Server: ${conn?.serverName ?: "Emby Server"}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Address: ${conn?.url ?: "Not configured"}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "User: ${conn?.username ?: "Guest"}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showDisconnectDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("switch_server_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Switch Server", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch / Disconnect Server", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Transcoding & Hardware Acceleration Settings
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, CinemaCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = CinemaDarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = "Transcoding", tint = AccentPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hardware-Accelerated Transcoding",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hardware Acceleration Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Request GPU Hardware Transcoding",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Requests NVENC, Intel QuickSync, or VAAPI hardware transcoding from the server to minimize server CPU overhead.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Switch(
                            checked = hwAccel,
                            onCheckedChange = {
                                hwAccel = it
                                viewModel.setHwAcceleration(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentCyan,
                                checkedTrackColor = AccentCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("hw_accel_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Default Quality Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Default Playback Stream",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = defaultQuality.label,
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box {
                            Button(
                                onClick = { qualityMenuExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Change", color = TextPrimary, fontSize = 12.sp)
                            }

                            DropdownMenu(
                                expanded = qualityMenuExpanded,
                                onDismissRequest = { qualityMenuExpanded = false },
                                modifier = Modifier.background(CinemaDarkSurface)
                            ) {
                                PlaybackQuality.values().forEach { q ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(q.label, color = TextPrimary)
                                                if (q == defaultQuality) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentCyan)
                                                }
                                            }
                                        },
                                        onClick = {
                                            defaultQuality = q
                                            viewModel.setDefaultQuality(q)
                                            qualityMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preferred Subtitle Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Preferred Subtitle Language",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = preferredSub,
                                color = AccentEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box {
                            Button(
                                onClick = { subMenuExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Change", color = TextPrimary, fontSize = 12.sp)
                            }

                            DropdownMenu(
                                expanded = subMenuExpanded,
                                onDismissRequest = { subMenuExpanded = false },
                                modifier = Modifier.background(CinemaDarkSurface)
                            ) {
                                listOf("English", "Spanish", "French", "German", "Japanese", "Off").forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(lang, color = TextPrimary)
                                                if (lang == preferredSub) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentEmerald)
                                                }
                                            }
                                        },
                                        onClick = {
                                            preferredSub = lang
                                            viewModel.setPreferredSubtitleLang(lang)
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

        // About & Premiere-Free Note
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, CinemaCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = CinemaDarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About EmbyStream",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "EmbyStream connects directly to your Emby server's open REST API. It bypasses Premiere playback locks for movies and TV shows, supports direct stream playback with ExoPlayer, requests server-side hardware transcoding, and handles offline synchronization straight to your device.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Version 1.0.0 • Native Android & Jetpack Compose",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Disconnect Confirmation Dialog
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect Server?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You will return to the server setup screen where you can connect to another Emby server or reconnect.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisconnectDialog = false
                        viewModel.disconnectServer()
                        onDisconnect()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Disconnect", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CinemaDarkSurface
        )
    }
}

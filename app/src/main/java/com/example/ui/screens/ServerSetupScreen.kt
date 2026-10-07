package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppAccent
import com.example.ui.theme.AppBackground
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
fun ServerSetupScreen(
    viewModel: EmbyViewModel,
    onConnected: () -> Unit
) {
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val homeState by viewModel.homeState.collectAsState()
    val isPrivateIp = remember(serverUrl) {
        val s = serverUrl.trim()
        s.contains("192.168.") || s.contains("10.") || s.contains("172.16.") || s.contains("localhost") || s.contains("127.0.0.1")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.xl))

            // Brand Icon & Title
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(AppRadii.hero))
                    .background(AppElevatedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = AppAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

            Text(
                text = "Emby",
                style = AppTypography.heroTitle
            )

            Text(
                text = "Connect to your media server",
                style = AppTypography.metadata
            )

            Spacer(modifier = Modifier.height(AppSpacing.xl))

            // Setup Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadii.card),
                color = AppElevatedSurface
            ) {
                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                    Text(
                        text = "Server Connection",
                        style = AppTypography.cardTitle
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server URL", fontSize = 13.sp) },
                        placeholder = { Text("http://emby.example.com:8096", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = AppTextSecondary, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppAccent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = AppTextPrimary,
                            unfocusedTextColor = AppTextPrimary,
                            focusedContainerColor = AppSurface,
                            unfocusedContainerColor = AppSurface
                        ),
                        shape = RoundedCornerShape(AppRadii.button),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
                    )

                    if (isPrivateIp) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cloud emulator cannot reach private local LAN IPs directly. Tap 'Explore Demo Server' below to test immediately, or connect via a public domain / port forward.",
                            style = AppTypography.minorLabel,
                            color = AppAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AppTextSecondary, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppAccent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = AppTextPrimary,
                            unfocusedTextColor = AppTextPrimary,
                            focusedContainerColor = AppSurface,
                            unfocusedContainerColor = AppSurface
                        ),
                        shape = RoundedCornerShape(AppRadii.button),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AppTextSecondary, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = AppTextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppAccent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = AppTextPrimary,
                            unfocusedTextColor = AppTextPrimary,
                            focusedContainerColor = AppSurface,
                            unfocusedContainerColor = AppSurface
                        ),
                        shape = RoundedCornerShape(AppRadii.button),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.connectToServer(serverUrl, username, password, onConnected)
                            }
                        )
                    )

                    if (homeState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = homeState.errorMessage!!,
                            style = AppTypography.metadata,
                            color = AppLiveRed
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    // Connect Action
                    Button(
                        onClick = {
                            viewModel.connectToServer(serverUrl, username, password, onConnected)
                        },
                        enabled = !homeState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("connect_server_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppAccent,
                            contentColor = Color(0xFF04191C)
                        ),
                        shape = RoundedCornerShape(AppRadii.button)
                    ) {
                        if (homeState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF04191C),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Connect", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    // Demo Mode Action
                    Button(
                        onClick = {
                            viewModel.loadDemoMode(onConnected)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("demo_mode_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppSelectedSurface,
                            contentColor = AppTextPrimary
                        ),
                        shape = RoundedCornerShape(AppRadii.button)
                    ) {
                        Text("Explore Demo Server (Preloaded)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))
        }
    }
}

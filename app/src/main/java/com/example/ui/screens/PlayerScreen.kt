package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.EmbyItemDto
import com.example.data.model.PlaybackQuality
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    item: EmbyItemDto,
    initialQuality: PlaybackQuality,
    localFilePath: String? = null,
    startFromBeginning: Boolean = false,
    playerViewModel: PlayerViewModel,
    isInPipMode: Boolean = false,
    onNavigateBack: () -> Unit
) {
    // Dedicated authentic Iowa 511 Traffic Camera CCTV monitor
    if (item.collectionType == "TrafficCam") {
        LiveTrafficCamScreen(
            initialItem = item,
            isInPipMode = isInPipMode,
            onNavigateBack = onNavigateBack
        )
        return
    }

    val context = LocalContext.current
    val uiState by playerViewModel.uiState.collectAsState()
    val activity = context.findActivity()
    val isSystemPip = isInPipMode || activity?.isInPictureInPictureMode == true

    LaunchedEffect(isSystemPip) {
        playerViewModel.setInPipMode(isSystemPip)
    }

    var showQualitySheet by remember { mutableStateOf(false) }
    var showSubtitleSheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showChapterSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showToonamiServerSheet by remember { mutableStateOf(false) }

    // Immersive fullscreen setup
    DisposableEffect(item.id, localFilePath, initialQuality, startFromBeginning) {
        val window = context.findActivity()?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        playerViewModel.initializePlayer(
            context = context,
            item = item,
            localFile = localFilePath,
            initialQuality = initialQuality,
            startFromBeginning = startFromBeginning
        )

        onDispose {
            window?.let {
                val insetsController = WindowCompat.getInsetsController(it, it.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            playerViewModel.release()
        }
    }

    BackHandler {
        onNavigateBack()
    }

    // Auto-dismiss resume notification after 6 seconds
    LaunchedEffect(uiState.showResumeBanner) {
        if (uiState.showResumeBanner) {
            delay(6000)
            playerViewModel.dismissResumeBanner()
        }
    }

    // Auto-hide controls after 4 seconds of inactivity while playing
    LaunchedEffect(uiState.areControlsVisible, uiState.isPlaying) {
        if (uiState.areControlsVisible && uiState.isPlaying && !uiState.isControlsLocked) {
            delay(4000)
            playerViewModel.toggleControlsVisibility()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                playerViewModel.toggleControlsVisibility()
            }
            .testTag("player_screen_root")
    ) {
        // Media3 ExoPlayer View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false // Custom Jetpack Compose overlay
                    resizeMode = uiState.resizeMode
                    player = playerViewModel.getPlayer()
                }
            },
            update = { playerView ->
                playerView.player = playerViewModel.getPlayer()
                playerView.resizeMode = uiState.resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (uiState.isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AccentCyan,
                    modifier = Modifier.size(54.dp),
                    strokeWidth = 3.dp
                )
            }
        }

        // Error State Overlay
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CinemaDarkSurface,
                    border = BorderStroke(1.5.dp, Color(0xFFE50914)),
                    modifier = Modifier.widthIn(max = 380.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE50914).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Error",
                                tint = Color(0xFFE50914),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Playback Error",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = uiState.errorMessage ?: "Unable to stream media.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text("Back")
                            }

                            Button(
                                onClick = { playerViewModel.retry() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = uiState.areControlsVisible && !isSystemPip,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.name,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val subtitle = when {
                                item.collectionType == "TrafficCam" -> "IA 511 DOT Live Camera • ${item.officialRating ?: "Cedar Falls, IA"}"
                                uiState.isLiveStream -> "24/7 Live Stream • ${item.seriesName ?: "Broadcast"}"
                                uiState.isOfflinePlayback -> "Offline Sync Playback"
                                item.seriesName != null -> "${item.seriesName} • S${item.parentIndexNumber ?: 1}E${item.indexNumber ?: 1}"
                                else -> uiState.quality.label
                            }
                            Text(
                                text = subtitle,
                                color = AccentCyan,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Top Right Controls (PiP, Chapters, Sleep Timer, Aspect Ratio, Lock)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Picture-in-Picture Button
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            IconButton(
                                onClick = {
                                    playerViewModel.setInPipMode(true)
                                    (context as? com.example.MainActivity)?.enterPipMode() ?: run {
                                        val activity = context.findActivity()
                                        if (activity != null) {
                                            try {
                                                val params = PictureInPictureParams.Builder()
                                                    .setAspectRatio(Rational(16, 9))
                                                    .build()
                                                activity.enterPictureInPictureMode(params)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("pip_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPictureAlt,
                                    contentDescription = "Picture in Picture",
                                    tint = Color.White
                                )
                            }
                        }

                        // Chapters Button
                        if (uiState.chapters.isNotEmpty()) {
                            IconButton(
                                onClick = { showChapterSheet = true },
                                modifier = Modifier.testTag("chapters_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Chapters",
                                    tint = Color.White
                                )
                            }
                        }

                        // Sleep Timer Button
                        IconButton(
                            onClick = { showSleepTimerSheet = true },
                            modifier = Modifier.testTag("sleep_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Sleep Timer",
                                tint = if (uiState.sleepTimerMinutes != null) AccentCyan else Color.White
                            )
                        }

                        IconButton(
                            onClick = { playerViewModel.cycleAspectRatio() },
                            modifier = Modifier.testTag("aspect_ratio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Aspect Ratio",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { playerViewModel.toggleLock() },
                            modifier = Modifier.testTag("lock_controls_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isControlsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (uiState.isControlsLocked) AccentAmber else Color.White
                            )
                        }
                    }
                }

                // Resume Playback Toast Banner
                AnimatedVisibility(
                    visible = uiState.showResumeBanner && uiState.resumedFromMs != null,
                    enter = fadeIn() + slideInVertically { -it },
                    exit = fadeOut() + slideOutVertically { -it },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CinemaDarkSurface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Resumed at ${formatDuration(uiState.resumedFromMs ?: 0L)}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Start Over",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { playerViewModel.restartFromBeginning() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { playerViewModel.dismissResumeBanner() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Center Play / Pause & Quick Seek Controls
                if (!uiState.isControlsLocked) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 10s
                        IconButton(
                            onClick = { playerViewModel.seekBack10() },
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .testTag("rewind_10_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Large Play/Pause Glowing Button
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(AccentCyan, AccentEmerald)))
                                .clickable { playerViewModel.togglePlayPause() }
                                .testTag("play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = CinemaBlack,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        // Forward 10s
                        IconButton(
                            onClick = { playerViewModel.seekForward10() },
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .testTag("forward_10_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                // Bottom Controls Bar (Scrubber, Timestamps, Quality & Subtitles)
                if (!uiState.isControlsLocked) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Time Scrubber or Live Stream Badge
                        if (uiState.isLiveStream) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (item.collectionType == "TrafficCam") AccentAmber else Color(0xFFE50914)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (item.collectionType == "TrafficCam") Color.Black else Color.White)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (item.collectionType == "TrafficCam") "CCTV LIVE" else "LIVE",
                                                color = if (item.collectionType == "TrafficCam") Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.name,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = if (item.collectionType == "TrafficCam") "IA 511 Feed • 1080p" else "Low Latency HLS",
                                    color = if (item.collectionType == "TrafficCam") AccentAmber else AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            val duration = uiState.durationMs
                            val current = uiState.currentPositionMs.coerceIn(0L, duration)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDuration(current),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Slider(
                                    value = if (duration > 0) current.toFloat() / duration.toFloat() else 0f,
                                    onValueChange = { fraction ->
                                        playerViewModel.seekTo((fraction * duration).toLong())
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                        .testTag("player_seek_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentCyan,
                                        activeTrackColor = AccentCyan,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                    )
                                )

                                Text(
                                    text = formatDuration(duration),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Bottom Action Pills (Quality, Subtitles, Audio, Speed)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isToonamiStream = item.name.contains("Toonami", ignoreCase = true) ||
                                item.seriesName?.contains("Toonami", ignoreCase = true) == true ||
                                uiState.currentToonamiServer != null

                            val isLiquidTvStream = uiState.isLiquidTv ||
                                item.name.contains("Liquid Television", ignoreCase = true) ||
                                item.id == "free_liquid_tv"

                            if (isLiquidTvStream) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { playerViewModel.shuffleLiquidTv(context) }
                                        .testTag("shuffle_liquid_tv_pill"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentPurple.copy(alpha = 0.25f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Replay,
                                            contentDescription = "Random Episode",
                                            tint = AccentPurple,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Random Episode",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (isToonamiStream) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showToonamiServerSheet = true }
                                        .testTag("toonami_server_pill"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentCyan.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Server",
                                            tint = AccentCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Server: ${uiState.currentToonamiServer ?: playerViewModel.selectedToonamiServer}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // Quality Pill (Direct Play vs Transcode)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showQualitySheet = true }
                                        .testTag("quality_selector_pill"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (uiState.quality == PlaybackQuality.DIRECT_PLAY)
                                        AccentEmerald.copy(alpha = 0.2f)
                                    else AccentPurple.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = "Quality",
                                            tint = if (uiState.quality == PlaybackQuality.DIRECT_PLAY) AccentEmerald else AccentPurple,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (uiState.isOfflinePlayback) "Offline File" else uiState.quality.label,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Subtitle Toggle Button
                                IconButton(
                                    onClick = { showSubtitleSheet = true },
                                    modifier = Modifier.testTag("subtitle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ClosedCaption,
                                        contentDescription = "Subtitles",
                                        tint = if (uiState.selectedSubtitleId != null) AccentCyan else Color.White
                                    )
                                }

                                // Audio Tracks Button
                                IconButton(
                                    onClick = { showAudioSheet = true },
                                    modifier = Modifier.testTag("audio_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = "Audio Tracks",
                                        tint = Color.White
                                    )
                                }

                                // Speed Button
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showSpeedSheet = true }
                                        .testTag("speed_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${uiState.playbackSpeed}x",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Skip Intro Button (Visible when within intro chapter)
        AnimatedVisibility(
            visible = uiState.canSkipIntro && !uiState.isControlsLocked && !uiState.isInPipMode,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
        ) {
            Button(
                onClick = { playerViewModel.skipIntro() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.5.dp, AccentCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("skip_intro_button")
            ) {
                Icon(Icons.Default.FastForward, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Skip Intro", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Up Next Auto-Play Countdown Banner Overlay
        AnimatedVisibility(
            visible = uiState.showUpNextOverlay && uiState.nextEpisode != null && !uiState.isInPipMode,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
        ) {
            val nextEp = uiState.nextEpisode!!
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CinemaDarkSurface.copy(alpha = 0.95f),
                border = BorderStroke(1.5.dp, AccentCyan),
                shadowElevation = 12.dp,
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UP NEXT IN ${uiState.upNextCountdownSeconds}s",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(
                            onClick = { playerViewModel.dismissUpNext() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextEp.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "S${nextEp.parentIndexNumber ?: 1}E${nextEp.indexNumber ?: 1} • ${nextEp.durationMinutes} min",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { playerViewModel.playNextEpisode(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = CinemaBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { playerViewModel.dismissUpNext() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets for Quality, Subtitles, Audio, and Speed

    // 1. Quality Selector Sheet
    if (showQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Playback Stream Quality",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Direct Play serves original files with 0% server overhead. Transcoding uses server GPU hardware acceleration.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                PlaybackQuality.values().forEach { quality ->
                    val isSelected = uiState.quality == quality
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                playerViewModel.setQuality(quality)
                                showQualitySheet = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = quality.label,
                                    color = if (isSelected) AccentCyan else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = quality.description,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentCyan)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 1b. Toonami Server Selector Sheet (Matching https://www.toonamiaftermath.com/servers)
    if (showToonamiServerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showToonamiServerSheet = false },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Toonami Stream Server",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Origin servers matching toonamiaftermath.com/servers. Select a specific node or leave on Auto for dynamic balancing.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                playerViewModel.toonamiServers.forEach { serverNode ->
                    val isSelected = (playerViewModel.selectedToonamiServer == serverNode) ||
                        (playerViewModel.selectedToonamiServer == "Auto" && uiState.currentToonamiServer == serverNode && serverNode != "Auto")
                    val description = when (serverNode) {
                        "Auto" -> "Dynamic auto-routing to the lowest-latency active node"
                        "n6" -> "Primary high-capacity US origin node"
                        "n4" -> "US East fast CDN stream server"
                        "n7" -> "US West low-latency stream server"
                        "n3" -> "US Central origin backup server"
                        "n5" -> "Global / European relay mirror"
                        "n8" -> "High-bandwidth failover node"
                        "n1" -> "Master Origin 1 (Low latency)"
                        "n2" -> "Master Origin 2 (Redundant failover)"
                        else -> "Alternative Toonami relay node"
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                playerViewModel.setToonamiServer(serverNode)
                                showToonamiServerSheet = false
                            }
                            .testTag("server_option_$serverNode"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (serverNode == "Auto") "Auto (Recommended)" else "Server $serverNode",
                                    color = if (isSelected) AccentCyan else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = description,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentCyan)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 2. Subtitle Selector Sheet
    if (showSubtitleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSubtitleSheet = false },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Subtitles",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // "Off" option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            playerViewModel.selectSubtitle(null)
                            showSubtitleSheet = false
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (uiState.selectedSubtitleId == null) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Off",
                            color = if (uiState.selectedSubtitleId == null) AccentCyan else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.selectedSubtitleId == null) {
                            Icon(Icons.Default.Check, contentDescription = "Off", tint = AccentCyan)
                        }
                    }
                }

                // Available tracks
                uiState.subtitles.forEach { track ->
                    val isSelected = uiState.selectedSubtitleId == track.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                playerViewModel.selectSubtitle(track)
                                showSubtitleSheet = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = track.name,
                                color = if (isSelected) AccentCyan else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentCyan)
                            }
                        }
                    }
                }

                if (uiState.subtitles.isEmpty()) {
                    Text(
                        text = "No additional subtitle tracks detected for this stream.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 3. Audio Tracks Sheet
    if (showAudioSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioSheet = false },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Audio Stream & Language",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                uiState.audioTracks.forEach { track ->
                    val isSelected = uiState.selectedAudioTrackId == track.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                playerViewModel.selectAudioTrack(track)
                                showAudioSheet = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = track.name,
                                color = if (isSelected) AccentCyan else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentCyan)
                            }
                        }
                    }
                }

                if (uiState.audioTracks.isEmpty()) {
                    Text(
                        text = "Default stereo / primary audio stream active.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 4. Speed Sheet
    if (showSpeedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Playback Speed",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                    val isSelected = uiState.playbackSpeed == speed
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                playerViewModel.setPlaybackSpeed(speed)
                                showSpeedSheet = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${speed}x ${if (speed == 1.0f) "(Normal)" else ""}",
                                color = if (isSelected) AccentCyan else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentCyan)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 5. Chapter Selector Sheet
    if (showChapterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChapterSheet = false },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chapters",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.chapters.size} total",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.chapters) { chapter ->
                        val isCurrent = uiState.currentChapter == chapter
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    playerViewModel.seekToChapter(chapter)
                                    showChapterSheet = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = if (isCurrent) AccentCyan else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = chapter.name ?: "Chapter ${uiState.chapters.indexOf(chapter) + 1}",
                                        color = if (isCurrent) AccentCyan else TextPrimary,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = formatDuration(chapter.startPositionMs),
                                    color = if (isCurrent) AccentCyan else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 6. Sleep Timer Sheet
    if (showSleepTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSleepTimerSheet = false },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Sleep Timer",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.sleepTimerRemainingSeconds != null) {
                    val remainingMins = (uiState.sleepTimerRemainingSeconds ?: 0) / 60
                    val remainingSecs = (uiState.sleepTimerRemainingSeconds ?: 0) % 60
                    Text(
                        text = "Active: pausing in ${remainingMins}m ${remainingSecs}s",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val options = listOf(
                    null to "Off",
                    15 to "15 Minutes",
                    30 to "30 Minutes",
                    45 to "45 Minutes",
                    60 to "60 Minutes"
                )

                options.forEach { (mins, label) ->
                    val isSelected = uiState.sleepTimerMinutes == mins
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                playerViewModel.setSleepTimer(mins)
                                showSleepTimerSheet = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CinemaSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) AccentCyan else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentCyan)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

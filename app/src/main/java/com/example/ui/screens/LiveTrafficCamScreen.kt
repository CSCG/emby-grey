package com.example.ui.screens

import android.app.PictureInPictureParams
import android.content.Context
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.EmbyItemDto
import com.example.data.repository.FreeLiveTvChannels
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaCardBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun LiveTrafficCamScreen(
    initialItem: EmbyItemDto,
    isInPipMode: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isSystemPip = isInPipMode || context.findActivity()?.isInPictureInPictureMode == true
    val trafficCams = remember {
        val list = FreeLiveTvChannels.channels.filter { it.isTrafficCam }
        if (list.isNotEmpty()) list else FreeLiveTvChannels.channels.take(8)
    }

    var selectedCamIndex by remember {
        val idx = trafficCams.indexOfFirst { it.id == initialItem.id }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    val currentCam = trafficCams.getOrElse(selectedCamIndex) { trafficCams.first() }

    var refreshTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentTimeString by remember { mutableStateOf("") }
    var areControlsVisible by remember { mutableStateOf(true) }
    var refreshCountdown by remember { mutableIntStateOf(5) }

    // Immersive Fullscreen handling
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            window?.let {
                val insetsController = WindowCompat.getInsetsController(it, it.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    // Live clock and auto-refresh countdown (updates snapshot every 5 seconds)
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a 'CST'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("America/Chicago")
        }
        while (true) {
            currentTimeString = timeFormat.format(Date())
            delay(1000)
            if (refreshCountdown > 1) {
                refreshCountdown--
            } else {
                refreshCountdown = 5
                refreshTimestamp = System.currentTimeMillis()
            }
        }
    }

    val rawUrl = currentCam.snapshotUrl ?: currentCam.streamUrl ?: ""
    val imageUrl = if (rawUrl.contains("?")) "$rawUrl&t=$refreshTimestamp" else "$rawUrl?t=$refreshTimestamp"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { areControlsVisible = !areControlsVisible }
            .testTag("traffic_cam_viewer")
    ) {
        // Real Live Roadway Snapshot from Iowa DOT
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .crossfade(300)
                .build(),
            contentDescription = currentCam.name,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AccentAmber,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Iowa DOT Camera Stream Active",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Connecting to DOT network feed...",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Overlay Controls & HUD
        AnimatedVisibility(
            visible = areControlsVisible && !isSystemPip,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.8f),
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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .testTag("traffic_cam_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AccentAmber
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "IA 511 CCTV",
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentCam.name,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${currentCam.location ?: "Cedar Falls, IA"} • Camera ${selectedCamIndex + 1} of ${trafficCams.size}",
                                color = AccentAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Live Time HUD
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, CinemaCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentTimeString,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Picture-in-Picture Button
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
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
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .testTag("traffic_cam_pip_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPictureAlt,
                                    contentDescription = "Picture in Picture",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Bottom Controls & Camera Quick-Switch Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Telemetry & Condition strip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CinemaDarkSurface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentCam.currentProgram?.name ?: "Cedar Valley Traffic Feed",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Auto-updates: ${refreshCountdown}s",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        refreshTimestamp = System.currentTimeMillis()
                                        refreshCountdown = 5
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Snapshot",
                                        tint = AccentAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step Prev / Next Camera Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CinemaSurfaceVariant,
                            border = BorderStroke(1.dp, CinemaCardBorder),
                            modifier = Modifier
                                .clickable {
                                    selectedCamIndex = if (selectedCamIndex > 0) selectedCamIndex - 1 else trafficCams.size - 1
                                    refreshTimestamp = System.currentTimeMillis()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Camera", tint = AccentAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous Camera", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CinemaSurfaceVariant,
                            border = BorderStroke(1.dp, CinemaCardBorder),
                            modifier = Modifier
                                .clickable {
                                    selectedCamIndex = (selectedCamIndex + 1) % trafficCams.size
                                    refreshTimestamp = System.currentTimeMillis()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Next Camera", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Camera", tint = AccentAmber, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Quick-Picker Pills for all Cedar Falls/Waterloo cams
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(trafficCams) { index, cam ->
                            val isSelected = index == selectedCamIndex
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AccentAmber else CinemaDarkSurface,
                                border = BorderStroke(1.dp, if (isSelected) AccentAmber else CinemaCardBorder),
                                modifier = Modifier.clickable {
                                    selectedCamIndex = index
                                    refreshTimestamp = System.currentTimeMillis()
                                    refreshCountdown = 5
                                }
                            ) {
                                Text(
                                    text = "CH ${cam.number ?: (index + 1)} • ${cam.name.replace("IA511: ", "").replace("Cedar Falls - ", "CF: ").replace("Waterloo - ", "WLOO: ")}",
                                    color = if (isSelected) CinemaBlack else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Minimal Floating Badge in PiP Mode
        if (isSystemPip) {
            Surface(
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(AccentAmber)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE • ${currentCam.name.replace("IA511: ", "").replace("Cedar Falls - ", "CF: ").replace("Waterloo - ", "WLOO: ")}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun Context.findActivity(): android.app.Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}


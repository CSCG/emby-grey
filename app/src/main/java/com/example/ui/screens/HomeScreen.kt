package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.EmbyItemDto
import com.example.data.repository.DemoDataProvider
import com.example.ui.components.LibraryCard
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.SectionHeader
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
import com.example.ui.viewmodel.EmbyViewModel

@Composable
fun HomeScreen(
    viewModel: EmbyViewModel,
    onNavigateToItem: (String) -> Unit,
    onNavigateToLibrary: (String, String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLiveTv: () -> Unit = {}
) {
    val homeState by viewModel.homeState.collectAsState()
    val liveTvChannels by viewModel.liveTvChannels.collectAsState()
    val conn = homeState.connection

    val heroItem = homeState.latestItems.firstOrNull { it.type == "Movie" }
        ?: homeState.latestItems.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToSettings() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AccentCyan, AccentEmerald))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Server",
                            tint = CinemaBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = conn?.serverName ?: "EmbyStream",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (conn?.isDemo == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AccentEmerald.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "DEMO",
                                        color = AccentEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (conn?.isDemo == true) "Open Media Server" else (conn?.username ?: "Not connected"),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToLiveTv,
                        modifier = Modifier.testTag("live_tv_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = "Live TV",
                            tint = Color(0xFFE50914)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = onNavigateToDownloads,
                        modifier = Modifier.testTag("downloads_icon_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Downloads", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_icon_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                }
            }
        }

        // Hero Spotlight Banner
        if (heroItem != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CinemaSurfaceVariant)
                ) {
                    val heroContext = LocalContext.current
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(heroContext)
                            .data(viewModel.getImageUrl(heroItem, isBackdrop = true))
                            .crossfade(true)
                            .build(),
                        contentDescription = heroItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CinemaDarkSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = AccentCyan.copy(alpha = 0.5f),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(CinemaSurfaceVariant, CinemaDarkSurface)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = TextMuted.copy(alpha = 0.35f),
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }
                    )

                    // Scrim gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CinemaBlack.copy(alpha = 0.4f),
                                        CinemaBlack.copy(alpha = 0.95f)
                                    ),
                                    startY = 50f
                                )
                            )
                    )

                    // Hero Content
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentCyan.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "FEATURED SPOTLIGHT",
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = heroItem.name,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = heroItem.overview ?: "Stream directly or transcode in high definition.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 40.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onNavigateToPlayer(heroItem.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentCyan,
                                    contentColor = CinemaBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("hero_play_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Direct", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = { onNavigateToItem(heroItem.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Details", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Details", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Continue Watching Section
        if (homeState.continueWatching.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Continue Watching",
                    subtitle = "Resume where you left off"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(homeState.continueWatching) { item ->
                        MediaPosterCard(
                            item = item,
                            imageUrl = viewModel.getImageUrl(item, isBackdrop = true),
                            onClick = { onNavigateToPlayer(item.id) },
                            modifier = Modifier.width(200.dp),
                            aspectRatio = 16f / 9f
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Live TV Channels Section
        if (liveTvChannels.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Live TV & Streaming Channels",
                    subtitle = "24/7 Free FAST and tuner broadcasts",
                    actionText = "View Guide",
                    onActionClick = onNavigateToLiveTv
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(liveTvChannels.take(12)) { channel ->
                        val isTraffic = channel.isTrafficCam
                        val (tagText, tagColor) = when {
                            channel.isTrafficCam -> "DOT CAM" to AccentAmber
                            channel.category == "Classic Cartoons" -> "TOONS" to AccentCyan
                            channel.category == "Anime" -> "ANIME" to AccentPurple
                            channel.isOnlineFast -> "FAST" to AccentEmerald
                            else -> "TUNER" to AccentPurple
                        }

                        Surface(
                            modifier = Modifier
                                .width(225.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val itemDto = viewModel.buildLiveTvItemDto(channel)
                                    onNavigateToPlayer(itemDto.id)
                                }
                                .testTag("home_live_channel_${channel.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = CinemaDarkSurface
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isTraffic) AccentAmber else Color(0xFFE50914))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTraffic) "CAM" else "LIVE",
                                            color = if (isTraffic) AccentAmber else Color(0xFFE50914),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = tagColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = tagText,
                                            color = tagColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = channel.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = channel.currentProgram?.name ?: "Continuous Live Broadcast",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isTraffic) Icons.Default.Videocam else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isTraffic) AccentAmber else AccentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTraffic) "View Cam" else "Watch Now",
                                        color = if (isTraffic) AccentAmber else AccentCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Media Libraries Section
        if (homeState.libraries.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Media Libraries",
                    subtitle = "Browse your organized collection"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(homeState.libraries) { lib ->
                        LibraryCard(
                            name = lib.name,
                            collectionType = lib.collectionType ?: "movies",
                            onClick = {
                                onNavigateToLibrary(lib.id, lib.name)
                            },
                            modifier = Modifier
                                .width(170.dp)
                                .height(115.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Latest Feature Films & TV Shows
        if (homeState.latestItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Latest Additions",
                    subtitle = "Recently added to server",
                    actionText = "See All",
                    onActionClick = {
                        val firstLib = homeState.libraries.firstOrNull()
                        if (firstLib != null) {
                            onNavigateToLibrary(firstLib.id, firstLib.name)
                        }
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(homeState.latestItems) { item ->
                        MediaPosterCard(
                            item = item,
                            imageUrl = viewModel.getImageUrl(item, isBackdrop = false),
                            onClick = { onNavigateToItem(item.id) },
                            modifier = Modifier.width(135.dp)
                        )
                    }
                }
            }
        }

        // Loading indicator
        if (homeState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentCyan)
                }
            }
        }
    }
}

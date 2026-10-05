package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EmbyItemDto
import com.example.data.model.LiveTvChannelDto
import com.example.ui.theme.AccentAmber
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
fun LiveTvScreen(
    viewModel: EmbyViewModel,
    onPlayLiveChannel: (EmbyItemDto) -> Unit
) {
    val channels by viewModel.liveTvChannels.collectAsState()
    val selectedCategory by viewModel.selectedLiveTvCategory.collectAsState()
    val isLoading by viewModel.isLiveTvLoading.collectAsState()

    val categories = remember(channels) {
        val unique = channels.map { it.category }.distinct().filter { it.isNotBlank() }
        listOf("All") + unique
    }

    val filteredChannels = remember(channels, selectedCategory) {
        if (selectedCategory == "All") {
            channels
        } else {
            channels.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .statusBarsPadding()
            .testTag("live_tv_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFE50914), AccentPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = "Live TV",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Live TV & Guides",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Pulsing Live Dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE50914))
                        )
                    }
                    Text(
                        text = "${channels.size} live channels & cameras active",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = { viewModel.refreshLiveTv() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CinemaSurfaceVariant)
                    .testTag("refresh_live_tv_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Guide",
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.filterLiveTv(category) },
                    label = {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (category.contains("Iowa")) AccentAmber else AccentCyan,
                        selectedLabelColor = CinemaBlack,
                        containerColor = CinemaSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) (if (category.contains("Iowa")) AccentAmber else AccentCyan) else CinemaCardBorder,
                        selectedBorderColor = if (category.contains("Iowa")) AccentAmber else AccentCyan,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Channels & EPG List
        if (isLoading && channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentCyan)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Feature banner for Cedar Falls & Iowa local cams
                if (selectedCategory == "Iowa & Cedar Falls" || selectedCategory == "All") {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CinemaDarkSurface,
                            border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AccentAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Cedar Falls & Iowa 511",
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AccentAmber.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "LOCAL HUB",
                                                color = AccentAmber,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Live IA511 highway traffic cameras (US-218, Viking Rd, UNI campus), Eastern Iowa Doppler weather & news.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                items(filteredChannels, key = { it.id }) { channel ->
                    LiveChannelCard(
                        channel = channel,
                        logoUrl = viewModel.getChannelLogoUrl(channel),
                        onWatch = {
                            val itemDto = viewModel.buildLiveTvItemDto(channel)
                            onPlayLiveChannel(itemDto)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LiveChannelCard(
    channel: LiveTvChannelDto,
    logoUrl: String?,
    onWatch: () -> Unit
) {
    val isTraffic = channel.isTrafficCam

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onWatch() }
            .border(1.dp, if (isTraffic) AccentAmber.copy(alpha = 0.35f) else CinemaCardBorder, RoundedCornerShape(14.dp))
            .testTag("channel_${channel.id}"),
        shape = RoundedCornerShape(14.dp),
        color = CinemaDarkSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Channel Logo / Camera Thumbnail
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CinemaSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!logoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = channel.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = if (isTraffic) Icons.Default.Videocam else Icons.Default.Tv,
                                contentDescription = null,
                                tint = if (isTraffic) AccentAmber else AccentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channel.name,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (channel.number != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "CH ${channel.number}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            if (!channel.location.isNullOrBlank()) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = channel.location,
                                    color = AccentAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "•",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = channel.category,
                                color = if (isTraffic) AccentAmber else AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Channel Source Tag
                val (tagText, tagColor) = when {
                    channel.isTrafficCam -> "DOT LIVE CAM" to AccentAmber
                    channel.category == "Classic Cartoons" -> "RETRO TOONS" to AccentCyan
                    channel.category == "Anime" -> "ANIME 24/7" to AccentPurple
                    channel.isOnlineFast -> "FREE FAST" to AccentEmerald
                    else -> "EMBY TUNER" to AccentPurple
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = tagText,
                        color = tagColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // EPG Program Info Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CinemaSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
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
                                text = if (isTraffic) "CAMERA ACTIVE" else "NOW PLAYING",
                                color = if (isTraffic) AccentAmber else Color(0xFFE50914),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        val timeInfo = channel.currentProgram?.formattedTime
                        if (!timeInfo.isNullOrBlank()) {
                            Text(
                                text = timeInfo,
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (!channel.weatherNote.isNullOrBlank()) {
                            Text(
                                text = channel.weatherNote,
                                color = AccentEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Live Stream",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = channel.currentProgram?.name ?: "24/7 Continuous Live Broadcast",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!channel.currentProgram?.overview.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = channel.currentProgram!!.overview!!,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Upcoming Schedule Guide
                    if (channel.upcomingPrograms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "UPCOMING SCHEDULE",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        channel.upcomingPrograms.take(3).forEach { upNext ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = upNext.name,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!upNext.startDate.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = upNext.startDate,
                                        color = AccentCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Watch Live Button
            Button(
                onClick = onWatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTraffic) AccentAmber else Color(0xFFE50914),
                    contentColor = if (isTraffic) CinemaBlack else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("watch_live_${channel.id}")
            ) {
                Icon(
                    imageVector = if (isTraffic) Icons.Default.Videocam else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTraffic) "View Traffic Camera Live" else "Watch Channel Live",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

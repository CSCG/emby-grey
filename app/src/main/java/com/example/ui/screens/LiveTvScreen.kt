package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.EmbyItemDto
import com.example.data.model.LiveTvChannelDto
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
            .background(AppBackground)
            .statusBarsPadding()
            .testTag("live_tv_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live TV",
                    style = AppTypography.sectionTitle
                )
                Text(
                    text = "${channels.size} channels available",
                    style = AppTypography.metadata
                )
            }

            IconButton(
                onClick = { viewModel.refreshLiveTv() },
                modifier = Modifier.testTag("refresh_live_tv_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = AppTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
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
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(AppRadii.badge),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppSelectedSurface,
                        selectedLabelColor = AppTextPrimary,
                        containerColor = AppElevatedSurface,
                        labelColor = AppTextSecondary
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        // Channels & EPG List
        if (isLoading && channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppAccent,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.md, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                items(filteredChannels, key = { it.id }) { channel ->
                    LiveChannelGuideCard(
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

/**
 * Modern content-first Live TV channel item.
 * Clean 16:9 thumbnail preview, small red LIVE indicator, title, metadata, and upcoming guide.
 */
@Composable
fun LiveChannelGuideCard(
    channel: LiveTvChannelDto,
    logoUrl: String?,
    onWatch: () -> Unit
) {
    val context = LocalContext.current
    val isTraffic = channel.isTrafficCam
    val previewImage = channel.snapshotUrl ?: logoUrl

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadii.card))
            .clickable(onClick = onWatch)
            .testTag("channel_${channel.id}"),
        shape = RoundedCornerShape(AppRadii.card),
        color = AppElevatedSurface
    ) {
        Column(modifier = Modifier.padding(AppSpacing.sm)) {
            // Top Preview & Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 16:9 Preview Frame
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(AppRadii.tag))
                        .background(AppSurface)
                ) {
                    if (!previewImage.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(previewImage)
                                .crossfade(true)
                                .build(),
                            contentDescription = channel.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(modifier = Modifier.fillMaxSize().background(AppSurface))
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(AppSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = AppTextTertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(AppSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = AppTextTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Small consistent red LIVE indicator
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        shape = RoundedCornerShape(AppRadii.badge),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(AppLiveRed)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.sm))

                // Channel Name & Currently Playing
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = channel.name,
                            style = AppTypography.cardTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (channel.number != null) {
                            Text(
                                text = "Ch ${channel.number}",
                                style = AppTypography.metadata,
                                color = AppTextTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val programName = channel.currentProgram?.name ?: if (isTraffic) (channel.location ?: "Traffic Camera") else "Live Stream"
                    Text(
                        text = programName,
                        style = AppTypography.body,
                        color = AppTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val timeOrSource = channel.currentProgram?.formattedTime ?: channel.location ?: channel.category
                    Text(
                        text = timeOrSource,
                        style = AppTypography.metadata,
                        color = if (channel.currentProgram?.formattedTime != null) AppAccent else AppTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Overview or upcoming programs if present
            if (!channel.currentProgram?.overview.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = channel.currentProgram!!.overview!!,
                    style = AppTypography.metadata,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Upcoming Schedule Guide
            if (channel.upcomingPrograms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppDivider)
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))

                channel.upcomingPrograms.take(2).forEach { upNext ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = upNext.name,
                            style = AppTypography.metadata,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (!upNext.startDate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Text(
                                text = upNext.startDate,
                                style = AppTypography.metadata,
                                color = AppTextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onWatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppSelectedSurface,
                        contentColor = AppTextPrimary
                    ),
                    shape = RoundedCornerShape(AppRadii.button),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("watch_live_${channel.id}"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Watch",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
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

/**
 * Content-first media poster card.
 * Artwork dominates with restrained 8dp corners. No outer card container or heavy borders.
 */
@Composable
fun MediaPosterCard(
    item: EmbyItemDto,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 2f / 3f,
    showRemainingTime: Boolean = false
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadii.card))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppAccent.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag("media_card_${item.id}")
    ) {
        // Thumbnail Artwork Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(AppRadii.card))
                .background(AppElevatedSurface)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppElevatedSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppAccent.copy(alpha = 0.4f),
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppElevatedSurface)
                            .padding(AppSpacing.xs),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.type == "Series") Icons.Default.Tv else Icons.Default.Movie,
                            contentDescription = null,
                            tint = AppTextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )

            // Subtle bottom gradient for readability if needed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                            startY = 160f
                        )
                    )
            )

            // Rating indicator (minimal, top-end)
            if (item.communityRating != null && item.communityRating > 0 && aspectRatio <= 1f) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(AppSpacing.xs),
                    shape = RoundedCornerShape(AppRadii.badge),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.1f", item.communityRating),
                            color = AppTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Flush playback progress bar against bottom edge of thumbnail
            if (item.resumeFraction > 0.02f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(AppSelectedSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = item.resumeFraction.coerceIn(0f, 1f))
                            .height(3.dp)
                            .background(AppAccent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        // Title & metadata below artwork
        Text(
            text = item.name,
            style = AppTypography.cardTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        val metadataText = if (showRemainingTime && item.resumePositionTicks > 0L && item.runTimeTicks != null) {
            val remainingTicks = (item.runTimeTicks - item.resumePositionTicks).coerceAtLeast(0L)
            val remainingMins = (remainingTicks / 10_000_000L / 60L).toInt()
            if (remainingMins > 0) "${remainingMins}m remaining" else "${item.durationMinutes}m"
        } else {
            val year = item.productionYear?.toString() ?: ""
            val duration = if (item.durationMinutes > 0) "${item.durationMinutes}m" else ""
            listOf(year, duration).filter { it.isNotBlank() }.joinToString(" • ")
        }

        if (metadataText.isNotBlank()) {
            Text(
                text = metadataText,
                style = AppTypography.metadata,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Clean Section Header without redundant promotional descriptions.
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = AppTypography.sectionTitle
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTypography.metadata
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = AppAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(AppRadii.badge))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Refined Library collection card without loud neon gradients.
 */
@Composable
fun LibraryCard(
    name: String,
    collectionType: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedType = collectionType.lowercase()
    val icon = when {
        normalizedType.contains("movie") -> Icons.Default.Movie
        normalizedType.contains("tv") -> Icons.Default.Tv
        normalizedType.contains("collection") || normalizedType.contains("boxset") -> Icons.Default.VideoLibrary
        normalizedType.contains("playlist") -> Icons.Default.PlaylistPlay
        normalizedType.contains("live") || normalizedType.contains("iptv") -> Icons.Default.LiveTv
        else -> Icons.Default.Folder
    }

    val subtitle = when {
        normalizedType.contains("movie") -> "Feature Films"
        normalizedType.contains("tv") -> "TV Series"
        normalizedType.contains("collection") -> "Collections"
        normalizedType.contains("playlist") -> "Playlists"
        normalizedType.contains("live") || normalizedType.contains("iptv") -> "Live Streams"
        else -> "Collection"
    }

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadii.card))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppAccent.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .testTag("library_card_$name"),
        shape = RoundedCornerShape(AppRadii.card),
        color = AppElevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTextSecondary,
                modifier = Modifier.size(26.dp)
            )

            Column {
                Text(
                    text = name,
                    style = AppTypography.cardTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTypography.metadata,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Content-first Live Channel / Stream preview item.
 * Uses 16:9 thumbnail preview with consistent small red LIVE indicator.
 */
@Composable
fun LiveChannelCard(
    channel: LiveTvChannelDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isTraffic = channel.isTrafficCam
    val image = thumbnailUrl ?: channel.snapshotUrl ?: channel.logoUrl

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadii.card))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AppAccent.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag("home_live_channel_${channel.id}")
    ) {
        // 16:9 Preview Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(AppRadii.card))
                .background(AppElevatedSurface)
        ) {
            if (!image.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentDescription = channel.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().background(AppElevatedSurface))
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize().background(AppElevatedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = AppTextTertiary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(AppElevatedSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = AppTextTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Dark bottom scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 40f
                        )
                    )
            )

            // Small consistent red LIVE indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(AppSpacing.xs),
                shape = RoundedCornerShape(AppRadii.badge),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(AppLiveRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        // Title and source
        Text(
            text = channel.name,
            style = AppTypography.cardTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        val subtitle = when {
            isTraffic && !channel.location.isNullOrBlank() -> channel.location
            !channel.currentProgram?.name.isNullOrBlank() -> channel.currentProgram!!.name
            else -> channel.category
        }

        Text(
            text = subtitle,
            style = AppTypography.metadata,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

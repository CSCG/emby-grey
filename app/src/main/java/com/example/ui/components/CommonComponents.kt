package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.EmbyItemDto
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MediaPosterCard(
    item: EmbyItemDto,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 2f / 3f
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("media_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = CinemaSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .background(CinemaDarkSurface)
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
                                .background(CinemaDarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
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
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (item.type == "Series") Icons.Default.Tv else Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                )

                // Bottom gradient for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 140f
                            )
                        )
                )

                // Rating Badge
                if (item.communityRating != null && item.communityRating > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = AccentAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format("%.1f", item.communityRating),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Media type pill
                if (item.type == "Series") {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = AccentCyan.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "SERIES",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Resume progress bar
                if (item.resumeFraction > 0.02f) {
                    LinearProgressIndicator(
                        progress = { item.resumeFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = AccentCyan,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            // Text metadata
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val year = item.productionYear?.toString() ?: ""
                    val duration = if (item.durationMinutes > 0) "${item.durationMinutes}m" else ""
                    Text(
                        text = listOf(year, duration).filter { it.isNotBlank() }.joinToString(" • "),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    if (item.officialRating != null) {
                        Text(
                            text = item.officialRating,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

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
        normalizedType.contains("tv") -> "TV Series & Shows"
        normalizedType.contains("collection") || normalizedType.contains("boxset") -> "Collections & Boxsets"
        normalizedType.contains("playlist") -> "Custom Playlists"
        normalizedType.contains("live") || normalizedType.contains("iptv") -> "Live Streams & IPTV"
        else -> "Media Collection"
    }

    val gradientColors = when {
        normalizedType.contains("movie") -> listOf(Color(0xFF0072FF), Color(0xFF00C6FF))
        normalizedType.contains("tv") -> listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
        normalizedType.contains("collection") -> listOf(Color(0xFF00897B), Color(0xFF4DB6AC))
        normalizedType.contains("playlist") -> listOf(Color(0xFF5E35B1), Color(0xFF9575CD))
        normalizedType.contains("live") || normalizedType.contains("iptv") -> listOf(Color(0xFFE53935), Color(0xFFFF7043))
        else -> listOf(Color(0xFF11998E), Color(0xFF38EF7D))
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("library_card_$name"),
        colors = CardDefaults.cardColors(containerColor = CinemaSurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.35f) }))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = AccentCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

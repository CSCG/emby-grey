package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.local.DownloadItemEntity
import com.example.data.model.PlaybackQuality
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AppAccent
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppDivider
import com.example.ui.theme.AppElevatedSurface
import com.example.ui.theme.AppRadii
import com.example.ui.theme.AppSelectedSurface
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.AppTextTertiary
import com.example.ui.theme.AppTypography
import com.example.ui.viewmodel.EmbyViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    viewModel: EmbyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToItem: (String) -> Unit = {},
    onPlayMedia: (String, PlaybackQuality, Boolean) -> Unit
) {
    BackHandler { onNavigateBack() }

    val item by viewModel.selectedItem.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val collectionItems by viewModel.collectionItems.collectAsState()
    val similarItems by viewModel.similarItems.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    val selectedPerson by viewModel.selectedPerson.collectAsState()
    val personItems by viewModel.personItems.collectAsState()

    val currentDownload = downloads.firstOrNull { it.id == itemId }
    var transcodeMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.selectItem(itemId)
    }

    if (item == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppAccent)
        }
        return
    }

    val currentItem = item!!
    val resumeMs = currentItem.resumePositionTicks / 10_000L
    val totalDurationMs = currentItem.durationMinutes * 60 * 1000L
    val hasResume = resumeMs > 5000L && (totalDurationMs <= 0 || resumeMs < (totalDurationMs - 15000L))
    val isSeries = currentItem.type.equals("Series", ignoreCase = true)
    val isCollection = currentItem.type.equals("BoxSet", ignoreCase = true) ||
            currentItem.type.equals("CollectionFolder", ignoreCase = true) ||
            currentItem.type.equals("Playlist", ignoreCase = true) ||
            currentItem.type.equals("Folder", ignoreCase = true) ||
            currentItem.type?.contains("Collection", ignoreCase = true) == true ||
            currentItem.id.startsWith("boxset_") ||
            collectionItems.isNotEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("item_detail_screen")
    ) {
        // Full Hero Backdrop Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                val detailContext = LocalContext.current
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(detailContext)
                        .data(viewModel.getImageUrl(currentItem, isBackdrop = true))
                        .crossfade(true)
                        .build(),
                    contentDescription = currentItem.name,
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
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = AppTextTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                )

                // Atmospheric functional dark vertical gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    AppBackground.copy(alpha = 0.8f),
                                    AppBackground
                                )
                            )
                        )
                )

                // Top Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                // Poster & Title Row at Bottom of Backdrop
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(105.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(AppRadii.card))
                            .background(AppElevatedSurface)
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(detailContext)
                                .data(viewModel.getImageUrl(currentItem, isBackdrop = false))
                                .crossfade(true)
                                .build(),
                            contentDescription = currentItem.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.sm))

                    Column {
                        Text(
                            text = currentItem.name,
                            style = AppTypography.heroTitle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xxs))

                        val year = currentItem.productionYear?.toString()
                        val duration = if (currentItem.durationMinutes > 0) "${currentItem.durationMinutes} min" else null
                        val rating = currentItem.officialRating
                        val metaParts = listOfNotNull(year, duration, rating).joinToString(" • ")

                        if (metaParts.isNotBlank()) {
                            Text(
                                text = metaParts,
                                style = AppTypography.metadata
                            )
                        }

                        if (currentItem.communityRating != null && currentItem.communityRating!! > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", currentItem.communityRating),
                                    style = AppTypography.metadata,
                                    color = AppTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Play, Resume, Transcode)
        item {
            val targetEpisode = if (isSeries) {
                episodes.firstOrNull { it.resumePositionTicks > 0 && !it.isPlayed }
                    ?: episodes.firstOrNull { !it.isPlayed }
                    ?: episodes.firstOrNull()
            } else null
            val targetTitle = if (isCollection) {
                collectionItems.firstOrNull { it.resumePositionTicks > 0 && !it.isPlayed }
                    ?: collectionItems.firstOrNull { !it.isPlayed }
                    ?: collectionItems.firstOrNull()
            } else null
            val targetPlayableId = targetEpisode?.id ?: targetTitle?.id ?: currentItem.id

            Column(modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    // Primary Play Button
                    Button(
                        onClick = { onPlayMedia(targetPlayableId, PlaybackQuality.DIRECT_PLAY, false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("direct_play_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppAccent,
                            contentColor = Color(0xFF04191C)
                        ),
                        shape = RoundedCornerShape(AppRadii.button)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val buttonLabel = when {
                            hasResume -> "Resume"
                            isSeries && targetEpisode != null -> "Play S${targetEpisode.parentIndexNumber ?: 1}E${targetEpisode.indexNumber ?: 1}"
                            isCollection && targetTitle != null -> "Play Collection"
                            isCollection -> "Play Collection"
                            else -> "Play"
                        }
                        Text(
                            text = buttonLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    if (hasResume) {
                        Button(
                            onClick = { onPlayMedia(targetPlayableId, PlaybackQuality.DIRECT_PLAY, true) },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("start_from_beginning_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppElevatedSurface,
                                contentColor = AppTextPrimary
                            ),
                            shape = RoundedCornerShape(AppRadii.button)
                        ) {
                            Text("Start Over", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Transcode Options Dropdown Button
                    Box {
                        Button(
                            onClick = { transcodeMenuExpanded = true },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("transcode_menu_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppElevatedSurface,
                                contentColor = AppTextSecondary
                            ),
                            shape = RoundedCornerShape(AppRadii.button)
                        ) {
                            Icon(Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quality", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        DropdownMenu(
                            expanded = transcodeMenuExpanded,
                            onDismissRequest = { transcodeMenuExpanded = false },
                            modifier = Modifier
                                .background(AppElevatedSurface)
                                .clip(RoundedCornerShape(AppRadii.card))
                        ) {
                            DropdownMenuItem(
                                text = { Text("1080p (10 Mbps)", color = AppTextPrimary, fontSize = 14.sp) },
                                onClick = {
                                    transcodeMenuExpanded = false
                                    onPlayMedia(targetPlayableId, PlaybackQuality.P1080, false)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("720p (4 Mbps)", color = AppTextPrimary, fontSize = 14.sp) },
                                onClick = {
                                    transcodeMenuExpanded = false
                                    onPlayMedia(targetPlayableId, PlaybackQuality.P720, false)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("480p (1.5 Mbps)", color = AppTextPrimary, fontSize = 14.sp) },
                                onClick = {
                                    transcodeMenuExpanded = false
                                    onPlayMedia(targetPlayableId, PlaybackQuality.P480, false)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                // Offline Download Action
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppRadii.card))
                        .clickable {
                            if (currentDownload == null) {
                                viewModel.downloadItem(currentItem)
                            } else if (currentDownload.status == DownloadItemEntity.STATUS_COMPLETED) {
                                viewModel.deleteDownload(currentItem.id)
                            }
                        }
                        .testTag("offline_sync_button"),
                    shape = RoundedCornerShape(AppRadii.card),
                    color = AppElevatedSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (currentDownload?.status) {
                                DownloadItemEntity.STATUS_COMPLETED -> Icons.Default.CloudDone
                                else -> Icons.Default.CloudDownload
                            }
                            val iconTint = if (currentDownload?.status == DownloadItemEntity.STATUS_COMPLETED) AppAccent else AppTextSecondary

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                val title = when (currentDownload?.status) {
                                    DownloadItemEntity.STATUS_COMPLETED -> "Downloaded for Offline"
                                    DownloadItemEntity.STATUS_DOWNLOADING -> "Downloading..."
                                    else -> "Download"
                                }
                                Text(
                                    text = title,
                                    style = AppTypography.cardTitle
                                )
                                val sub = when (currentDownload?.status) {
                                    DownloadItemEntity.STATUS_COMPLETED -> "${currentDownload.formattedSize} • Tap to delete"
                                    DownloadItemEntity.STATUS_DOWNLOADING -> "${(currentDownload.progressFraction * 100).toInt()}% complete"
                                    else -> "Save to device for offline playback"
                                }
                                Text(
                                    text = sub,
                                    style = AppTypography.metadata
                                )
                            }
                        }

                        if (currentDownload?.status == DownloadItemEntity.STATUS_DOWNLOADING) {
                            CircularProgressIndicator(
                                progress = { currentDownload.progressFraction },
                                modifier = Modifier.size(20.dp),
                                color = AppAccent,
                                strokeWidth = 2.dp
                            )
                        } else if (currentDownload?.status == DownloadItemEntity.STATUS_COMPLETED) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AppTextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Genres Chips
        if (!currentItem.genres.isNullOrEmpty()) {
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    currentItem.genres.forEach { genre ->
                        Surface(
                            shape = RoundedCornerShape(AppRadii.badge),
                            color = AppElevatedSurface
                        ) {
                            Text(
                                text = genre,
                                style = AppTypography.metadata,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Overview Synopsis
        if (!currentItem.overview.isNullOrBlank()) {
            item {
                Column(modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
                    Text(
                        text = "Overview",
                        style = AppTypography.sectionTitle
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = currentItem.overview,
                        style = AppTypography.body
                    )
                }
            }
        }

        // TV Series Episodes List
        if (currentItem.type.equals("Series", ignoreCase = true)) {
            if (episodes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "Episodes (${episodes.size})",
                        style = AppTypography.sectionTitle,
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                    )
                }

            items(episodes) { ep ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = 4.dp)
                        .clip(RoundedCornerShape(AppRadii.card))
                        .clickable { onPlayMedia(ep.id, PlaybackQuality.DIRECT_PLAY, false) },
                    shape = RoundedCornerShape(AppRadii.card),
                    color = AppElevatedSurface
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(AppRadii.badge))
                                .background(AppSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AppTextPrimary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.sm))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "E${ep.indexNumber ?: 1}: ${ep.name}",
                                style = AppTypography.cardTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!ep.overview.isNullOrBlank()) {
                                Text(
                                    text = ep.overview,
                                    style = AppTypography.metadata,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.downloadItem(ep) }) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Download",
                                tint = AppTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

        // Collection Titles List
        if (isCollection) {
            if (collectionItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    SectionHeader(
                        title = "Titles in Collection (${collectionItems.size})",
                        subtitle = "Select any title to view details or play"
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))
                }

                items(collectionItems) { titleItem ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = 4.dp)
                            .clip(RoundedCornerShape(AppRadii.card))
                            .clickable { onNavigateToItem(titleItem.id) }
                            .testTag("collection_title_${titleItem.id}"),
                        shape = RoundedCornerShape(AppRadii.card),
                        color = AppElevatedSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(AppSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Poster Thumbnail
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(AppRadii.badge))
                                    .background(AppSurface)
                            ) {
                                SubcomposeAsyncImage(
                                    model = viewModel.getImageUrl(titleItem, isBackdrop = false),
                                    contentDescription = titleItem.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(AppSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = AppAccent,
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 1.5.dp
                                            )
                                        }
                                    },
                                    error = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(AppSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (titleItem.type == "Series") Icons.Default.Tv else Icons.Default.Movie,
                                                contentDescription = null,
                                                tint = AppTextTertiary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                )

                                // Resume Progress bar if partially watched
                                if (titleItem.resumeFraction > 0.02f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(AppSelectedSurface)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = titleItem.resumeFraction.coerceIn(0f, 1f))
                                                .height(3.dp)
                                                .background(AppAccent)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(AppSpacing.sm))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = titleItem.name,
                                    style = AppTypography.cardTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val year = titleItem.productionYear?.toString()
                                val dur = if (titleItem.durationMinutes > 0) "${titleItem.durationMinutes} min" else null
                                val itemType = if (titleItem.type == "Series") "TV Series" else "Movie"
                                val meta = listOfNotNull(year, dur, itemType).joinToString(" • ")
                                Text(
                                    text = meta,
                                    style = AppTypography.metadata,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!titleItem.overview.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = titleItem.overview,
                                        style = AppTypography.metadata,
                                        color = AppTextTertiary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(AppSpacing.xs))

                            // Action button: Play this specific title
                            IconButton(
                                onClick = { onPlayMedia(titleItem.id, PlaybackQuality.DIRECT_PLAY, false) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(AppSurface, CircleShape)
                                    .testTag("play_title_button_${titleItem.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play ${titleItem.name}",
                                    tint = AppAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cast & Crew Section
        val people = currentItem.people ?: emptyList()
        if (people.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                SectionHeader(title = "Cast & Crew")
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(people) { person ->
                        Surface(
                            modifier = Modifier
                                .width(90.dp)
                                .clip(RoundedCornerShape(AppRadii.card))
                                .clickable { viewModel.selectPerson(person) }
                                .testTag("person_${person.id ?: person.name}"),
                            shape = RoundedCornerShape(AppRadii.card),
                            color = AppElevatedSurface
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.xs),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(AppSurface)
                                ) {
                                    val photoUrl = viewModel.getPersonImageUrl(person)
                                    if (photoUrl != null) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = person.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = person.name.take(1),
                                                color = AppTextSecondary,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(AppSpacing.xxs))

                                Text(
                                    text = person.name,
                                    style = AppTypography.cardTitle,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!person.role.isNullOrBlank()) {
                                    Text(
                                        text = person.role,
                                        style = AppTypography.metadata,
                                        color = AppTextTertiary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // More Like This / Recommended Titles Section
        if (similarItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                SectionHeader(title = "More Like This")
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(similarItems) { sim ->
                        MediaPosterCard(
                            item = sim,
                            imageUrl = viewModel.getImageUrl(sim, isBackdrop = false),
                            onClick = { viewModel.selectItem(sim.id) },
                            modifier = Modifier.width(135.dp),
                            aspectRatio = 2f / 3f
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Actor Filmography Bottom Sheet
    if (selectedPerson != null) {
        val person = selectedPerson!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedPerson() },
            containerColor = AppElevatedSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .padding(AppSpacing.md)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(AppSurface)
                    ) {
                        val photoUrl = viewModel.getPersonImageUrl(person)
                        if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = person.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Column {
                        Text(
                            text = person.name,
                            style = AppTypography.sectionTitle
                        )
                        if (!person.role.isNullOrBlank()) {
                            Text(
                                text = person.role,
                                style = AppTypography.metadata
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                Text(
                    text = "Titles on this Server",
                    style = AppTypography.cardTitle
                )

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                if (personItems.isEmpty()) {
                    Text(
                        text = "Searching media libraries...",
                        style = AppTypography.metadata,
                        modifier = Modifier.padding(vertical = AppSpacing.sm)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(personItems) { pItem ->
                            MediaPosterCard(
                                item = pItem,
                                imageUrl = viewModel.getImageUrl(pItem, isBackdrop = false),
                                onClick = {
                                    viewModel.clearSelectedPerson()
                                    viewModel.selectItem(pItem.id)
                                },
                                modifier = Modifier.width(120.dp),
                                aspectRatio = 2f / 3f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))
            }
        }
    }
}

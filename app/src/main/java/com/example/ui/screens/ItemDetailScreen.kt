package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.repository.DemoDataProvider
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaCardBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EmbyViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    viewModel: EmbyViewModel,
    onNavigateBack: () -> Unit,
    onPlayMedia: (String, PlaybackQuality, Boolean) -> Unit
) {
    BackHandler { onNavigateBack() }

    LaunchedEffect(itemId) {
        viewModel.selectItem(itemId)
    }

    val item by viewModel.selectedItem.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val similarItems by viewModel.similarItems.collectAsState()
    val selectedPerson by viewModel.selectedPerson.collectAsState()
    val personItems by viewModel.personItems.collectAsState()

    val currentDownload = downloads.firstOrNull { it.id == itemId }
    var transcodeMenuExpanded by remember { mutableStateOf(false) }

    if (item == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CinemaBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentCyan)
        }
        return
    }

    val currentItem = item!!
    val resumeMs = currentItem.resumePositionTicks / 10_000L
    val totalDurationMs = currentItem.durationMinutes * 60 * 1000L
    val hasResume = resumeMs > 5000L && (totalDurationMs <= 0 || resumeMs < (totalDurationMs - 15000L))
    val isPlayed = currentItem.userData?.played == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .testTag("item_detail_screen")
    ) {
        // Full Hero Backdrop Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
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
                        Box(
                            modifier = Modifier.fillMaxSize().background(CinemaDarkSurface),
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
                                tint = TextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                )

                // Atmospheric Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    CinemaBlack.copy(alpha = 0.8f),
                                    CinemaBlack
                                )
                            )
                        )
                )

                // Top Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, CinemaCardBorder, RoundedCornerShape(10.dp))
                            .background(CinemaDarkSurface)
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(detailContext)
                                .data(viewModel.getImageUrl(currentItem, isBackdrop = false))
                                .crossfade(true)
                                .build(),
                            contentDescription = currentItem.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(CinemaDarkSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = AccentCyan.copy(alpha = 0.5f),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = currentItem.name,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentItem.productionYear != null) {
                                Text(
                                    text = currentItem.productionYear.toString(),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            if (currentItem.durationMinutes > 0) {
                                Text(
                                    text = "${currentItem.durationMinutes} min",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            if (currentItem.officialRating != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CinemaSurfaceVariant
                                ) {
                                    Text(
                                        text = currentItem.officialRating!!,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (currentItem.communityRating != null && currentItem.communityRating!! > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = AccentAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f / 10", currentItem.communityRating),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Direct Play, Transcode Menu, Offline Download)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Direct Play / Resume Button
                    Button(
                        onClick = { onPlayMedia(currentItem.id, PlaybackQuality.DIRECT_PLAY, false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("direct_play_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = CinemaBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasResume) "Resume" else "Direct Play",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (hasResume) {
                        OutlinedButton(
                            onClick = { onPlayMedia(currentItem.id, PlaybackQuality.DIRECT_PLAY, true) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("start_from_beginning_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Over", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Transcode Options Dropdown Button
                    Box {
                        OutlinedButton(
                            onClick = { transcodeMenuExpanded = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("transcode_menu_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(AccentPurple, AccentCyan))
                            )
                        ) {
                            Icon(Icons.Default.Transform, contentDescription = "Transcode", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Transcode", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = transcodeMenuExpanded,
                            onDismissRequest = { transcodeMenuExpanded = false },
                            modifier = Modifier.background(CinemaDarkSurface)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Default.HighQuality, contentDescription = null, tint = AccentCyan)
                                },
                                text = {
                                    Column {
                                        Text("1080p (10 Mbps)", color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Hardware accelerated", color = TextMuted, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    transcodeMenuExpanded = false
                                    onPlayMedia(currentItem.id, PlaybackQuality.P1080, false)
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = AccentCyan)
                                },
                                text = {
                                    Column {
                                        Text("720p (4 Mbps)", color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Smooth HD streaming", color = TextMuted, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    transcodeMenuExpanded = false
                                    onPlayMedia(currentItem.id, PlaybackQuality.P720, false)
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = AccentCyan)
                                },
                                text = {
                                    Column {
                                        Text("480p (1.5 Mbps)", color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Mobile data saving", color = TextMuted, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    transcodeMenuExpanded = false
                                    onPlayMedia(currentItem.id, PlaybackQuality.P480, false)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Offline Synchronization Action Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (currentDownload == null) {
                                viewModel.downloadItem(currentItem)
                            } else if (currentDownload.status == DownloadItemEntity.STATUS_COMPLETED) {
                                viewModel.deleteDownload(currentItem.id)
                            }
                        }
                        .border(1.dp, CinemaCardBorder, RoundedCornerShape(12.dp))
                        .testTag("offline_sync_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = CinemaSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (currentDownload?.status) {
                                DownloadItemEntity.STATUS_COMPLETED -> Icons.Default.CloudDone
                                DownloadItemEntity.STATUS_DOWNLOADING -> Icons.Default.CloudDownload
                                else -> Icons.Default.CloudDownload
                            }
                            val iconColor = when (currentDownload?.status) {
                                DownloadItemEntity.STATUS_COMPLETED -> AccentEmerald
                                DownloadItemEntity.STATUS_DOWNLOADING -> AccentCyan
                                else -> TextSecondary
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = "Download",
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                val title = when (currentDownload?.status) {
                                    DownloadItemEntity.STATUS_COMPLETED -> "Downloaded for Offline Playback"
                                    DownloadItemEntity.STATUS_DOWNLOADING -> "Downloading to Device..."
                                    else -> "Download for Offline Synchronization"
                                }
                                Text(
                                    text = title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val sub = when (currentDownload?.status) {
                                    DownloadItemEntity.STATUS_COMPLETED -> "${currentDownload.formattedSize} • Tap to remove"
                                    DownloadItemEntity.STATUS_DOWNLOADING -> "${(currentDownload.progressFraction * 100).toInt()}% completed"
                                    else -> "Play anywhere without an active connection"
                                }
                                Text(
                                    text = sub,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (currentDownload?.status == DownloadItemEntity.STATUS_DOWNLOADING) {
                            CircularProgressIndicator(
                                progress = { currentDownload.progressFraction },
                                modifier = Modifier.size(24.dp),
                                color = AccentCyan,
                                strokeWidth = 2.5.dp
                            )
                        } else if (currentDownload?.status == DownloadItemEntity.STATUS_COMPLETED) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
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
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentItem.genres.forEach { genre ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CinemaSurfaceVariant
                        ) {
                            Text(
                                text = genre,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Overview Synopsis
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Overview",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentItem.overview ?: "No overview description available.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // Technical Media & Subtitle Details
        item {
            val source = currentItem.mediaSources?.firstOrNull()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, CinemaCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = CinemaDarkSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Stream & Transcode Details",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ClosedCaption, contentDescription = "Subtitles", tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val subCount = source?.mediaStreams?.count { it.type.equals("Subtitle", true) } ?: 0
                            Text(
                                text = if (subCount > 0) "$subCount Subtitle Tracks" else "Subtitles Available",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Audiotrack, contentDescription = "Audio", tint = AccentEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val audioCodec = source?.mediaStreams?.firstOrNull { it.type.equals("Audio", true) }?.codec ?: "AAC"
                            Text(
                                text = "Audio: $audioCodec",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Direct Play: Supported",
                            color = AccentEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hardware Transcode: Enabled",
                            color = AccentPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // TV Series Episodes List
        if (currentItem.type == "Series" && episodes.isNotEmpty()) {
            item {
                Text(
                    text = "Episodes (${episodes.size})",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            items(episodes) { ep ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPlayMedia(ep.id, PlaybackQuality.DIRECT_PLAY, false) }
                        .border(1.dp, CinemaCardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = CinemaSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CinemaDarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AccentCyan)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "E${ep.indexNumber ?: 1}: ${ep.name}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (ep.overview != null) {
                                Text(
                                    text = ep.overview,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.downloadItem(ep) }) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Download Episode",
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Cast & Crew Section
        val people = currentItem.people ?: emptyList()
        if (people.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Cast & Crew",
                    subtitle = "Actors, directors, and creators"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(people) { person ->
                        Surface(
                            modifier = Modifier
                                .width(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectPerson(person) }
                                .testTag("person_${person.id ?: person.name}"),
                            shape = RoundedCornerShape(12.dp),
                            color = CinemaSurfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(CinemaDarkSurface)
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
                                                color = AccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = person.name,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                if (!person.role.isNullOrBlank()) {
                                    Text(
                                        text = person.role,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // More Like This / Recommended Titles Section
        if (similarItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "More Like This",
                    subtitle = "Recommended from your media server"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(similarItems) { sim ->
                        MediaPosterCard(
                            item = sim,
                            imageUrl = viewModel.getImageUrl(sim, isBackdrop = false),
                            onClick = {
                                viewModel.selectItem(sim.id)
                            },
                            modifier = Modifier.width(135.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Actor Filmography Bottom Sheet
    if (selectedPerson != null) {
        val person = selectedPerson!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedPerson() },
            containerColor = CinemaDarkSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CinemaSurfaceVariant)
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = person.name,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!person.role.isNullOrBlank()) {
                            Text(
                                text = "Known for: ${person.role}",
                                color = AccentCyan,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Titles on this Server",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (personItems.isEmpty()) {
                    Text(
                        text = "Searching media libraries...",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

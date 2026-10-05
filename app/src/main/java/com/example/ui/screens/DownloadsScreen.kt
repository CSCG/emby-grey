package com.example.ui.screens

import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.data.local.DownloadItemEntity
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaCardBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EmbyViewModel
import java.io.File

@Composable
fun DownloadsScreen(
    viewModel: EmbyViewModel,
    onPlayOfflineMedia: (DownloadItemEntity) -> Unit
) {
    val downloads by viewModel.downloads.collectAsState()
    var itemToDelete by remember { mutableStateOf<DownloadItemEntity?>(null) }

    val completedDownloads = downloads.filter { it.status == DownloadItemEntity.STATUS_COMPLETED }
    val activeDownloads = downloads.filter { it.status == DownloadItemEntity.STATUS_DOWNLOADING || it.status == DownloadItemEntity.STATUS_QUEUED }

    val usedBytes = viewModel.getUsedStorageBytes()
    val context = LocalContext.current
    val freeBytes = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            10_000_000_000L
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .statusBarsPadding()
            .testTag("downloads_screen_content"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Title Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AccentCyan, AccentEmerald))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Downloads",
                        tint = CinemaBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Offline Synchronization",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Play media anywhere without an internet connection",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Storage Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, CinemaCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = CinemaDarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = "Storage",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Storage",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${formatBytes(usedBytes)} used",
                            color = AccentCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val usedMb = usedBytes / (1024.0 * 1024.0)
                    val freeMb = freeBytes / (1024.0 * 1024.0)
                    val totalMb = usedMb + freeMb
                    val fraction = if (totalMb > 0) (usedMb / totalMb).toFloat().coerceIn(0.01f, 1f) else 0.05f

                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AccentCyan,
                        trackColor = CinemaSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${completedDownloads.size} items downloaded",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${formatBytes(freeBytes)} free",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Active Downloads
        if (activeDownloads.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Active Downloads (${activeDownloads.size})",
                    subtitle = "Downloading media to local storage"
                )
            }

            items(activeDownloads) { dl ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, CinemaCardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = CinemaSurfaceVariant
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dl.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = dl.displaySubtitle,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(onClick = { viewModel.deleteDownload(dl.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Cancel",
                                    tint = AccentRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { dl.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentCyan,
                            trackColor = CinemaBlack
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(dl.progressFraction * 100).toInt()}% completed",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dl.formattedSize,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Completed Offline Media List
        if (completedDownloads.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Downloaded Media",
                    subtitle = "Ready for immediate offline playback"
                )
            }

            items(completedDownloads) { dl ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPlayOfflineMedia(dl) }
                        .border(1.dp, CinemaCardBorder, RoundedCornerShape(14.dp))
                        .testTag("download_item_${dl.id}"),
                    shape = RoundedCornerShape(14.dp),
                    color = CinemaDarkSurface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Poster Thumbnail
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CinemaSurfaceVariant)
                        ) {
                            if (dl.posterUrl != null) {
                                AsyncImage(
                                    model = dl.posterUrl,
                                    contentDescription = dl.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dl.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = dl.displaySubtitle,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AccentEmerald.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "OFFLINE READY",
                                        color = AccentEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = dl.formattedSize,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { itemToDelete = dl },
                            modifier = Modifier.testTag("delete_download_${dl.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Download",
                                tint = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Empty State
        if (downloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CinemaSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "No Downloads",
                                tint = AccentCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Offline Media Yet",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You can download any movie or TV show episode directly to your device. Look for the \"Download for Offline\" button on any media details page.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text("Delete Downloaded File?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${itemToDelete!!.title}\" from your offline storage?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = itemToDelete!!.id
                        itemToDelete = null
                        viewModel.deleteDownload(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CinemaDarkSurface
        )
    }
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1000) {
        String.format("%.2f GB", mb / 1024.0)
    } else {
        String.format("%.1f MB", mb)
    }
}

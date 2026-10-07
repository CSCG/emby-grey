package com.example.ui.screens

import android.os.Environment
import android.os.StatFs
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DownloadItemEntity
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
            .background(AppBackground)
            .statusBarsPadding()
            .testTag("downloads_screen_content"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Title Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Downloads",
                        style = AppTypography.sectionTitle
                    )
                    Text(
                        text = "${completedDownloads.size} items saved offline",
                        style = AppTypography.metadata
                    )
                }
            }
        }

        // Storage Bar Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                shape = RoundedCornerShape(AppRadii.card),
                color = AppElevatedSurface
            ) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = null,
                                tint = AppTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Text(
                                text = "Device Storage",
                                style = AppTypography.cardTitle
                            )
                        }

                        Text(
                            text = "${formatBytes(usedBytes)} used",
                            style = AppTypography.metadata,
                            color = AppAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    val usedMb = usedBytes / (1024.0 * 1024.0)
                    val freeMb = freeBytes / (1024.0 * 1024.0)
                    val totalMb = usedMb + freeMb
                    val fraction = if (totalMb > 0) (usedMb / totalMb).toFloat().coerceIn(0.01f, 1f) else 0.05f

                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AppAccent,
                        trackColor = AppSelectedSurface
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${completedDownloads.size} items",
                            style = AppTypography.metadata
                        )
                        Text(
                            text = "${formatBytes(freeBytes)} free",
                            style = AppTypography.metadata,
                            color = AppTextTertiary
                        )
                    }
                }
            }
        }

        // Active Downloads
        if (activeDownloads.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = "Downloading",
                    style = AppTypography.cardTitle,
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs)
                )
            }

            items(activeDownloads, key = { it.itemId }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = 4.dp),
                    shape = RoundedCornerShape(AppRadii.card),
                    color = AppElevatedSurface
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.itemName,
                                style = AppTypography.cardTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${(item.progressFraction * 100).toInt()}% • ${item.formattedSize}",
                                style = AppTypography.metadata
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { item.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp)),
                                color = AppAccent,
                                trackColor = AppSelectedSurface
                            )
                        }

                        IconButton(onClick = { viewModel.deleteDownload(item.id) }) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel",
                                tint = AppTextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Completed Downloads
        if (completedDownloads.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = "Downloaded Titles",
                    style = AppTypography.cardTitle,
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs)
                )
            }

            items(completedDownloads, key = { it.itemId }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = 4.dp)
                        .clip(RoundedCornerShape(AppRadii.card))
                        .clickable { onPlayOfflineMedia(item) },
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
                                .width(50.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(AppRadii.tag))
                                .background(AppSurface)
                        ) {
                            if (item.posterUrl != null) {
                                AsyncImage(
                                    model = item.posterUrl,
                                    contentDescription = item.itemName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.sm))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.itemName,
                                style = AppTypography.cardTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val file = File(item.localFilePath ?: "")
                            val exists = !item.localFilePath.isNullOrBlank() && file.exists()
                            val detail = if (exists) "${item.formattedSize} • Ready to play" else "File missing"
                            Text(
                                text = detail,
                                style = AppTypography.metadata,
                                color = if (exists) AppTextSecondary else AppLiveRed
                            )
                        }

                        // Play Button
                        IconButton(onClick = { onPlayOfflineMedia(item) }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = AppAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Delete Button
                        IconButton(onClick = { itemToDelete = item }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AppTextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        } else if (activeDownloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Downloads",
                            style = AppTypography.cardTitle,
                            color = AppTextSecondary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                        Text(
                            text = "Download movies and episodes for offline viewing",
                            style = AppTypography.metadata
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Download", style = AppTypography.cardTitle) },
            text = { Text("Remove \"${itemToDelete!!.itemName}\" from your device?", style = AppTypography.body) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDownload(itemToDelete!!.itemId)
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = AppLiveRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = AppTextSecondary)
                }
            },
            containerColor = AppElevatedSurface,
            shape = RoundedCornerShape(AppRadii.modal)
        )
    }
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) {
        String.format("%.1f GB", mb / 1024.0)
    } else {
        String.format("%.0f MB", mb)
    }
}

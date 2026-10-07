package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
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
import com.example.ui.components.LibraryCard
import com.example.ui.components.LiveChannelCard
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.SectionHeader
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
    var showServerMenu by remember { mutableStateOf(false) }

    val heroItem = homeState.latestItems.firstOrNull { it.type == "Movie" }
        ?: homeState.latestItems.firstOrNull()
        ?: homeState.continueWatching.firstOrNull()

    // Toonami Aftermath featured channel
    val toonamiChannel = liveTvChannels.firstOrNull { it.id == "free_toonami_aftermath" }

    // Group items for Movies and Shows carousels
    val movieItems = remember(homeState.latestItems) {
        homeState.latestItems.filter { it.type.equals("Movie", ignoreCase = true) }
    }
    val showItems = remember(homeState.latestItems) {
        homeState.latestItems.filter { it.type.equals("Series", ignoreCase = true) }
    }
    val resumePlaybackItems = remember(homeState.continueWatching) {
        homeState.continueWatching.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Simplified Application Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Server name dropdown selector (quiet, content-forward)
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppRadii.badge))
                            .clickable { showServerMenu = true }
                            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = conn?.serverName ?: "Emby",
                            style = AppTypography.sectionTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Server Menu",
                            tint = AppTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showServerMenu,
                        onDismissRequest = { showServerMenu = false },
                        modifier = Modifier
                            .background(AppElevatedSurface)
                            .clip(RoundedCornerShape(AppRadii.card))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = conn?.serverName ?: "Emby Server",
                                        color = AppTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (conn?.isDemo == true) "Demo Mode Active" else (conn?.username ?: "Connected"),
                                        color = AppTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            onClick = {
                                showServerMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Server Settings", color = AppTextPrimary, fontSize = 14.sp) },
                            onClick = {
                                showServerMenu = false
                                onNavigateToSettings()
                            }
                        )
                    }
                }

                // Header actions: Search & Settings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AppTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Large Cinematic Featured Hero
        if (heroItem != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                        .padding(horizontal = AppSpacing.md)
                        .clip(RoundedCornerShape(AppRadii.hero))
                        .background(AppElevatedSurface)
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
                            Box(modifier = Modifier.fillMaxSize().background(AppElevatedSurface))
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize().background(AppElevatedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AppTextTertiary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )

                    // Functional dark vertical gradient over lower portion
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        AppBackground.copy(alpha = 0.7f),
                                        AppBackground.copy(alpha = 0.98f)
                                    ),
                                    startY = 0f
                                )
                            )
                    )

                    // Content info & actions at bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg)
                    ) {
                        Text(
                            text = heroItem.name,
                            style = AppTypography.heroTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xxs))

                        // Metadata line: 2008 • 9 min • Animation
                        val year = heroItem.productionYear?.toString()
                        val duration = if (heroItem.durationMinutes > 0) "${heroItem.durationMinutes} min" else null
                        val genre = heroItem.genres?.firstOrNull() ?: (if (heroItem.type == "Series") "TV Series" else "Movie")
                        val metaParts = listOfNotNull(year, duration, genre).joinToString(" • ")

                        Text(
                            text = metaParts,
                            style = AppTypography.metadata,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!heroItem.overview.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Text(
                                text = heroItem.overview,
                                style = AppTypography.body,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = AppSpacing.xl)
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.md))

                        // Actions: [▶ Play] and transparent [ⓘ Info]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onNavigateToPlayer(heroItem.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppAccent,
                                    contentColor = Color(0xFF04191C)
                                ),
                                shape = RoundedCornerShape(AppRadii.button),
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("hero_play_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.width(AppSpacing.sm))

                            TextButton(
                                onClick = { onNavigateToItem(heroItem.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = AppTextPrimary),
                                shape = RoundedCornerShape(AppRadii.button),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AppTextSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Info",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 3. Resume Playback (for five items / series / movies on the home screen)
        if (resumePlaybackItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Resume Playback",
                    subtitle = "Pick up where you left off",
                    modifier = Modifier.testTag("section_resume_playback")
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(resumePlaybackItems) { item ->
                        MediaPosterCard(
                            item = item,
                            imageUrl = viewModel.getImageUrl(item, isBackdrop = true),
                            onClick = { onNavigateToPlayer(item.id) },
                            modifier = Modifier
                                .width(200.dp)
                                .testTag("resume_item_${item.id}"),
                            aspectRatio = 16f / 9f,
                            showRemainingTime = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 4. Live Now (horizontal 16:9 carousel)
        if (liveTvChannels.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Live TV",
                    actionText = "Guide",
                    onActionClick = onNavigateToLiveTv
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(liveTvChannels.take(10)) { channel ->
                        LiveChannelCard(
                            channel = channel,
                            onClick = {
                                val itemDto = viewModel.buildLiveTvItemDto(channel)
                                onNavigateToPlayer(itemDto.id)
                            },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 5. Recently Added (poster carousel 2:3)
        if (homeState.latestItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Added"
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(homeState.latestItems.take(12)) { item ->
                        MediaPosterCard(
                            item = item,
                            imageUrl = viewModel.getImageUrl(item, isBackdrop = false),
                            onClick = { onNavigateToItem(item.id) },
                            modifier = Modifier.width(135.dp),
                            aspectRatio = 2f / 3f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 6. Movies (if available, poster carousel)
        if (movieItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Movies"
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(movieItems) { item ->
                        MediaPosterCard(
                            item = item,
                            imageUrl = viewModel.getImageUrl(item, isBackdrop = false),
                            onClick = { onNavigateToItem(item.id) },
                            modifier = Modifier.width(135.dp),
                            aspectRatio = 2f / 3f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 7. TV Shows (if available, poster carousel)
        if (showItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Shows"
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(showItems) { item ->
                        MediaPosterCard(
                            item = item,
                            imageUrl = viewModel.getImageUrl(item, isBackdrop = false),
                            onClick = { onNavigateToItem(item.id) },
                            modifier = Modifier.width(135.dp),
                            aspectRatio = 2f / 3f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 8. Toonami Aftermath Editorial Treatment Section
        if (toonamiChannel != null) {
            item {
                SectionHeader(
                    title = "Toonami Aftermath",
                    actionText = "Watch Live",
                    onActionClick = {
                        val itemDto = viewModel.buildLiveTvItemDto(toonamiChannel)
                        onNavigateToPlayer(itemDto.id)
                    }
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = AppSpacing.md)
                        .clip(RoundedCornerShape(AppRadii.card))
                        .background(AppElevatedSurface)
                        .clickable {
                            val itemDto = viewModel.buildLiveTvItemDto(toonamiChannel)
                            onNavigateToPlayer(itemDto.id)
                        }
                        .testTag("toonami_editorial_banner")
                ) {
                    val context = LocalContext.current
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("https://i.imgur.com/aSjhZK7.png")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Toonami Aftermath",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141923)))
                        }
                    )

                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        AppBackground.copy(alpha = 0.95f),
                                        AppBackground.copy(alpha = 0.75f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Editorial program content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppSpacing.lg),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppLiveRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE NOW",
                                color = AppTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        }

                        Column {
                            Text(
                                text = toonamiChannel.currentProgram?.name ?: "Toonami Broadcast Block",
                                style = AppTypography.sectionTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val programTime = toonamiChannel.currentProgram?.formattedTime ?: "24/7 Retro Animation"
                            Text(
                                text = programTime,
                                style = AppTypography.metadata,
                                color = AppAccent
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = AppAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Watch Live",
                                color = AppAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 9. Collections Section
        if (homeState.collections.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Collections",
                    subtitle = "Curated sagas & film franchises"
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(homeState.collections) { collection ->
                        MediaPosterCard(
                            item = collection,
                            imageUrl = viewModel.getImageUrl(collection, isBackdrop = true),
                            onClick = { onNavigateToItem(collection.id) },
                            modifier = Modifier
                                .width(220.dp)
                                .testTag("collection_${collection.id}"),
                            aspectRatio = 16f / 9f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // 10. Media Libraries Section
        if (homeState.libraries.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Libraries"
                )
                Spacer(modifier = Modifier.height(AppSpacing.headerToContentSpacing))

                LazyRow(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    items(homeState.libraries) { lib ->
                        LibraryCard(
                            name = lib.name,
                            collectionType = lib.collectionType ?: "movies",
                            onClick = { onNavigateToLibrary(lib.id, lib.name) },
                            modifier = Modifier
                                .width(160.dp)
                                .height(100.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sectionSpacing))
            }
        }

        // Loading indicator
        if (homeState.isLoading) {
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
        }
    }
}

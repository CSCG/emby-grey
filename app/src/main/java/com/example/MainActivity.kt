package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.DownloadItemEntity
import com.example.data.model.EmbyItemDto
import com.example.data.model.PlaybackQuality
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.LibrariesScreen
import com.example.ui.screens.LibraryDetailScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.ServerSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EmbyViewModel
import com.example.ui.viewmodel.EmbyViewModelFactory
import com.example.ui.viewmodel.PlayerViewModel

sealed interface Screen {
    data object ServerSetup : Screen
    data object Main : Screen
    data class LibraryDetail(val id: String, val name: String) : Screen
    data class ItemDetail(val itemId: String) : Screen
    data class Player(
        val item: EmbyItemDto,
        val quality: PlaybackQuality,
        val localFilePath: String? = null,
        val startFromBeginning: Boolean = false
    ) : Screen
    data object Search : Screen
}

enum class MainTab {
    HOME, LIVE_TV, LIBRARIES, DOWNLOADS, SETTINGS
}

class MainActivity : ComponentActivity() {
    var activeScreen by mutableStateOf<Screen>(Screen.ServerSetup)
        private set
    var isInPipMode by mutableStateOf(false)
        private set

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (activeScreen is Screen.Player) {
            enterPipMode()
        }
    }

    fun enterPipMode(aspectRatio: Rational = Rational(16, 9)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val paramsBuilder = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    paramsBuilder.setAutoEnterEnabled(true)
                    paramsBuilder.setSeamlessResizeEnabled(true)
                }
                enterPictureInPictureMode(paramsBuilder.build())
            } catch (_: Exception) {
                try {
                    @Suppress("DEPRECATION")
                    enterPictureInPictureMode()
                } catch (_: Exception) {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as EmbyApplication

        setContent {
            MyApplicationTheme {
                val embyViewModel: EmbyViewModel = viewModel(
                    factory = EmbyViewModelFactory(
                        app.repository,
                        app.downloadManager,
                        app.serverPreferences
                    )
                )

                val playerViewModel: PlayerViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return PlayerViewModel(app.repository) as T
                        }
                    }
                )

                val homeState by embyViewModel.homeState.collectAsState()
                val isConnected = homeState.connection != null

                var currentScreen by remember(isConnected) {
                    mutableStateOf<Screen>(if (isConnected) Screen.Main else Screen.ServerSetup)
                }
                var screenBackStack by remember { mutableStateOf(listOf<Screen>()) }

                fun navigateTo(newScreen: Screen) {
                    screenBackStack = screenBackStack + currentScreen
                    currentScreen = newScreen
                }

                fun navigateBack() {
                    if (screenBackStack.isNotEmpty()) {
                        currentScreen = screenBackStack.last()
                        screenBackStack = screenBackStack.dropLast(1)
                    } else {
                        currentScreen = Screen.Main
                    }
                }

                LaunchedEffect(currentScreen) {
                    activeScreen = currentScreen
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (currentScreen is Screen.Player) {
                            setPictureInPictureParams(
                                PictureInPictureParams.Builder()
                                    .setAutoEnterEnabled(true)
                                    .setAspectRatio(Rational(16, 9))
                                    .setSeamlessResizeEnabled(true)
                                    .build()
                            )
                        } else {
                            setPictureInPictureParams(
                                PictureInPictureParams.Builder()
                                    .setAutoEnterEnabled(false)
                                    .build()
                            )
                        }
                    }
                }

                var currentTab by remember { mutableStateOf(MainTab.HOME) }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        androidx.compose.animation.core.tween<Float>(durationMillis = 200).let { spec ->
                            fadeIn(animationSpec = spec) togetherWith fadeOut(animationSpec = androidx.compose.animation.core.tween(180))
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.ServerSetup -> {
                            ServerSetupScreen(
                                viewModel = embyViewModel,
                                onConnected = {
                                    currentScreen = Screen.Main
                                }
                            )
                        }

                        is Screen.Main -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = com.example.ui.theme.AppBackground,
                                bottomBar = {
                                    NavigationBar(
                                        modifier = Modifier
                                            .windowInsetsPadding(WindowInsets.navigationBars)
                                            .testTag("bottom_nav_bar"),
                                        containerColor = com.example.ui.theme.AppSurface,
                                        tonalElevation = 0.dp
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.HOME,
                                            onClick = { currentTab = MainTab.HOME },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                                    contentDescription = "Home"
                                                )
                                            },
                                            label = { Text("Home", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.HOME) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = com.example.ui.theme.AppAccent,
                                                selectedTextColor = com.example.ui.theme.AppAccent,
                                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                                unselectedIconColor = com.example.ui.theme.AppTextSecondary,
                                                unselectedTextColor = com.example.ui.theme.AppTextTertiary
                                            ),
                                            modifier = Modifier.testTag("nav_tab_home")
                                        )

                                        NavigationBarItem(
                                            selected = currentTab == MainTab.LIVE_TV,
                                            onClick = { currentTab = MainTab.LIVE_TV },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == MainTab.LIVE_TV) Icons.Filled.LiveTv else Icons.Outlined.LiveTv,
                                                    contentDescription = "Live"
                                                )
                                            },
                                            label = { Text("Live", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.LIVE_TV) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = com.example.ui.theme.AppAccent,
                                                selectedTextColor = com.example.ui.theme.AppAccent,
                                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                                unselectedIconColor = com.example.ui.theme.AppTextSecondary,
                                                unselectedTextColor = com.example.ui.theme.AppTextTertiary
                                            ),
                                            modifier = Modifier.testTag("nav_tab_live_tv")
                                        )

                                        NavigationBarItem(
                                            selected = currentTab == MainTab.LIBRARIES,
                                            onClick = { currentTab = MainTab.LIBRARIES },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == MainTab.LIBRARIES) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                                    contentDescription = "Library"
                                                )
                                            },
                                            label = { Text("Library", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.LIBRARIES) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = com.example.ui.theme.AppAccent,
                                                selectedTextColor = com.example.ui.theme.AppAccent,
                                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                                unselectedIconColor = com.example.ui.theme.AppTextSecondary,
                                                unselectedTextColor = com.example.ui.theme.AppTextTertiary
                                            ),
                                            modifier = Modifier.testTag("nav_tab_libraries")
                                        )

                                        NavigationBarItem(
                                            selected = currentTab == MainTab.DOWNLOADS,
                                            onClick = { currentTab = MainTab.DOWNLOADS },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == MainTab.DOWNLOADS) Icons.Filled.CloudDone else Icons.Outlined.CloudDone,
                                                    contentDescription = "Downloads"
                                                )
                                            },
                                            label = { Text("Downloads", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.DOWNLOADS) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = com.example.ui.theme.AppAccent,
                                                selectedTextColor = com.example.ui.theme.AppAccent,
                                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                                unselectedIconColor = com.example.ui.theme.AppTextSecondary,
                                                unselectedTextColor = com.example.ui.theme.AppTextTertiary
                                            ),
                                            modifier = Modifier.testTag("nav_tab_downloads")
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (currentTab) {
                                        MainTab.HOME -> {
                                            HomeScreen(
                                                viewModel = embyViewModel,
                                                onNavigateToItem = { id ->
                                                    navigateTo(Screen.ItemDetail(id))
                                                },
                                                onNavigateToLibrary = { id, name ->
                                                    navigateTo(Screen.LibraryDetail(id, name))
                                                },
                                                onNavigateToPlayer = { id ->
                                                    val liveCh = embyViewModel.liveTvChannels.value.firstOrNull { it.id == id }
                                                    if (liveCh != null) {
                                                        val itemDto = embyViewModel.buildLiveTvItemDto(liveCh)
                                                        navigateTo(Screen.Player(itemDto, PlaybackQuality.DIRECT_PLAY))
                                                    } else {
                                                        val item = (homeState.continueWatching + homeState.latestItems + homeState.collections).firstOrNull { it.id == id }
                                                        if (item != null) {
                                                            if (item.type.equals("Series", ignoreCase = true)) {
                                                                val eps = embyViewModel.episodes.value.filter { it.seriesId == item.id }
                                                                val targetEp = eps.firstOrNull { it.resumePositionTicks > 0 && !it.isPlayed }
                                                                    ?: eps.firstOrNull { !it.isPlayed }
                                                                    ?: eps.firstOrNull()
                                                                if (targetEp != null) {
                                                                    navigateTo(Screen.Player(targetEp, PlaybackQuality.DIRECT_PLAY))
                                                                } else {
                                                                    navigateTo(Screen.Player(item, PlaybackQuality.DIRECT_PLAY))
                                                                }
                                                            } else {
                                                                navigateTo(Screen.Player(item, PlaybackQuality.DIRECT_PLAY))
                                                            }
                                                        } else {
                                                            navigateTo(Screen.ItemDetail(id))
                                                        }
                                                    }
                                                },
                                                onNavigateToDownloads = {
                                                    currentTab = MainTab.DOWNLOADS
                                                },
                                                onNavigateToSettings = {
                                                    currentTab = MainTab.SETTINGS
                                                },
                                                onNavigateToSearch = {
                                                    navigateTo(Screen.Search)
                                                },
                                                onNavigateToLiveTv = {
                                                    currentTab = MainTab.LIVE_TV
                                                }
                                            )
                                        }

                                        MainTab.LIVE_TV -> {
                                            LiveTvScreen(
                                                viewModel = embyViewModel,
                                                onPlayLiveChannel = { channelItem ->
                                                    navigateTo(
                                                        Screen.Player(
                                                            item = channelItem,
                                                            quality = PlaybackQuality.DIRECT_PLAY
                                                        )
                                                    )
                                                }
                                            )
                                        }

                                        MainTab.LIBRARIES -> {
                                            LibrariesScreen(
                                                viewModel = embyViewModel,
                                                onNavigateToLibrary = { id, name ->
                                                    navigateTo(Screen.LibraryDetail(id, name))
                                                },
                                                onNavigateToSearch = {
                                                    navigateTo(Screen.Search)
                                                }
                                            )
                                        }

                                        MainTab.DOWNLOADS -> {
                                            DownloadsScreen(
                                                viewModel = embyViewModel,
                                                onPlayOfflineMedia = { download ->
                                                    val offlineItem = EmbyItemDto(
                                                        id = download.id,
                                                        name = download.title,
                                                        type = download.mediaType,
                                                        seriesName = download.seriesName,
                                                        indexNumber = download.episodeNumber,
                                                        parentIndexNumber = download.seasonNumber,
                                                        runTimeTicks = download.durationMs * 10_000L
                                                    )
                                                    navigateTo(
                                                        Screen.Player(
                                                            item = offlineItem,
                                                            quality = PlaybackQuality.DIRECT_PLAY,
                                                            localFilePath = download.localFilePath
                                                        )
                                                    )
                                                }
                                            )
                                        }

                                        MainTab.SETTINGS -> {
                                            SettingsScreen(
                                                viewModel = embyViewModel,
                                                onDisconnect = {
                                                    currentScreen = Screen.ServerSetup
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is Screen.LibraryDetail -> {
                            LibraryDetailScreen(
                                libraryId = screen.id,
                                libraryName = screen.name,
                                viewModel = embyViewModel,
                                onNavigateBack = { currentScreen = Screen.Main },
                                onNavigateToItem = { id -> navigateTo(Screen.ItemDetail(id)) }
                            )
                        }

                        is Screen.ItemDetail -> {
                            ItemDetailScreen(
                                itemId = screen.itemId,
                                viewModel = embyViewModel,
                                onNavigateBack = { navigateBack() },
                                onNavigateToItem = { id -> navigateTo(Screen.ItemDetail(id)) },
                                onPlayMedia = { id, quality, startFromBeginning ->
                                    val episode = embyViewModel.episodes.value.firstOrNull { it.id == id }
                                    val collectionItem = embyViewModel.collectionItems.value.firstOrNull { it.id == id }
                                    val selected = embyViewModel.selectedItem.value
                                    val targetItem = episode
                                        ?: collectionItem
                                        ?: if (selected?.id == id && !selected.type.equals("Series", ignoreCase = true)) selected
                                        else null
                                        ?: (homeState.continueWatching + homeState.latestItems + homeState.collections).firstOrNull { it.id == id && !it.type.equals("Series", ignoreCase = true) }
                                        ?: run {
                                            val eps = embyViewModel.episodes.value
                                            eps.firstOrNull { it.resumePositionTicks > 0 && !it.isPlayed }
                                                ?: eps.firstOrNull { !it.isPlayed }
                                                ?: eps.firstOrNull()
                                        }
                                        ?: selected
                                    if (targetItem != null) {
                                        navigateTo(
                                            Screen.Player(
                                                item = targetItem,
                                                quality = quality,
                                                startFromBeginning = startFromBeginning
                                            )
                                        )
                                    }
                                }
                            )
                        }

                        is Screen.Search -> {
                            SearchScreen(
                                viewModel = embyViewModel,
                                onNavigateBack = { navigateBack() },
                                onNavigateToItem = { id -> navigateTo(Screen.ItemDetail(id)) }
                            )
                        }

                        is Screen.Player -> {
                            PlayerScreen(
                                item = screen.item,
                                initialQuality = screen.quality,
                                localFilePath = screen.localFilePath,
                                startFromBeginning = screen.startFromBeginning,
                                playerViewModel = playerViewModel,
                                onNavigateBack = {
                                    embyViewModel.loadHomeData()
                                    navigateBack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.BehindLiveWindowException
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.data.model.ChapterInfoDto
import com.example.data.model.EmbyItemDto
import com.example.data.model.PlaybackQuality
import com.example.data.repository.EmbyRepository
import com.example.data.repository.LiquidTvCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class SubtitleTrack(
    val id: String,
    val name: String,
    val language: String,
    val isExternal: Boolean = false,
    val trackGroupIndex: Int? = null,
    val trackIndex: Int? = null
)

data class AudioTrack(
    val id: String,
    val name: String,
    val language: String,
    val trackGroupIndex: Int,
    val trackIndex: Int
)

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val quality: PlaybackQuality = PlaybackQuality.DIRECT_PLAY,
    val resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    val playbackSpeed: Float = 1.0f,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val selectedSubtitleId: String? = null,
    val audioTracks: List<AudioTrack> = emptyList(),
    val selectedAudioTrackId: String? = null,
    val areControlsVisible: Boolean = true,
    val isControlsLocked: Boolean = false,
    val isOfflinePlayback: Boolean = false,
    val resumedFromMs: Long? = null,
    val showResumeBanner: Boolean = false,
    val errorMessage: String? = null,
    // Pro features
    val chapters: List<ChapterInfoDto> = emptyList(),
    val currentChapter: ChapterInfoDto? = null,
    val canSkipIntro: Boolean = false,
    val introEndMs: Long? = null,
    val nextEpisode: EmbyItemDto? = null,
    val showUpNextOverlay: Boolean = false,
    val upNextCountdownSeconds: Int = 10,
    val sleepTimerMinutes: Int? = null,
    val sleepTimerRemainingSeconds: Int? = null,
    val isInPipMode: Boolean = false,
    val isLiveStream: Boolean = false,
    val currentToonamiServer: String? = null,
    val availableToonamiServers: List<String> = emptyList(),
    val isLiquidTv: Boolean = false
)

class PlayerViewModel(
    private val repository: EmbyRepository
) : ViewModel() {

    val toonamiServers = listOf("Auto", "n6", "n4", "n7", "n3", "n5", "n8", "n1", "n2")
    var selectedToonamiServer: String = "Auto"
        private set

    fun setToonamiServer(server: String) {
        selectedToonamiServer = server
        _uiState.value = _uiState.value.copy(currentToonamiServer = server, isBuffering = true)
        loadMedia(initialPositionMs = null, startFromBeginning = true, includeSubtitles = false)
    }

    fun shuffleLiquidTv(context: Context) {
        val randomEp = LiquidTvCatalog.getRandomEpisode()
        currentItem = currentItem?.copy(
            name = randomEp.fullTitle,
            overview = "Featuring: ${randomEp.featuredSegments}",
            path = randomEp.streamUrl
        )
        _uiState.value = _uiState.value.copy(
            isBuffering = true,
            currentPositionMs = 0L
        )
        loadDirectStream(randomEp.streamUrl)
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var progressTrackingJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var upNextCountdownJob: Job? = null
    private var currentItem: EmbyItemDto? = null
    private var localFilePath: String? = null
    private var hasUpNextBeenDismissed: Boolean = false
    private var hasAttachedSubtitles: Boolean = false
    private var fallbackAttempt: Int = 0

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun initializePlayer(
        context: Context,
        item: EmbyItemDto,
        localFile: String? = null,
        initialQuality: PlaybackQuality = repository.preferences.defaultQuality,
        startFromBeginning: Boolean = false
    ) {
        val isLiquidTv = item.id == "free_liquid_tv" || item.name.contains("Liquid Television", ignoreCase = true)
        val initialItem = if (isLiquidTv) {
            val randomEp = LiquidTvCatalog.getRandomEpisode()
            item.copy(
                name = randomEp.fullTitle,
                overview = "Featuring: ${randomEp.featuredSegments}",
                path = randomEp.streamUrl
            )
        } else item

        currentItem = initialItem
        localFilePath = localFile
        val isOffline = localFile != null && File(localFile).exists()
        val chaps = item.chapters ?: emptyList()
        hasUpNextBeenDismissed = false
        val isLive = item.type == "LiveTvChannel" || item.type == "TvChannel"

        _uiState.value = _uiState.value.copy(
            quality = if (isOffline) PlaybackQuality.DIRECT_PLAY else initialQuality,
            isOfflinePlayback = isOffline,
            isLiveStream = isLive,
            isLiquidTv = isLiquidTv,
            isBuffering = true,
            showResumeBanner = false,
            resumedFromMs = null,
            chapters = chaps,
            currentChapter = chaps.firstOrNull(),
            canSkipIntro = false,
            introEndMs = null,
            showUpNextOverlay = false,
            nextEpisode = null
        )

        // Preload next episode if this item is a TV episode
        if (item.type.equals("Episode", ignoreCase = true) && !item.seriesId.isNullOrBlank()) {
            viewModelScope.launch {
                val next = repository.getNextEpisode(
                    seriesId = item.seriesId,
                    currentSeasonNum = item.parentIndexNumber,
                    currentEpisodeNum = item.indexNumber
                )
                _uiState.value = _uiState.value.copy(nextEpisode = next)
            }
        }

        // Traffic cameras use dedicated live CCTV monitor feed without video player
        if (item.collectionType == "TrafficCam") {
            _uiState.value = _uiState.value.copy(isBuffering = false)
            return
        }

        // Build hardware-accelerated renderers factory
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        // Resilient HTTP Data Source with browser User-Agent, redirects and extended timeouts
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 ExoPlayer")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(20_000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val player = ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()

        exoPlayer = player

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                reportProgress(isCompleted = false)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _uiState.value = _uiState.value.copy(isBuffering = true)
                    }
                    Player.STATE_READY -> {
                        _uiState.value = _uiState.value.copy(
                            isBuffering = false,
                            durationMs = player.duration.coerceAtLeast(0L),
                            errorMessage = null
                        )
                        updateTracks(player)
                    }
                    Player.STATE_ENDED -> {
                        _uiState.value = _uiState.value.copy(isPlaying = false)
                        reportProgress(isCompleted = true)
                        if (_uiState.value.isLiquidTv) {
                            shuffleLiquidTv(context)
                        } else if (_uiState.value.nextEpisode != null && !hasUpNextBeenDismissed) {
                            triggerUpNextCountdown(context)
                        }
                    }
                    Player.STATE_IDLE -> Unit
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateTracks(player)
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PlayerViewModel", "ExoPlayer playback error: ${error.errorCodeName}", error)

                // Level 0: If live window shifted, recover immediately by seeking to live edge
                val cause = error.cause
                if (cause is BehindLiveWindowException || _uiState.value.isLiveStream && error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    Log.w("PlayerViewModel", "Live window shifted. Re-seeking to live default position.")
                    player.seekToDefaultPosition()
                    player.prepare()
                    player.play()
                    return
                }

                // Level 1: If playback failed due to a sidecar subtitle loading error (e.g. 404), gracefully retry main video without subtitles
                if (hasAttachedSubtitles) {
                    hasAttachedSubtitles = false
                    Log.w("PlayerViewModel", "Retrying media stream without sidecar subtitles after error")
                    loadMedia(
                        initialPositionMs = if (_uiState.value.isLiveStream) null else player.currentPosition.coerceAtLeast(0L),
                        startFromBeginning = false,
                        includeSubtitles = false
                    )
                    return
                }

                // Level 2: Automatic failover to verified resilient stream for this channel/category
                if (fallbackAttempt < 2) {
                    fallbackAttempt++
                    val fallbackUrl = getResilientFallbackStream(currentItem)
                    Log.w("PlayerViewModel", "Stream error encountered. Automatically failing over to resilient feed (attempt $fallbackAttempt): $fallbackUrl")
                    loadDirectStream(fallbackUrl)
                    return
                }

                _uiState.value = _uiState.value.copy(
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = "Stream momentarily unavailable. Tap Retry to reconnect."
                )
            }
        })

        loadMedia(initialPositionMs = null, startFromBeginning = startFromBeginning, includeSubtitles = true)
        startProgressTracking(context)

        // Report start to Emby
        viewModelScope.launch {
            repository.reportPlaybackStart(
                itemId = item.id,
                mediaSourceId = item.mediaSources?.firstOrNull()?.id,
                playMethod = if (isOffline || initialQuality == PlaybackQuality.DIRECT_PLAY) "DirectPlay" else "Transcode"
            )
        }
    }

    private suspend fun resolveLiveStreamUrl(rawUrl: String): String = withContext(Dispatchers.IO) {
        if (rawUrl.contains("toonamiaftermath.com", ignoreCase = true)) {
            // If the user selected a specific server node (not "Auto"), query the streamUrl API for that specific node
            if (selectedToonamiServer != "Auto") {
                val channel = when {
                    rawUrl.contains("pst", ignoreCase = true) || rawUrl.contains("west", ignoreCase = true) -> "pst"
                    rawUrl.contains("movie", ignoreCase = true) -> "movies"
                    rawUrl.contains("snick", ignoreCase = true) -> "snick-est"
                    else -> "est"
                }
                try {
                    val queryUrl = "https://api.toonamiaftermath.com/streamUrl?channelName=$channel&useHttps=true&overrideServer=$selectedToonamiServer"
                    val connection = (URL(queryUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 3500
                        readTimeout = 3500
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36")
                    }
                    if (connection.responseCode in 200..299) {
                        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                        if (responseBody.startsWith("http")) {
                            Log.d("PlayerViewModel", "Resolved Toonami override server stream: $responseBody")
                            _uiState.value = _uiState.value.copy(
                                currentToonamiServer = selectedToonamiServer,
                                availableToonamiServers = toonamiServers
                            )
                            return@withContext responseBody
                        }
                    }
                } catch (e: Exception) {
                    Log.w("PlayerViewModel", "Error fetching Toonami stream for server $selectedToonamiServer: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    currentToonamiServer = "Direct",
                    availableToonamiServers = toonamiServers
                )
            }
        }
        rawUrl
    }

    private fun loadMedia(
        initialPositionMs: Long? = null,
        startFromBeginning: Boolean = false,
        includeSubtitles: Boolean = true
    ) {
        val player = exoPlayer ?: return
        val item = currentItem ?: return

        fallbackAttempt = 0
        val isLive = _uiState.value.isLiveStream
        val isLiquidTv = _uiState.value.isLiquidTv
        val rawUriString = when {
            localFilePath != null && File(localFilePath!!).exists() -> localFilePath!!
            isLiquidTv && !item.path.isNullOrBlank() -> item.path
            isLiquidTv -> LiquidTvCatalog.getRandomEpisode().streamUrl
            isLive && !item.path.isNullOrBlank() && item.path.startsWith("http") -> item.path
            isLive && !item.overview.isNullOrBlank() && item.overview.startsWith("http") -> item.overview
            isLive -> repository.buildLiveTvStreamUrl(item.id)
            else -> repository.buildStreamUrl(item.id, quality = _uiState.value.quality)
        }

        viewModelScope.launch {
            val resolvedStreamUrl = if (isLive && !isLiquidTv) resolveLiveStreamUrl(rawUriString) else rawUriString
            val uri = if (localFilePath != null && File(localFilePath!!).exists()) {
                Uri.fromFile(File(localFilePath!!))
            } else {
                Uri.parse(resolvedStreamUrl)
            }

            val mediaItemBuilder = MediaItem.Builder().setUri(uri)
            val isHls = resolvedStreamUrl.contains(".m3u8", ignoreCase = true)
            val isMp4 = resolvedStreamUrl.contains(".mp4", ignoreCase = true)

            if (isHls) {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            } else if (isMp4) {
                mediaItemBuilder.setMimeType(MimeTypes.VIDEO_MP4)
            }

            if (isLive && !isLiquidTv) {
                mediaItemBuilder.setLiveConfiguration(
                    MediaItem.LiveConfiguration.Builder()
                        .setMaxPlaybackSpeed(1.05f)
                        .setMinPlaybackSpeed(0.95f)
                        .setTargetOffsetMs(30_000L)
                        .build()
                )
            }

            // Add subtitle streams if available and requested
            hasAttachedSubtitles = false
            if (includeSubtitles && !isLive) {
                val subtitleConfigs = mutableListOf<MediaItem.SubtitleConfiguration>()
                item.mediaSources?.firstOrNull()?.mediaStreams?.filter { it.type.equals("Subtitle", ignoreCase = true) }?.forEach { sub ->
                    val subUrl = repository.buildSubtitleUrl(item.id, item.mediaSources.firstOrNull()?.id, sub.index)
                    if (!subUrl.isNullOrBlank()) {
                        val mime = when {
                            sub.codec?.contains("vtt", ignoreCase = true) == true -> MimeTypes.TEXT_VTT
                            sub.codec?.contains("srt", ignoreCase = true) == true -> MimeTypes.APPLICATION_SUBRIP
                            else -> MimeTypes.TEXT_VTT
                        }
                        val config = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subUrl))
                            .setMimeType(mime)
                            .setLanguage(sub.language ?: "und")
                            .setLabel(sub.displayTitle ?: "Subtitle ${sub.index}")
                            .setSelectionFlags(if (sub.isDefault == true) C.SELECTION_FLAG_DEFAULT else 0)
                            .build()
                        subtitleConfigs.add(config)
                    }
                }
                if (subtitleConfigs.isNotEmpty()) {
                    mediaItemBuilder.setSubtitleConfigurations(subtitleConfigs)
                    hasAttachedSubtitles = true
                }
            }

            val mediaItem = mediaItemBuilder.build()
            if (isHls) {
                val httpFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(20_000)
                    .setReadTimeoutMs(20_000)
                val hlsSource = HlsMediaSource.Factory(httpFactory)
                    .setAllowChunklessPreparation(false)
                    .createMediaSource(mediaItem)
                player.setMediaSource(hlsSource)
            } else {
                player.setMediaItem(mediaItem)
            }

            player.prepare()

            // For live streams, never seekTo(0L) - always seek to default live position
            if (isLive && !isLiquidTv) {
                player.seekToDefaultPosition()
            } else if (!startFromBeginning) {
                val resumeMs = initialPositionMs ?: repository.getResumePosition(item.id, item)
                if (resumeMs != null && resumeMs > 5000L) {
                    player.seekTo(resumeMs)
                    _uiState.value = _uiState.value.copy(
                        resumedFromMs = resumeMs,
                        showResumeBanner = true
                    )
                }
            } else {
                player.seekTo(0L)
            }
            player.play()
        }
    }

    private fun loadDirectStream(streamUrl: String) {
        val player = exoPlayer ?: return
        viewModelScope.launch {
            val resolved = if (_uiState.value.isLiveStream && !_uiState.value.isLiquidTv) resolveLiveStreamUrl(streamUrl) else streamUrl
            val uri = Uri.parse(resolved)
            val builder = MediaItem.Builder().setUri(uri)
            val isHls = resolved.contains(".m3u8", ignoreCase = true)
            val isMp4 = resolved.contains(".mp4", ignoreCase = true)

            if (isHls) {
                builder.setMimeType(MimeTypes.APPLICATION_M3U8)
                if (_uiState.value.isLiveStream && !_uiState.value.isLiquidTv) {
                    builder.setLiveConfiguration(
                        MediaItem.LiveConfiguration.Builder()
                            .setMaxPlaybackSpeed(1.05f)
                            .setMinPlaybackSpeed(0.95f)
                            .setTargetOffsetMs(30_000L)
                            .build()
                    )
                }
            } else if (isMp4) {
                builder.setMimeType(MimeTypes.VIDEO_MP4)
            }
            val mediaItem = builder.build()
            if (isHls) {
                val httpFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(20_000)
                    .setReadTimeoutMs(20_000)
                val hlsSource = HlsMediaSource.Factory(httpFactory)
                    .setAllowChunklessPreparation(false)
                    .createMediaSource(mediaItem)
                player.setMediaSource(hlsSource)
            } else {
                player.setMediaItem(mediaItem)
            }

            player.prepare()
            if (_uiState.value.isLiveStream && !_uiState.value.isLiquidTv) {
                player.seekToDefaultPosition()
            }
            player.play()
        }
    }

    private fun getResilientFallbackStream(item: EmbyItemDto?): String {
        val cat = item?.seriesName ?: ""
        val name = item?.name ?: ""
        return when {
            name.contains("Toonami", ignoreCase = true) || cat.contains("Toonami", ignoreCase = true) ->
                "http://api.toonamiaftermath.com:3000/est/playlist.m3u8"
            cat.contains("Cartoons", ignoreCase = true) || name.contains("Cartoons", ignoreCase = true) ->
                "https://daiconnect.com/live/hls/tvup/rk-cartoonclassics/578f4b7eb725168349ec0af81b21d388/index.m3u8"
            cat.contains("Anime", ignoreCase = true) || name.contains("Anime", ignoreCase = true) ->
                "https://amg18481-amg18481c1-amgplt0352.playout.now3.amagi.tv/playlist/amg18481-amg18481c1-amgplt0352/playlist.m3u8"
            cat.contains("Kids", ignoreCase = true) ->
                "https://3abn.bozztv.com/3abn2/Kids_live/smil:Kids_live.smil/playlist.m3u8"
            cat.contains("News", ignoreCase = true) || cat.contains("Weather", ignoreCase = true) ->
                "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8"
            cat.contains("Nature", ignoreCase = true) ->
                "https://playertest.longtailvideo.com/adaptive/oceans/oceans.m3u8"
            cat.contains("Movie", ignoreCase = true) || cat.contains("Cinema", ignoreCase = true) ->
                "https://30a-tv.com/feeds/pzaz/30atvmovies.m3u8"
            else ->
                "http://api.toonamiaftermath.com:3000/est/playlist.m3u8"
        }
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isBuffering = true
        )
        fallbackAttempt = 0
        loadMedia(initialPositionMs = 0L, startFromBeginning = true, includeSubtitles = false)
    }

    private fun updateTracks(player: ExoPlayer) {
        val subs = mutableListOf<SubtitleTrack>()
        val audio = mutableListOf<AudioTrack>()

        val tracks = player.currentTracks
        var subIndex = 0

        for (groupIndex in 0 until tracks.groups.size) {
            val group = tracks.groups[groupIndex]
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (trackIndex in 0 until group.length) {
                    val format = group.getTrackFormat(trackIndex)
                    val label = format.label ?: format.language ?: "Subtitle ${subIndex + 1}"
                    subs.add(
                        SubtitleTrack(
                            id = "sub_${groupIndex}_$trackIndex",
                            name = label,
                            language = format.language ?: "und",
                            isExternal = false,
                            trackGroupIndex = groupIndex,
                            trackIndex = trackIndex
                        )
                    )
                    subIndex++
                }
            } else if (group.type == C.TRACK_TYPE_AUDIO) {
                for (trackIndex in 0 until group.length) {
                    val format = group.getTrackFormat(trackIndex)
                    val label = format.label ?: format.language ?: "Audio ${audio.size + 1}"
                    audio.add(
                        AudioTrack(
                            id = "audio_${groupIndex}_$trackIndex",
                            name = label,
                            language = format.language ?: "und",
                            trackGroupIndex = groupIndex,
                            trackIndex = trackIndex
                        )
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            subtitles = subs,
            audioTracks = audio
        )
    }

    private fun startProgressTracking(context: Context) {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (isActive) {
                val player = exoPlayer
                if (player != null) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    val buf = player.bufferedPosition.coerceAtLeast(0L)

                    // Chapter and skip intro checking
                    val chaps = _uiState.value.chapters
                    val curChap = chaps.lastOrNull { pos >= it.startPositionMs }
                    var canSkip = false
                    var introEnd: Long? = null
                    if (curChap != null && (curChap.name?.contains("Intro", ignoreCase = true) == true || curChap.name?.contains("Opening", ignoreCase = true) == true)) {
                        val idx = chaps.indexOf(curChap)
                        if (idx in 0 until chaps.size - 1) {
                            introEnd = chaps[idx + 1].startPositionMs
                            canSkip = true
                        }
                    }

                    // Check for Up Next overlay trigger (within 25s of end)
                    val nextEp = _uiState.value.nextEpisode
                    val shouldShowUpNext = nextEp != null && dur > 60_000L && (dur - pos) <= 25_000L && !hasUpNextBeenDismissed && !_uiState.value.showUpNextOverlay

                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        bufferedPositionMs = buf,
                        currentChapter = curChap,
                        canSkipIntro = canSkip,
                        introEndMs = introEnd
                    )

                    if (shouldShowUpNext) {
                        triggerUpNextCountdown(context)
                    }

                    // Report to Emby every 5 seconds while playing
                    if (player.isPlaying && pos > 0 && dur > 0 && (pos / 1000) % 5 == 0L) {
                        reportProgress(isCompleted = false)
                    }
                }
                delay(500)
            }
        }
    }

    fun skipIntro() {
        val target = _uiState.value.introEndMs ?: return
        seekTo(target)
    }

    fun seekToChapter(chapter: ChapterInfoDto) {
        seekTo(chapter.startPositionMs)
    }

    fun triggerUpNextCountdown(context: Context) {
        if (_uiState.value.showUpNextOverlay) return
        _uiState.value = _uiState.value.copy(showUpNextOverlay = true, upNextCountdownSeconds = 10)
        upNextCountdownJob?.cancel()
        upNextCountdownJob = viewModelScope.launch {
            for (sec in 10 downTo 1) {
                _uiState.value = _uiState.value.copy(upNextCountdownSeconds = sec)
                delay(1000)
            }
            if (_uiState.value.showUpNextOverlay) {
                playNextEpisode(context)
            }
        }
    }

    fun dismissUpNext() {
        hasUpNextBeenDismissed = true
        upNextCountdownJob?.cancel()
        _uiState.value = _uiState.value.copy(showUpNextOverlay = false)
    }

    fun playNextEpisode(context: Context) {
        val next = _uiState.value.nextEpisode ?: return
        upNextCountdownJob?.cancel()
        _uiState.value = _uiState.value.copy(showUpNextOverlay = false)
        release()
        initializePlayer(
            context = context,
            item = next,
            localFile = null,
            initialQuality = _uiState.value.quality,
            startFromBeginning = true
        )
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null) {
            _uiState.value = _uiState.value.copy(sleepTimerMinutes = null, sleepTimerRemainingSeconds = null)
            return
        }
        val totalSeconds = minutes * 60
        _uiState.value = _uiState.value.copy(sleepTimerMinutes = minutes, sleepTimerRemainingSeconds = totalSeconds)
        sleepTimerJob = viewModelScope.launch {
            for (sec in totalSeconds downTo 1) {
                _uiState.value = _uiState.value.copy(sleepTimerRemainingSeconds = sec)
                delay(1000)
            }
            exoPlayer?.pause()
            _uiState.value = _uiState.value.copy(
                isPlaying = false,
                sleepTimerMinutes = null,
                sleepTimerRemainingSeconds = null
            )
            reportProgress(isCompleted = false)
        }
    }

    fun setInPipMode(inPip: Boolean) {
        _uiState.value = _uiState.value.copy(
            isInPipMode = inPip,
            areControlsVisible = !inPip
        )
    }

    private fun reportProgress(isCompleted: Boolean) {
        if (_uiState.value.isLiveStream) return
        val item = currentItem ?: return
        val pos = exoPlayer?.currentPosition ?: 0L
        val dur = exoPlayer?.duration ?: 0L
        val isPaused = !(exoPlayer?.isPlaying ?: false)

        viewModelScope.launch {
            repository.reportPlaybackProgress(
                itemId = item.id,
                positionMs = if (isCompleted) dur else pos,
                durationMs = dur,
                isPaused = isPaused,
                playMethod = if (_uiState.value.quality == PlaybackQuality.DIRECT_PLAY) "DirectPlay" else "Transcode",
                mediaSourceId = item.mediaSources?.firstOrNull()?.id
            )
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            reportProgress(isCompleted = false)
        } else {
            player.play()
            reportProgress(isCompleted = false)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
        reportProgress(isCompleted = false)
    }

    fun seekBack10() {
        val player = exoPlayer ?: return
        val target = (player.currentPosition - 10_000).coerceAtLeast(0L)
        player.seekTo(target)
        reportProgress(isCompleted = false)
    }

    fun seekForward10() {
        val player = exoPlayer ?: return
        val target = (player.currentPosition + 10_000).coerceAtMost(player.duration)
        player.seekTo(target)
        reportProgress(isCompleted = false)
    }

    fun restartFromBeginning() {
        val player = exoPlayer ?: return
        val item = currentItem
        player.seekTo(0L)
        _uiState.value = _uiState.value.copy(
            showResumeBanner = false,
            resumedFromMs = null,
            currentPositionMs = 0L
        )
        if (item != null) {
            viewModelScope.launch {
                repository.clearResumePosition(item.id)
            }
        }
        reportProgress(isCompleted = false)
    }

    fun dismissResumeBanner() {
        _uiState.value = _uiState.value.copy(showResumeBanner = false)
    }

    fun setQuality(quality: PlaybackQuality) {
        if (_uiState.value.isOfflinePlayback || _uiState.value.quality == quality) return
        val player = exoPlayer ?: return
        val currentPos = player.currentPosition

        _uiState.value = _uiState.value.copy(quality = quality, isBuffering = true)
        loadMedia(initialPositionMs = currentPos)
    }

    fun selectSubtitle(track: SubtitleTrack?) {
        val player = exoPlayer ?: return
        val params = player.trackSelectionParameters.buildUpon()

        if (track == null) {
            // Disable subtitles
            params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            player.trackSelectionParameters = params.build()
            _uiState.value = _uiState.value.copy(selectedSubtitleId = null)
        } else if (track.trackGroupIndex != null && track.trackIndex != null) {
            params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            val group = player.currentTracks.groups[track.trackGroupIndex]
            params.setOverrideForType(
                TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex)
            )
            player.trackSelectionParameters = params.build()
            _uiState.value = _uiState.value.copy(selectedSubtitleId = track.id)
        }
    }

    fun selectAudioTrack(track: AudioTrack) {
        val player = exoPlayer ?: return
        val params = player.trackSelectionParameters.buildUpon()
        val group = player.currentTracks.groups[track.trackGroupIndex]
        params.setOverrideForType(
            TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex)
        )
        player.trackSelectionParameters = params.build()
        _uiState.value = _uiState.value.copy(selectedAudioTrackId = track.id)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun cycleAspectRatio() {
        val nextMode = when (_uiState.value.resizeMode) {
            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        _uiState.value = _uiState.value.copy(resizeMode = nextMode)
    }

    fun toggleControlsVisibility() {
        if (!_uiState.value.isControlsLocked) {
            _uiState.value = _uiState.value.copy(areControlsVisible = !_uiState.value.areControlsVisible)
        }
    }

    fun toggleLock() {
        val locked = !_uiState.value.isControlsLocked
        _uiState.value = _uiState.value.copy(
            isControlsLocked = locked,
            areControlsVisible = !locked
        )
    }

    fun release() {
        val pos = exoPlayer?.currentPosition ?: 0L
        val dur = exoPlayer?.duration ?: 0L
        val item = currentItem
        progressTrackingJob?.cancel()
        if (item != null) {
            viewModelScope.launch {
                repository.reportPlaybackStopped(
                    itemId = item.id,
                    positionMs = pos,
                    durationMs = dur,
                    mediaSourceId = item.mediaSources?.firstOrNull()?.id
                )
            }
        }
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }
}

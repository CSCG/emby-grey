package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ResumePointEntity
import com.example.data.local.ServerPreferences
import com.example.data.model.AuthRequest
import com.example.data.model.EmbyItemDto
import com.example.data.model.LiveTvChannelDto
import com.example.data.model.PersonDto
import com.example.data.model.PlaybackProgressReport
import com.example.data.model.PlaybackQuality
import com.example.data.model.ServerConnection
import com.example.data.model.UserDataDto
import com.example.data.remote.EmbyApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class EmbyRepository(
    private val context: Context,
    val preferences: ServerPreferences,
    private val database: AppDatabase
) {
    val deviceId = UUID.nameUUIDFromBytes(context.packageName.toByteArray()).toString()
    val authHeader = "MediaBrowser Client=\"EmbyStream\", Device=\"Android\", DeviceId=\"$deviceId\", Version=\"1.0.0\""

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = run {
        val builder = OkHttpClient.Builder()
            .connectTimeout(7, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
        try {
            val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAll, SecureRandom())
            builder.sslSocketFactory(sslContext.socketFactory, trustAll[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Lenient SSL configuration skipped", e)
        }
        builder.build()
    }

    private var activeApi: EmbyApiService? = null
    private var activeServerUrl: String? = null

    init {
        val current = preferences.getConnection()
        if (current != null && !current.isDemo && current.url.isNotBlank()) {
            setupApi(current.url, current.token)
        }
    }

    private fun setupApi(baseUrl: String, token: String) {
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        activeServerUrl = formattedUrl

        val client = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("X-Emby-Token", token)
                chain.proceed(requestBuilder.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        activeApi = retrofit.create(EmbyApiService::class.java)
    }

    suspend fun authenticate(url: String, username: String, password: String):Result<ServerConnection> = withContext(Dispatchers.IO) {
        try {
            val formattedUrl = if (url.endsWith("/")) url else "$url/"
            val retrofit = Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val api = retrofit.create(EmbyApiService::class.java)
            val response = api.authenticate(
                authorization = authHeader,
                request = AuthRequest(username = username, pw = password)
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.accessToken ?: ""
                val userId = body.user?.id ?: ""
                val connection = ServerConnection(
                    url = formattedUrl.trimEnd('/'),
                    username = username,
                    userId = userId,
                    token = token,
                    serverName = body.serverId ?: "Emby Server",
                    isDemo = false
                )
                preferences.saveConnection(connection)
                setupApi(connection.url, token)
                Result.success(connection)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Authentication failed: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EmbyRepository", "Auth error", e)
            val isLocalIp = url.contains("192.168.") || url.contains("10.") || url.contains("172.16.") || url.contains("localhost") || url.contains("127.0.0.1")
            val isTimeout = e is java.net.SocketTimeoutException || e.message?.contains("failed to connect") == true || e.message?.contains("timed out", ignoreCase = true) == true
            val friendlyMsg = if (isTimeout && isLocalIp) {
                "Cannot reach private local IP ($url) from the cloud emulator. Please tap 'Try Demo Mode' below to explore the app, or use a public server URL/domain."
            } else if (isTimeout) {
                "Connection timed out connecting to $url. Please check that your server is reachable and port 8096 is open."
            } else {
                e.localizedMessage ?: "Connection error"
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    fun connectDemo(): ServerConnection {
        val demo = ServerConnection(
            url = "http://demo.embystream.local",
            username = "DemoUser",
            userId = "user_demo",
            token = "demo_token_xyz",
            serverName = "Emby Demo Cinema",
            isDemo = true
        )
        preferences.saveConnection(demo)
        activeApi = null
        return demo
    }

    fun isDemo(): Boolean {
        return preferences.getConnection()?.isDemo == true || activeApi == null
    }

    suspend fun getViews(): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            return@withContext DemoDataProvider.demoViews
        }
        try {
            val res = activeApi!!.getUserViews(conn.userId)
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.items
            } else {
                DemoDataProvider.demoViews
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching views, falling back to demo", e)
            DemoDataProvider.demoViews
        }
    }

    suspend fun getResumeItems(): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            val demoList = listOf(
                DemoDataProvider.demoMovies[0], // Big Buck Bunny (Movie)
                DemoDataProvider.demoSeries[0], // Cosmos Laundromat (Series)
                DemoDataProvider.demoMovies[1], // Tears of Steel (Movie)
                DemoDataProvider.demoMovies[2], // Sintel (Movie)
                DemoDataProvider.demoMovies[3]  // Elephants Dream (Movie)
            )
            return@withContext demoList.take(5)
        }
        try {
            val combinedList = mutableListOf<EmbyItemDto>()
            val res = activeApi!!.getResumeItems(conn.userId, limit = 10)
            if (res.isSuccessful && res.body() != null) {
                combinedList.addAll(res.body()!!.items)
            }
            // Merge with local Room resume points
            val localPoints = database.resumePointDao().getAllRecentResumePoints()
            for (point in localPoints) {
                if (combinedList.none { it.id == point.itemId }) {
                    try {
                        val item = getItemDetails(point.itemId)
                        if (item != null) {
                            val enriched = item.copy(
                                userData = UserDataDto(
                                    playbackPositionTicks = point.positionTicks,
                                    played = point.isCompleted
                                )
                            )
                            combinedList.add(enriched)
                        }
                    } catch (e: Exception) {
                        Log.w("EmbyRepository", "Error fetching local resume item", e)
                    }
                }
            }
            // If fewer than 5 items, supplement from demo/sample items so 5 items are always ready for resume playback
            if (combinedList.size < 5) {
                val demoCandidates = listOf(
                    DemoDataProvider.demoMovies[0],
                    DemoDataProvider.demoSeries[0],
                    DemoDataProvider.demoMovies[1],
                    DemoDataProvider.demoMovies[2],
                    DemoDataProvider.demoMovies[3]
                )
                for (cand in demoCandidates) {
                    if (combinedList.none { it.id == cand.id }) {
                        combinedList.add(cand)
                        if (combinedList.size >= 5) break
                    }
                }
            }
            combinedList.take(5)
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching resume items", e)
            listOf(
                DemoDataProvider.demoMovies[0],
                DemoDataProvider.demoSeries[0],
                DemoDataProvider.demoMovies[1],
                DemoDataProvider.demoMovies[2],
                DemoDataProvider.demoMovies[3]
            ).take(5)
        }
    }

    suspend fun getCollections(): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            return@withContext DemoDataProvider.demoCollections
        }
        try {
            val res = activeApi!!.getItems(
                userId = conn.userId,
                includeItemTypes = "BoxSet",
                sortBy = "SortName",
                sortOrder = "Ascending",
                recursive = true,
                limit = 20
            )
            if (res.isSuccessful && res.body() != null && res.body()!!.items.isNotEmpty()) {
                res.body()!!.items
            } else {
                DemoDataProvider.demoCollections
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching collections", e)
            DemoDataProvider.demoCollections
        }
    }

    suspend fun getLatestItems(parentId: String? = null): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            return@withContext DemoDataProvider.demoMovies + DemoDataProvider.demoSeries
        }
        try {
            val res = activeApi!!.getLatestItems(conn.userId, parentId = parentId)
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                res.body()!!
            } else {
                // If /Items/Latest returned empty, query recent items sorted by DateCreated
                val fallbackItems = getItems(
                    includeItemTypes = "Movie,Series,Episode",
                    sortBy = "DateCreated",
                    sortOrder = "Descending"
                )
                if (fallbackItems.isNotEmpty()) fallbackItems.take(16) else DemoDataProvider.demoMovies
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching latest items, attempting fallback", e)
            val fallbackItems = getItems(
                includeItemTypes = "Movie,Series,Episode",
                sortBy = "DateCreated",
                sortOrder = "Descending"
            )
            if (fallbackItems.isNotEmpty()) fallbackItems.take(16) else DemoDataProvider.demoMovies
        }
    }

    suspend fun getItems(
        parentId: String? = null,
        includeItemTypes: String? = null,
        searchTerm: String? = null,
        sortBy: String = "SortName",
        sortOrder: String = "Ascending"
    ): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            var items = DemoDataProvider.demoMovies + DemoDataProvider.demoSeries
            if (!searchTerm.isNullOrBlank()) {
                items = items.filter { it.name.contains(searchTerm, ignoreCase = true) || it.overview?.contains(searchTerm, ignoreCase = true) == true }
            }
            if (!includeItemTypes.isNullOrBlank()) {
                val types = includeItemTypes.split(",")
                items = items.filter { types.contains(it.type) }
            }
            return@withContext items
        }
        try {
            val res = activeApi!!.getItems(
                userId = conn.userId,
                parentId = parentId,
                includeItemTypes = includeItemTypes,
                searchTerm = searchTerm,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.items
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching items", e)
            emptyList()
        }
    }

    suspend fun searchMedia(
        query: String,
        filterType: String? = null
    ): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        val types = when (filterType) {
            "Movie" -> "Movie"
            "Series" -> "Series"
            else -> "Movie,Series"
        }

        if (conn == null || conn.isDemo || activeApi == null) {
            var items = DemoDataProvider.demoMovies + DemoDataProvider.demoSeries
            if (query.isNotBlank()) {
                items = items.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.overview?.contains(query, ignoreCase = true) == true ||
                    it.seriesName?.contains(query, ignoreCase = true) == true
                }
            }
            if (filterType != null && filterType != "All") {
                items = items.filter { it.type.equals(filterType, ignoreCase = true) }
            }
            return@withContext items
        }

        try {
            val res = activeApi!!.getItems(
                userId = conn.userId,
                includeItemTypes = types,
                searchTerm = query.trim(),
                sortBy = "SortName",
                sortOrder = "Ascending",
                recursive = true,
                limit = 60
            )
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.items
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error searching items for query: $query", e)
            emptyList()
        }
    }

    suspend fun getItemDetails(itemId: String): EmbyItemDto? = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            val all = DemoDataProvider.demoMovies + DemoDataProvider.demoSeries + DemoDataProvider.demoEpisodes + DemoDataProvider.demoCollections
            return@withContext all.firstOrNull { it.id == itemId }
        }
        try {
            val res = activeApi!!.getItemDetails(conn.userId, itemId)
            if (res.isSuccessful) {
                res.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching item details", e)
            null
        }
    }

    suspend fun getEpisodes(seriesId: String, seasonId: String? = null): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            return@withContext DemoDataProvider.demoEpisodes.filter { it.seriesId == seriesId }
        }
        try {
            val res = activeApi!!.getEpisodes(
                seriesId = seriesId,
                seasonId = seasonId?.takeIf { it.isNotBlank() },
                userId = conn.userId
            )
            if (res.isSuccessful && res.body() != null && res.body()!!.items.isNotEmpty()) {
                res.body()!!.items
            } else {
                // Secondary check: query via getItems with ParentId = seriesId and IncludeItemTypes = "Episode"
                val itemsRes = activeApi!!.getItems(
                    userId = conn.userId,
                    parentId = seriesId,
                    includeItemTypes = "Episode",
                    sortBy = "SortName",
                    sortOrder = "Ascending",
                    recursive = true
                )
                if (itemsRes.isSuccessful && itemsRes.body() != null && itemsRes.body()!!.items.isNotEmpty()) {
                    itemsRes.body()!!.items
                } else {
                    res.body()?.items ?: emptyList()
                }
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching episodes, trying fallback", e)
            try {
                val fallbackRes = activeApi!!.getItems(
                    userId = conn.userId,
                    parentId = seriesId,
                    includeItemTypes = "Episode",
                    sortBy = "SortName",
                    sortOrder = "Ascending",
                    recursive = true
                )
                if (fallbackRes.isSuccessful && fallbackRes.body() != null) {
                    return@withContext fallbackRes.body()!!.items
                }
            } catch (fallbackError: Exception) {
                Log.w("EmbyRepository", "Fallback getItems also failed", fallbackError)
            }
            emptyList()
        }
    }

    suspend fun getSimilarItems(itemId: String): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            val all = DemoDataProvider.demoMovies + DemoDataProvider.demoSeries
            return@withContext all.filter { it.id != itemId }.take(6)
        }
        try {
            val res = activeApi!!.getSimilarItems(itemId, conn.userId)
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.items
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching similar items", e)
            emptyList()
        }
    }

    suspend fun getCollectionItems(collectionId: String): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null || collectionId.startsWith("boxset_")) {
            return@withContext DemoDataProvider.getCollectionItems(collectionId)
        }
        try {
            val res = activeApi!!.getItems(
                userId = conn.userId,
                parentId = collectionId,
                sortBy = "SortName",
                sortOrder = "Ascending",
                recursive = true,
                limit = 100
            )
            if (res.isSuccessful && res.body() != null && res.body()!!.items.isNotEmpty()) {
                res.body()!!.items
            } else {
                val fallbackRes = activeApi!!.getItems(
                    userId = conn.userId,
                    parentId = collectionId,
                    sortBy = "SortName",
                    sortOrder = "Ascending",
                    recursive = false,
                    limit = 100
                )
                if (fallbackRes.isSuccessful && fallbackRes.body() != null && fallbackRes.body()!!.items.isNotEmpty()) {
                    fallbackRes.body()!!.items
                } else {
                    DemoDataProvider.getCollectionItems(collectionId)
                }
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching collection items", e)
            DemoDataProvider.getCollectionItems(collectionId)
        }
    }

    suspend fun getItemsByPerson(personId: String): List<EmbyItemDto> = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            return@withContext (DemoDataProvider.demoMovies + DemoDataProvider.demoSeries).take(3)
        }
        try {
            val res = activeApi!!.getItems(
                userId = conn.userId,
                personIds = personId,
                includeItemTypes = "Movie,Series",
                sortBy = "PremiereDate",
                sortOrder = "Descending"
            )
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.items
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Error fetching items for person: $personId", e)
            emptyList()
        }
    }

    fun getPersonImageUrl(person: PersonDto): String? {
        val conn = preferences.getConnection()
        if (person.id.isNullOrBlank()) return null
        if (conn == null || conn.isDemo) {
            return "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80"
        }
        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token
        return if (person.primaryImageTag != null) {
            "$baseUrl/Items/${person.id}/Images/Primary?maxWidth=300&quality=85&tag=${person.primaryImageTag}&api_key=$token"
        } else {
            "$baseUrl/Items/${person.id}/Images/Primary?maxWidth=300&quality=85&api_key=$token"
        }
    }

    suspend fun getNextEpisode(seriesId: String, currentSeasonNum: Int?, currentEpisodeNum: Int?): EmbyItemDto? = withContext(Dispatchers.IO) {
        val episodes = getEpisodes(seriesId)
        if (episodes.isEmpty()) return@withContext null
        val sorted = episodes.sortedWith(
            compareBy<EmbyItemDto> { it.parentIndexNumber ?: 1 }
                .thenBy { it.indexNumber ?: 1 }
        )
        val curSeason = currentSeasonNum ?: 1
        val curEp = currentEpisodeNum ?: 1
        sorted.firstOrNull { ep ->
            val epSeason = ep.parentIndexNumber ?: 1
            val epNum = ep.indexNumber ?: 1
            (epSeason == curSeason && epNum > curEp) || (epSeason > curSeason)
        }
    }

    fun buildStreamUrl(
        itemId: String,
        mediaSourceId: String? = null,
        quality: PlaybackQuality = PlaybackQuality.DIRECT_PLAY
    ): String {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo) {
            return DemoDataProvider.getStreamUrl(itemId)
        }

        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token
        val sourceId = mediaSourceId ?: itemId

        return if (quality == PlaybackQuality.DIRECT_PLAY) {
            "$baseUrl/Videos/$itemId/stream?static=true&api_key=$token"
        } else {
            val bitrate = quality.maxBitrate ?: 4_000_000L
            val channels = preferences.audioChannels
            val playSessionId = UUID.randomUUID().toString().replace("-", "")
            // Enforce H.264 video transcode without video stream copy so codecs unsupported by the device/emulator (such as HEVC) are re-encoded to universal H.264
            "$baseUrl/Videos/$itemId/master.m3u8?MediaSourceId=$sourceId&VideoCodec=h264&AudioCodec=aac&AudioChannels=$channels&MaxStreamingBitrate=$bitrate&VideoBitrate=$bitrate&TranscodingMaxAudioChannels=$channels&EnableSubtitlesInManifest=true&PlaySessionId=$playSessionId&api_key=$token"
        }
    }

    fun buildProgressiveTranscodeUrl(
        itemId: String,
        mediaSourceId: String? = null,
        quality: PlaybackQuality = PlaybackQuality.P1080
    ): String {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo) {
            return DemoDataProvider.getStreamUrl(itemId)
        }

        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token
        val sourceId = mediaSourceId ?: itemId
        val bitrate = quality.maxBitrate ?: 4_000_000L
        val channels = preferences.audioChannels
        val playSessionId = UUID.randomUUID().toString().replace("-", "")
        return "$baseUrl/Videos/$itemId/stream.mp4?static=false&MediaSourceId=$sourceId&VideoCodec=h264&AudioCodec=aac&AudioChannels=$channels&MaxStreamingBitrate=$bitrate&VideoBitrate=$bitrate&PlaySessionId=$playSessionId&api_key=$token"
    }

    fun buildSubtitleUrl(itemId: String, mediaSourceId: String?, index: Int, format: String = "vtt"): String? {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo) {
            return DemoDataProvider.getSubtitleUrl(itemId, if (index == 3) "spa" else "eng")
        }
        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token
        val sourceId = mediaSourceId ?: itemId
        return "$baseUrl/Videos/$itemId/$sourceId/Subtitles/$index/Stream.$format?api_key=$token"
    }

    suspend fun getLiveTvChannels(): List<LiveTvChannelDto> = withContext(Dispatchers.IO) {
        val freeChannels = FreeLiveTvChannels.channels
        val conn = preferences.getConnection()
        val rawChannels = if (conn == null || conn.isDemo || activeApi == null) {
            freeChannels
        } else {
            try {
                val res = activeApi!!.getLiveTvChannels(userId = conn.userId)
                if (res.isSuccessful && res.body() != null && res.body()!!.items.isNotEmpty()) {
                    val serverChannels = res.body()!!.items.map { ch ->
                        ch.copy(
                            isOnlineFast = false,
                            category = "Emby Server Tuner"
                        )
                    }
                    serverChannels + freeChannels
                } else {
                    freeChannels
                }
            } catch (e: Exception) {
                Log.w("EmbyRepository", "Error fetching Live TV channels from server, using free channels", e)
                freeChannels
            }
        }

        // Enrich with real-time Toonami EPG data
        try {
            ToonamiEpgService.enrichWithEpg(rawChannels)
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Toonami EPG enrichment failed: ${e.message}")
            rawChannels
        }
    }

    fun buildLiveTvStreamUrl(channelId: String): String {
        val freeCh = FreeLiveTvChannels.channels.firstOrNull { it.id == channelId }
        if (freeCh?.streamUrl != null) {
            return freeCh.streamUrl
        }
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo) {
            return FreeLiveTvChannels.channels.first().streamUrl ?: ""
        }
        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token
        return "$baseUrl/Videos/$channelId/live.m3u8?api_key=$token"
    }

    fun getChannelLogoUrl(channel: LiveTvChannelDto): String? {
        if (!channel.logoUrl.isNullOrBlank()) {
            return channel.logoUrl
        }
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo) return null
        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token
        return "$baseUrl/Items/${channel.id}/Images/Primary?maxWidth=300&api_key=$token"
    }

    fun getImageUrl(item: EmbyItemDto, isBackdrop: Boolean = false): String {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo) {
            return DemoDataProvider.getImageUrl(item.id, isBackdrop)
        }
        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token

        return if (isBackdrop) {
            val backdropTag = item.backdropImageTags?.firstOrNull()
            when {
                backdropTag != null -> "$baseUrl/Items/${item.id}/Images/Backdrop/0?maxWidth=1280&quality=85&tag=$backdropTag&api_key=$token"
                item.imageTags?.containsKey("Thumb") == true -> "$baseUrl/Items/${item.id}/Images/Thumb?maxWidth=1280&quality=85&tag=${item.imageTags["Thumb"]}&api_key=$token"
                item.imageTags?.containsKey("Primary") == true -> "$baseUrl/Items/${item.id}/Images/Primary?maxWidth=1280&quality=85&tag=${item.imageTags["Primary"]}&api_key=$token"
                item.type.equals("Episode", ignoreCase = true) && !item.seriesId.isNullOrBlank() ->
                    "$baseUrl/Items/${item.seriesId}/Images/Backdrop/0?maxWidth=1280&quality=85&api_key=$token"
                else -> "$baseUrl/Items/${item.id}/Images/Primary?maxWidth=1280&quality=85&api_key=$token"
            }
        } else {
            val primaryTag = item.imageTags?.get("Primary")
            val thumbTag = item.imageTags?.get("Thumb")
            when {
                primaryTag != null -> "$baseUrl/Items/${item.id}/Images/Primary?maxWidth=500&quality=90&tag=$primaryTag&api_key=$token"
                thumbTag != null -> "$baseUrl/Items/${item.id}/Images/Thumb?maxWidth=500&quality=90&tag=$thumbTag&api_key=$token"
                item.type.equals("Episode", ignoreCase = true) && !item.seriesId.isNullOrBlank() ->
                    "$baseUrl/Items/${item.seriesId}/Images/Primary?maxWidth=500&quality=90&api_key=$token"
                else -> "$baseUrl/Items/${item.id}/Images/Primary?maxWidth=500&quality=90&api_key=$token"
            }
        }
    }

    fun getImageUrl(itemId: String, isBackdrop: Boolean = false): String {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo) {
            return DemoDataProvider.getImageUrl(itemId, isBackdrop)
        }
        val baseUrl = conn.url.trimEnd('/')
        val token = conn.token
        val type = if (isBackdrop) "Backdrop/0" else "Primary"
        val maxDim = if (isBackdrop) 1280 else 500
        return "$baseUrl/Items/$itemId/Images/$type?maxWidth=$maxDim&quality=85&api_key=$token"
    }

    suspend fun reportPlaybackStart(
        itemId: String,
        mediaSourceId: String? = null,
        playMethod: String = "DirectPlay",
        audioIndex: Int? = null,
        subtitleIndex: Int? = null
    ) = withContext(Dispatchers.IO) {
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) return@withContext
        try {
            activeApi!!.reportPlaying(
                PlaybackProgressReport(
                    itemId = itemId,
                    mediaSourceId = mediaSourceId,
                    positionTicks = 0L,
                    isPaused = false,
                    playMethod = playMethod,
                    audioStreamIndex = audioIndex,
                    subtitleStreamIndex = subtitleIndex
                )
            )
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Failed reporting playback start to Emby", e)
        }
    }

    suspend fun reportPlaybackProgress(
        itemId: String,
        positionMs: Long,
        durationMs: Long,
        isPaused: Boolean,
        playMethod: String,
        mediaSourceId: String? = null,
        audioIndex: Int? = null,
        subtitleIndex: Int? = null
    ) = withContext(Dispatchers.IO) {
        val positionTicks = positionMs * 10_000L
        val durationTicks = durationMs * 10_000L

        // Always save to Room local resume database
        val isCompleted = durationMs > 0 && positionMs >= (durationMs * 0.9)
        database.resumePointDao().saveResumePoint(
            ResumePointEntity(
                itemId = itemId,
                positionTicks = positionTicks,
                durationTicks = durationTicks,
                isCompleted = isCompleted
            )
        )

        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) {
            return@withContext
        }

        try {
            activeApi!!.reportProgress(
                PlaybackProgressReport(
                    itemId = itemId,
                    mediaSourceId = mediaSourceId,
                    positionTicks = positionTicks,
                    isPaused = isPaused,
                    playMethod = playMethod,
                    audioStreamIndex = audioIndex,
                    subtitleStreamIndex = subtitleIndex
                )
            )
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Failed reporting playback progress to Emby", e)
        }
    }

    suspend fun reportPlaybackStopped(
        itemId: String,
        positionMs: Long,
        durationMs: Long,
        mediaSourceId: String? = null
    ) = withContext(Dispatchers.IO) {
        val positionTicks = positionMs * 10_000L
        val durationTicks = durationMs * 10_000L
        val isCompleted = durationMs > 0 && positionMs >= (durationMs * 0.9)

        database.resumePointDao().saveResumePoint(
            ResumePointEntity(
                itemId = itemId,
                positionTicks = positionTicks,
                durationTicks = durationTicks,
                isCompleted = isCompleted
            )
        )

        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) return@withContext

        try {
            if (isCompleted) {
                activeApi!!.markPlayed(conn.userId, itemId)
            }
            activeApi!!.reportStopped(
                PlaybackProgressReport(
                    itemId = itemId,
                    mediaSourceId = mediaSourceId,
                    positionTicks = positionTicks,
                    isPaused = true
                )
            )
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Failed reporting playback stopped to Emby", e)
        }
    }

    suspend fun markItemPlayed(itemId: String, played: Boolean) = withContext(Dispatchers.IO) {
        database.resumePointDao().saveResumePoint(
            ResumePointEntity(
                itemId = itemId,
                positionTicks = 0L,
                durationTicks = 0L,
                isCompleted = played
            )
        )
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) return@withContext
        try {
            if (played) {
                activeApi!!.markPlayed(conn.userId, itemId)
            } else {
                activeApi!!.markUnplayed(conn.userId, itemId)
            }
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Failed toggling played status on Emby", e)
        }
    }

    suspend fun clearResumePosition(itemId: String) = withContext(Dispatchers.IO) {
        database.resumePointDao().clearResumePoint(itemId)
        val conn = preferences.getConnection()
        if (conn == null || conn.isDemo || activeApi == null) return@withContext
        try {
            activeApi!!.reportStopped(
                PlaybackProgressReport(
                    itemId = itemId,
                    positionTicks = 0L,
                    isPaused = true
                )
            )
        } catch (e: Exception) {
            Log.w("EmbyRepository", "Failed clearing resume position on Emby", e)
        }
    }

    suspend fun getResumePosition(itemId: String, item: EmbyItemDto?): Long? = withContext(Dispatchers.IO) {
        // Priority 1: Check server playbackPositionTicks
        val serverTicks = item?.userData?.playbackPositionTicks ?: 0L
        val isServerPlayed = item?.userData?.played == true

        if (!isServerPlayed && serverTicks > 50_000_000L) { // > 5 seconds
            return@withContext serverTicks / 10_000L
        }

        // Priority 2: Check local Room database
        val point = database.resumePointDao().getResumePointSync(itemId)
        if (point != null && !point.isCompleted && point.positionTicks > 50_000_000L) {
            return@withContext point.positionTicks / 10_000L
        }

        null
    }

    suspend fun getLocalResumePosition(itemId: String): Long? = withContext(Dispatchers.IO) {
        val point = database.resumePointDao().getResumePointSync(itemId)
        if (point != null && !point.isCompleted && point.positionTicks > 0) {
            point.positionTicks / 10_000L // to ms
        } else {
            null
        }
    }
}

package com.example.data.repository

import android.util.Log
import com.example.data.model.LiveTvChannelDto
import com.example.data.model.LiveTvProgramDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap

/**
 * Service to fetch real-time EPG (Electronic Program Guide) data for Toonami Aftermath channels
 * using https://api.toonamiaftermath.com/channelsCurrentMedia and /mediainfo.
 */
object ToonamiEpgService {

    private const val TAG = "ToonamiEpgService"
    private const val CACHE_TTL_MS = 90_000L // 90 seconds cache

    private var lastFetchTime = 0L
    private var cachedChannelMedia: Map<String, List<ToonamiScheduleItem>> = emptyMap()
    private val mediaInfoCache = ConcurrentHashMap<String, MediaInfoDetails>()

    data class ToonamiScheduleItem(
        val name: String,
        val fullName: String?,
        val blockName: String?,
        val episodeNumber: Int?,
        val startMillis: Long,
        val endMillis: Long,
        val imageUrl: String?
    )

    data class MediaInfoDetails(
        val summary: String?,
        val rating: Double?,
        val imageUrl: String?,
        val genres: List<String>?
    )

    /**
     * Enriches the provided channels list with live real-time Toonami EPG data.
     */
    suspend fun enrichWithEpg(channels: List<LiveTvChannelDto>): List<LiveTvChannelDto> = withContext(Dispatchers.IO) {
        val scheduleMap = fetchCurrentScheduleIfNeeded()
        if (scheduleMap.isEmpty()) {
            return@withContext channels
        }

        val nowMillis = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        channels.map { channel ->
            val targetKey = when (channel.id) {
                "free_toonami_aftermath" -> "Toonami Aftermath East"
                "free_toonami_west" -> "Toonami Aftermath West"
                "free_toonami_movies" -> "Movies"
                "free_toonami_radio" -> "Toonami Aftermath Radio"
                "free_toonami_snick" -> "Snickelodeon East"
                "free_toonami_action_block" -> "Toonami Aftermath East"
                else -> null
            }

            if (targetKey == null) {
                channel
            } else {
                val mediaList = scheduleMap[targetKey] ?: emptyList()
                if (mediaList.isEmpty()) {
                    channel
                } else {
                    // Find currently playing program
                    val currentIndex = mediaList.indexOfFirst { item ->
                        nowMillis in item.startMillis until item.endMillis
                    }.let { if (it >= 0) it else 0 }

                    val current = mediaList[currentIndex]
                    val displayName = current.fullName ?: current.name
                    val epText = if (current.episodeNumber != null && current.episodeNumber > 0) " (Ep ${current.episodeNumber})" else ""
                    val blockTag = if (!current.blockName.isNullOrBlank()) "[${current.blockName}] " else ""

                    val startFormatted = timeFormat.format(Date(current.startMillis))
                    val endFormatted = timeFormat.format(Date(current.endMillis))
                    val formattedTimeRange = "$startFormatted - $endFormatted"

                    // Look up media info if available
                    val mediaInfo = fetchMediaInfoCached(displayName)
                    val overview = mediaInfo?.summary ?: buildString {
                        append("Now broadcasting on Toonami Aftermath")
                        if (!current.blockName.isNullOrBlank()) append(" during the ${current.blockName} block")
                        if (current.episodeNumber != null) append(" (Episode ${current.episodeNumber})")
                        append(". Airing $formattedTimeRange.")
                    }

                    val updatedCurrentProgram = LiveTvProgramDto(
                        id = "${channel.id}_${current.startMillis}",
                        name = "$blockTag$displayName$epText",
                        overview = overview,
                        startDate = startFormatted,
                        endDate = endFormatted,
                        blockName = current.blockName,
                        episodeNumber = current.episodeNumber,
                        episodeTitle = current.fullName,
                        formattedTime = formattedTimeRange,
                        posterUrl = mediaInfo?.imageUrl ?: current.imageUrl,
                        genres = mediaInfo?.genres ?: listOf("Toonami", "Anime", "Retro")
                    )

                    // Upcoming schedule
                    val upcoming = mediaList.drop(currentIndex + 1).take(6).map { next ->
                        val nextDisplay = next.fullName ?: next.name
                        val nextEp = if (next.episodeNumber != null && next.episodeNumber > 0) " (Ep ${next.episodeNumber})" else ""
                        val nextBlock = if (!next.blockName.isNullOrBlank()) "[${next.blockName}] " else ""
                        val nextStart = timeFormat.format(Date(next.startMillis))
                        val nextEnd = timeFormat.format(Date(next.endMillis))

                        LiveTvProgramDto(
                            id = "${channel.id}_${next.startMillis}",
                            name = "$nextBlock$nextDisplay$nextEp",
                            overview = "Starts at $nextStart on Toonami Aftermath (${next.blockName ?: "Block"}).",
                            startDate = nextStart,
                            endDate = nextEnd,
                            blockName = next.blockName,
                            episodeNumber = next.episodeNumber,
                            formattedTime = "$nextStart - $nextEnd"
                        )
                    }

                    channel.copy(
                        currentProgram = updatedCurrentProgram,
                        upcomingPrograms = upcoming
                    )
                }
            }
        }
    }

    private fun fetchCurrentScheduleIfNeeded(): Map<String, List<ToonamiScheduleItem>> {
        val now = System.currentTimeMillis()
        if (cachedChannelMedia.isNotEmpty() && (now - lastFetchTime) < CACHE_TTL_MS) {
            return cachedChannelMedia
        }

        try {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val minute = if (cal.get(Calendar.MINUTE) >= 30) 30 else 0
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val startDateStr = URLEncoder.encode(isoFormat.format(cal.time), "UTF-8")
            val urlString = "https://api.toonamiaftermath.com/channelsCurrentMedia?startDate=$startDateStr"

            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
            }

            if (connection.responseCode in 200..299) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val parsedMap = parseScheduleJson(jsonStr)
                if (parsedMap.isNotEmpty()) {
                    cachedChannelMedia = parsedMap
                    lastFetchTime = now
                    Log.d(TAG, "Successfully updated Toonami EPG schedule for ${parsedMap.size} channels.")
                    return parsedMap
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch Toonami EPG schedule: ${e.message}")
        }

        return cachedChannelMedia
    }

    private fun parseScheduleJson(jsonStr: String): Map<String, List<ToonamiScheduleItem>> {
        val result = mutableMapOf<String, MutableList<ToonamiScheduleItem>>()
        val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val isoParserFallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        try {
            val rootArray = JSONArray(jsonStr)
            for (i in 0 until rootArray.length()) {
                val channelObj = rootArray.getJSONObject(i)
                val channelName = channelObj.optString("name")
                if (channelName.isBlank()) continue

                val mediaArray = channelObj.optJSONArray("media") ?: continue
                val items = mutableListOf<ToonamiScheduleItem>()

                for (j in 0 until mediaArray.length()) {
                    val mediaObj = mediaArray.getJSONObject(j)
                    val name = mediaObj.optString("name", "Unknown Title")
                    val blockName = mediaObj.optString("blockName", null)
                    val epNumber = if (mediaObj.has("episodeNumber") && !mediaObj.isNull("episodeNumber")) {
                        mediaObj.optInt("episodeNumber")
                    } else null

                    val startDateStr = mediaObj.optString("startDate")
                    val startMillis = try {
                        isoParser.parse(startDateStr)?.time
                            ?: isoParserFallback.parse(startDateStr)?.time
                            ?: System.currentTimeMillis()
                    } catch (_: Exception) {
                        try {
                            isoParserFallback.parse(startDateStr)?.time ?: System.currentTimeMillis()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }
                    }

                    // End time is next item's start time, or +25 minutes
                    val nextStartMillis = if (j + 1 < mediaArray.length()) {
                        val nextObj = mediaArray.getJSONObject(j + 1)
                        val nextDateStr = nextObj.optString("startDate")
                        try {
                            isoParser.parse(nextDateStr)?.time
                                ?: isoParserFallback.parse(nextDateStr)?.time
                                ?: (startMillis + 25 * 60 * 1000L)
                        } catch (_: Exception) {
                            startMillis + 25 * 60 * 1000L
                        }
                    } else {
                        startMillis + 25 * 60 * 1000L
                    }

                    var fullName: String? = null
                    var imageUrl: String? = null
                    if (mediaObj.has("info") && !mediaObj.isNull("info")) {
                        val infoObj = mediaObj.optJSONObject("info")
                        if (infoObj != null) {
                            fullName = infoObj.optString("fullname", null)
                            imageUrl = infoObj.optString("image", null)
                        }
                    }

                    items.add(
                        ToonamiScheduleItem(
                            name = name,
                            fullName = fullName,
                            blockName = blockName,
                            episodeNumber = epNumber,
                            startMillis = startMillis,
                            endMillis = nextStartMillis,
                            imageUrl = imageUrl
                        )
                    )
                }

                result[channelName] = items
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing Toonami schedule JSON: ${e.message}")
        }
        return result
    }

    private fun fetchMediaInfoCached(showName: String): MediaInfoDetails? {
        if (showName.isBlank()) return null
        mediaInfoCache[showName]?.let { return it }

        return try {
            val encodedName = URLEncoder.encode(showName, "UTF-8")
            val url = "https://api.toonamiaftermath.com/mediainfo?name=$encodedName"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(body)
                val summary = obj.optString("summary", null)
                val rating = if (obj.has("rating") && !obj.isNull("rating")) obj.optDouble("rating") else null
                val image = obj.optString("image", null)
                val genres = mutableListOf<String>()
                val genreArray = obj.optJSONArray("genres")
                if (genreArray != null) {
                    for (k in 0 until genreArray.length()) {
                        genres.add(genreArray.getString(k))
                    }
                }
                val details = MediaInfoDetails(
                    summary = summary,
                    rating = rating,
                    imageUrl = image,
                    genres = if (genres.isNotEmpty()) genres else null
                )
                mediaInfoCache[showName] = details
                details
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

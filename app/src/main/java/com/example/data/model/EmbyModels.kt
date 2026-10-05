package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmbyItemDto(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String = "",
    @Json(name = "Type") val type: String = "",
    @Json(name = "Overview") val overview: String? = null,
    @Json(name = "ProductionYear") val productionYear: Int? = null,
    @Json(name = "RunTimeTicks") val runTimeTicks: Long? = null,
    @Json(name = "CommunityRating") val communityRating: Float? = null,
    @Json(name = "OfficialRating") val officialRating: String? = null,
    @Json(name = "Genres") val genres: List<String>? = null,
    @Json(name = "SeriesName") val seriesName: String? = null,
    @Json(name = "SeriesId") val seriesId: String? = null,
    @Json(name = "SeasonName") val seasonName: String? = null,
    @Json(name = "SeasonId") val seasonId: String? = null,
    @Json(name = "IndexNumber") val indexNumber: Int? = null,
    @Json(name = "ParentIndexNumber") val parentIndexNumber: Int? = null,
    @Json(name = "UserData") val userData: UserDataDto? = null,
    @Json(name = "MediaSources") val mediaSources: List<MediaSourceDto>? = null,
    @Json(name = "ImageTags") val imageTags: Map<String, String>? = null,
    @Json(name = "BackdropImageTags") val backdropImageTags: List<String>? = null,
    @Json(name = "CollectionType") val collectionType: String? = null,
    @Json(name = "Chapters") val chapters: List<ChapterInfoDto>? = null,
    @Json(name = "People") val people: List<PersonDto>? = null,
    @Json(name = "SeriesPrimaryImageTag") val seriesPrimaryImageTag: String? = null,
    @Json(name = "Path") val path: String? = null
) {
    val durationMinutes: Long
        get() = (runTimeTicks ?: 0L) / (10_000L * 1000L * 60L)

    val resumePositionTicks: Long
        get() = userData?.playbackPositionTicks ?: 0L

    val isPlayed: Boolean
        get() = userData?.played ?: false

    val resumeFraction: Float
        get() {
            val total = runTimeTicks ?: return 0f
            if (total <= 0) return 0f
            return (resumePositionTicks.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
}

@JsonClass(generateAdapter = true)
data class UserDataDto(
    @Json(name = "PlaybackPositionTicks") val playbackPositionTicks: Long? = 0L,
    @Json(name = "PlayCount") val playCount: Int? = 0,
    @Json(name = "Played") val played: Boolean? = false,
    @Json(name = "IsFavorite") val isFavorite: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class MediaSourceDto(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String? = null,
    @Json(name = "Container") val container: String? = null,
    @Json(name = "Size") val size: Long? = null,
    @Json(name = "Bitrate") val bitrate: Int? = null,
    @Json(name = "SupportsDirectPlay") val supportsDirectPlay: Boolean? = true,
    @Json(name = "SupportsDirectStream") val supportsDirectStream: Boolean? = true,
    @Json(name = "SupportsTranscoding") val supportsTranscoding: Boolean? = true,
    @Json(name = "MediaStreams") val mediaStreams: List<MediaStreamDto>? = null
)

@JsonClass(generateAdapter = true)
data class MediaStreamDto(
    @Json(name = "Type") val type: String = "", // Video, Audio, Subtitle
    @Json(name = "Index") val index: Int = 0,
    @Json(name = "Codec") val codec: String? = null,
    @Json(name = "Language") val language: String? = null,
    @Json(name = "DisplayTitle") val displayTitle: String? = null,
    @Json(name = "IsDefault") val isDefault: Boolean? = false,
    @Json(name = "IsForced") val isForced: Boolean? = false,
    @Json(name = "IsExternal") val isExternal: Boolean? = false,
    @Json(name = "DeliveryUrl") val deliveryUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class ItemsResponse(
    @Json(name = "Items") val items: List<EmbyItemDto> = emptyList(),
    @Json(name = "TotalRecordCount") val totalRecordCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class AuthRequest(
    @Json(name = "Username") val username: String,
    @Json(name = "Pw") val pw: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "User") val user: UserDto? = null,
    @Json(name = "AccessToken") val accessToken: String? = null,
    @Json(name = "ServerId") val serverId: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String = "",
    @Json(name = "HasPassword") val hasPassword: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PlaybackInfoResponse(
    @Json(name = "MediaSources") val mediaSources: List<MediaSourceDto>? = null
)

@JsonClass(generateAdapter = true)
data class PlaybackProgressReport(
    @Json(name = "ItemId") val itemId: String,
    @Json(name = "MediaSourceId") val mediaSourceId: String? = null,
    @Json(name = "PositionTicks") val positionTicks: Long,
    @Json(name = "IsPaused") val isPaused: Boolean = false,
    @Json(name = "PlayMethod") val playMethod: String = "DirectPlay", // DirectPlay, Transcode
    @Json(name = "AudioStreamIndex") val audioStreamIndex: Int? = null,
    @Json(name = "SubtitleStreamIndex") val subtitleStreamIndex: Int? = null
)

enum class PlaybackQuality(
    val label: String,
    val description: String,
    val maxBitrate: Long?, // in bps
    val maxResolution: String?
) {
    DIRECT_PLAY("Direct Play", "Original file stream (zero transcoding)", null, "Original"),
    P1080("1080p (10 Mbps)", "Hardware transcode FHD", 10_000_000L, "1080p"),
    P720("720p (4 Mbps)", "Hardware transcode HD", 4_000_000L, "720p"),
    P480("480p (1.5 Mbps)", "Transcode SD (mobile friendly)", 1_500_000L, "480p"),
    P360("360p (700 Kbps)", "Low bandwidth saving", 700_000L, "360p")
}

data class ServerConnection(
    val url: String,
    val username: String,
    val userId: String,
    val token: String,
    val serverName: String = "Emby Server",
    val isDemo: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ChapterInfoDto(
    @Json(name = "Name") val name: String? = null,
    @Json(name = "StartPositionTicks") val startPositionTicks: Long = 0L,
    @Json(name = "ImageTag") val imageTag: String? = null
) {
    val startPositionMs: Long
        get() = startPositionTicks / 10_000L
}

@JsonClass(generateAdapter = true)
data class PersonDto(
    @Json(name = "Id") val id: String? = null,
    @Json(name = "Name") val name: String = "",
    @Json(name = "Role") val role: String? = null,
    @Json(name = "Type") val type: String? = null,
    @Json(name = "PrimaryImageTag") val primaryImageTag: String? = null
)

@JsonClass(generateAdapter = true)
data class LiveTvProgramDto(
    @Json(name = "Id") val id: String? = null,
    @Json(name = "Name") val name: String = "",
    @Json(name = "Overview") val overview: String? = null,
    @Json(name = "StartDate") val startDate: String? = null,
    @Json(name = "EndDate") val endDate: String? = null,
    @Json(name = "ProductionYear") val productionYear: Int? = null,
    @Json(name = "Genres") val genres: List<String>? = null,
    val blockName: String? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val formattedTime: String? = null,
    val posterUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class LiveTvChannelDto(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String,
    @Json(name = "Number") val number: String? = null,
    @Json(name = "ChannelType") val channelType: String? = "TV",
    @Json(name = "CurrentProgram") val currentProgram: LiveTvProgramDto? = null,
    @Json(name = "ImageTags") val imageTags: Map<String, String>? = null,
    val streamUrl: String? = null,
    val category: String = "General",
    val logoUrl: String? = null,
    val isOnlineFast: Boolean = false,
    val location: String? = null,
    val isTrafficCam: Boolean = false,
    val weatherNote: String? = null,
    val snapshotUrl: String? = null,
    val upcomingPrograms: List<LiveTvProgramDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LiveTvChannelsResponse(
    @Json(name = "Items") val items: List<LiveTvChannelDto> = emptyList(),
    @Json(name = "TotalRecordCount") val totalRecordCount: Int = 0
)

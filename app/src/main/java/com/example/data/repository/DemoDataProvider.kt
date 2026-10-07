package com.example.data.repository

import com.example.data.model.EmbyItemDto
import com.example.data.model.MediaSourceDto
import com.example.data.model.MediaStreamDto
import com.example.data.model.UserDataDto

object DemoDataProvider {

    val demoViews = listOf(
        EmbyItemDto(
            id = "view_movies",
            name = "Movies",
            type = "CollectionFolder",
            collectionType = "movies",
            overview = "Explore your feature films collection"
        ),
        EmbyItemDto(
            id = "view_tvshows",
            name = "TV Shows",
            type = "CollectionFolder",
            collectionType = "tvshows",
            overview = "Browse seasons and television series"
        ),
        EmbyItemDto(
            id = "view_animation",
            name = "Animation & Shorts",
            type = "CollectionFolder",
            collectionType = "movies",
            overview = "Open source cinema masterpieces"
        ),
        EmbyItemDto(
            id = "view_collections",
            name = "Collections",
            type = "CollectionFolder",
            collectionType = "boxsets",
            overview = "Curated sagas and open cinema franchises"
        )
    )

    val demoMovies = listOf(
        EmbyItemDto(
            id = "movie_bbb",
            name = "Big Buck Bunny",
            type = "Movie",
            overview = "A large and lovable rabbit deals with bullying forest creatures in this iconic Blender Foundation computer-animated comedy.",
            productionYear = 2008,
            runTimeTicks = 596_000_0000L, // ~9.9 mins
            communityRating = 8.4f,
            officialRating = "PG",
            genres = listOf("Animation", "Comedy", "Short"),
            userData = UserDataDto(playbackPositionTicks = 120_000_0000L, played = false),
            mediaSources = listOf(
                MediaSourceDto(
                    id = "src_bbb",
                    name = "1080p H.264",
                    container = "mp4",
                    size = 158_000_000L,
                    bitrate = 2_100_000,
                    mediaStreams = listOf(
                        MediaStreamDto(type = "Video", index = 0, codec = "h264", displayTitle = "1080p H.264 High"),
                        MediaStreamDto(type = "Audio", index = 1, codec = "aac", language = "eng", displayTitle = "English (AAC Stereo)"),
                        MediaStreamDto(type = "Subtitle", index = 2, codec = "vtt", language = "eng", displayTitle = "English [CC]", isDefault = true),
                        MediaStreamDto(type = "Subtitle", index = 3, codec = "vtt", language = "spa", displayTitle = "Spanish [Español]")
                    )
                )
            )
        ),
        EmbyItemDto(
            id = "movie_tos",
            name = "Tears of Steel",
            type = "Movie",
            overview = "In a dystopian future, a group of warriors and scientists gather at the Oude Kerk in Amsterdam to stage a crucial historical event in a desperate attempt to rescue the world from destructive robots.",
            productionYear = 2012,
            runTimeTicks = 734_000_0000L, // ~12.2 mins
            communityRating = 7.8f,
            officialRating = "PG-13",
            genres = listOf("Sci-Fi", "Action", "Short"),
            userData = UserDataDto(playbackPositionTicks = 320_000_0000L, played = false),
            mediaSources = listOf(
                MediaSourceDto(
                    id = "src_tos",
                    name = "1080p H.264 Sci-Fi",
                    container = "mp4",
                    size = 192_000_000L,
                    bitrate = 2_500_000,
                    mediaStreams = listOf(
                        MediaStreamDto(type = "Video", index = 0, codec = "h264", displayTitle = "1080p 24fps"),
                        MediaStreamDto(type = "Audio", index = 1, codec = "aac", language = "eng", displayTitle = "English 5.1 Surround"),
                        MediaStreamDto(type = "Subtitle", index = 2, codec = "vtt", language = "eng", displayTitle = "English Commentary")
                    )
                )
            )
        ),
        EmbyItemDto(
            id = "movie_sintel",
            name = "Sintel",
            type = "Movie",
            overview = "A lonely young woman, Sintel, helps and befriends a baby dragon whom she names Scales. But when Scales is snatched away by an adult dragon, Sintel embarks on an arduous and dangerous quest across mountains and deserts to reclaim him.",
            productionYear = 2010,
            runTimeTicks = 918_000_0000L, // ~15.3 mins
            communityRating = 8.1f,
            officialRating = "PG-13",
            genres = listOf("Fantasy", "Adventure", "Drama"),
            userData = UserDataDto(playbackPositionTicks = 450_000_0000L, played = false),
            mediaSources = listOf(
                MediaSourceDto(
                    id = "src_sintel",
                    name = "1080p Master",
                    container = "mp4",
                    size = 240_000_000L,
                    bitrate = 2_800_000,
                    mediaStreams = listOf(
                        MediaStreamDto(type = "Video", index = 0, codec = "h264", displayTitle = "1080p Master"),
                        MediaStreamDto(type = "Audio", index = 1, codec = "aac", language = "eng", displayTitle = "English Surround"),
                        MediaStreamDto(type = "Subtitle", index = 2, codec = "vtt", language = "eng", displayTitle = "English [SDH]")
                    )
                )
            )
        ),
        EmbyItemDto(
            id = "movie_elephants_dream",
            name = "Elephants Dream",
            type = "Movie",
            overview = "Two strange characters, Proog and Emo, explore the complex surreal mechanisms of an infinite mechanical world that reacts to their inner emotions.",
            productionYear = 2006,
            runTimeTicks = 654_000_0000L,
            communityRating = 7.5f,
            officialRating = "PG",
            genres = listOf("Animation", "Sci-Fi", "Mystery"),
            userData = UserDataDto(playbackPositionTicks = 210_000_0000L, played = false),
            mediaSources = listOf(
                MediaSourceDto(
                    id = "src_ed",
                    name = "1080p Stream",
                    container = "mp4",
                    size = 130_000_000L,
                    bitrate = 1_800_000
                )
            )
        )
    )

    val demoCollections = listOf(
        EmbyItemDto(
            id = "boxset_open_cinema",
            name = "Blender Open Cinema Collection",
            type = "BoxSet",
            collectionType = "movies",
            overview = "The complete multi-award-winning collection of iconic Blender Foundation open movies and computer animations.",
            productionYear = 2024,
            communityRating = 8.6f,
            genres = listOf("Animation", "Cinema", "Sci-Fi", "Fantasy")
        )
    )

    val demoSeries = listOf(
        EmbyItemDto(
            id = "series_cosmos",
            name = "Cosmos Laundromat",
            type = "Series",
            overview = "On a desolate island, a suicidal sheep named Franck meets a quirky salesman named Victor, who offers him the gift of a lifetime: the chance to live many lives across the universe.",
            productionYear = 2015,
            communityRating = 8.7f,
            officialRating = "TV-14",
            genres = listOf("Animation", "Fantasy", "Sci-Fi"),
            userData = UserDataDto(playbackPositionTicks = 200_000_0000L, played = false)
        )
    )

    val demoEpisodes = listOf(
        EmbyItemDto(
            id = "ep_cosmos_1",
            name = "First Cycle: On Desolate Island",
            type = "Episode",
            seriesName = "Cosmos Laundromat",
            seriesId = "series_cosmos",
            seasonName = "Season 1",
            seasonId = "season_cosmos_1",
            indexNumber = 1,
            parentIndexNumber = 1,
            overview = "Franck encounters the mysterious Victor in a bizarre world between reality and reincarnation.",
            productionYear = 2015,
            runTimeTicks = 720_000_0000L,
            communityRating = 8.8f,
            genres = listOf("Animation", "Fantasy"),
            userData = UserDataDto(playbackPositionTicks = 200_000_0000L, played = false),
            mediaSources = listOf(
                MediaSourceDto(
                    id = "src_ep_cosmos_1",
                    name = "HD 1080p",
                    container = "mp4",
                    size = 180_000_000L,
                    bitrate = 2_200_000
                )
            )
        ),
        EmbyItemDto(
            id = "ep_cosmos_2",
            name = "Second Cycle: Rebirth & Reflection",
            type = "Episode",
            seriesName = "Cosmos Laundromat",
            seriesId = "series_cosmos",
            seasonName = "Season 1",
            seasonId = "season_cosmos_1",
            indexNumber = 2,
            parentIndexNumber = 1,
            overview = "Exploring the cosmic laundromat machines and navigating alternate consciousness.",
            productionYear = 2016,
            runTimeTicks = 680_000_0000L,
            communityRating = 8.5f,
            genres = listOf("Animation", "Fantasy"),
            userData = UserDataDto(playbackPositionTicks = 160_000_0000L, played = false),
            mediaSources = listOf(
                MediaSourceDto(
                    id = "src_ep_cosmos_2",
                    name = "HD 1080p",
                    container = "mp4",
                    size = 170_000_000L,
                    bitrate = 2_100_000
                )
            )
        )
    )

    fun getCollectionItems(collectionId: String): List<EmbyItemDto> {
        return demoMovies
    }

    // Direct playback video streams for open media
    fun getStreamUrl(itemId: String): String {
        return when (itemId) {
            "movie_bbb" -> "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            "movie_tos" -> "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
            "movie_sintel" -> "https://media.w3.org/2010/05/sintel/trailer.mp4"
            "movie_elephants_dream" -> "https://vjs.zencdn.net/v/oceans.mp4"
            "ep_cosmos_1" -> "https://media.w3.org/2010/05/bunny/movie.mp4"
            "ep_cosmos_2" -> "https://media.w3.org/2010/05/sintel/trailer.mp4"
            "series_cosmos" -> "https://media.w3.org/2010/05/bunny/movie.mp4"
            else -> "https://media.w3.org/2010/05/bunny/movie.mp4"
        }
    }

    // Direct subtitle stream URLs using local data URIs to guarantee zero 404 network failures
    fun getSubtitleUrl(itemId: String, language: String): String? {
        val vttContent = when (language) {
            "spa" -> "WEBVTT\n\n1\n00:00:01.000 --> 00:00:06.000\n[Música de bosque]\n\n2\n00:00:07.000 --> 00:00:15.000\nBig Buck Bunny se despierta en el bosque."
            else -> "WEBVTT\n\n1\n00:00:01.000 --> 00:00:06.000\n[Upbeat orchestral theme]\n\n2\n00:00:07.000 --> 00:00:15.000\nBig Buck Bunny awakens in the peaceful morning forest."
        }
        return "data:text/vtt;charset=utf-8," + java.net.URLEncoder.encode(vttContent, "UTF-8")
    }

    // High quality backdrop/poster artwork URLs for the open films
    fun getImageUrl(itemId: String, isBackdrop: Boolean = false): String {
        return when (itemId) {
            "movie_bbb" -> if (isBackdrop)
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1280&q=80"
                else "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80"
            "movie_tos" -> if (isBackdrop)
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1280&q=80"
                else "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80"
            "movie_sintel" -> if (isBackdrop)
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1280&q=80"
                else "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&q=80"
            "movie_elephants_dream" -> if (isBackdrop)
                "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1280&q=80"
                else "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&q=80"
            "series_cosmos", "ep_cosmos_1", "ep_cosmos_2" -> if (isBackdrop)
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1280&q=80"
                else "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&q=80"
            "boxset_open_cinema" -> if (isBackdrop)
                "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1280&q=80"
                else "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80"
            else -> "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80"
        }
    }
}

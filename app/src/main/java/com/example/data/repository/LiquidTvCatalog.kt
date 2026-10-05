package com.example.data.repository

import com.example.data.model.LiveTvProgramDto
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Catalog and stream resolver for MTV's Liquid Television complete archive:
 * https://archive.org/details/liquid-television-complete
 */
object LiquidTvCatalog {

    data class Episode(
        val season: Int,
        val episode: Int,
        val filename: String,
        val featuredSegments: String,
        val durationSeconds: Int
    ) {
        val title: String get() = "Liquid TV S${season}E${episode}"
        val fullTitle: String get() = "MTV Liquid Television - Season $season, Episode $episode"
        val streamUrl: String get() {
            val encoded = URLEncoder.encode(filename, "UTF-8").replace("+", "%20")
            return "https://archive.org/download/liquid-television-complete/$encoded"
        }
    }

    val episodes: List<Episode> = listOf(
        Episode(1, 1, "Liquid Television 101.mp4", "Dangerous Puppets, Invisible Hands (Ch. 1), Æon Flux debut, Winter Steele", 1541),
        Episode(1, 2, "Liquid Television 102.mp4", "Winter Steele: Easy Wheels, Psychogram, Cut-Up Camera, Æon Flux: Gravity", 1481),
        Episode(1, 3, "Liquid Television 103.mp4", "Beavis and Butt-Head in 'Frog Baseball', Stick Figure Theater, Æon Flux: Mirror", 1618),
        Episode(1, 4, "Liquid Television 104.mp4", "Beavis and Butt-Head: Peace, Love and Understanding, The Specialists, Æon Flux", 1475),
        Episode(1, 5, "Liquid Television 105.mp4", "Glove Story, Bobby & Billy, Invisible Hands, Æon Flux: Leisure", 1491),
        Episode(1, 6, "Liquid Television 106.mp4", "The Running Man, Slick & Seattle, Dangerous Puppets, Æon Flux: Tide", 1425),
        Episode(2, 1, "Liquid Television 201.mp4", "The Specialists, Dog-Boy, Crazy Daisy Ed, Æon Flux: War", 1421),
        Episode(2, 2, "Liquid Television 202.mp4", "Brad Dharma: Psychedelic Detective, Stick Figure Theater, Winter Steele", 1440),
        Episode(2, 3, "Liquid Television 203.mp4", "Joe Normal, Winter Steele: Born to Be Wild, Dog-Boy, Æon Flux", 1413),
        Episode(2, 4, "Liquid Television 204.mp4", "Stick Figure Theater, Danger Rangers, Bobby & Billy, Æon Flux", 1424),
        Episode(2, 5, "Liquid Television 205.mp4", "Black Hula, Bobby & Billy: On The Farm, The Specialists, Æon Flux", 1430),
        Episode(2, 6, "Liquid Television 206.mp4", "Crazy Daisy Ed, Dog-Boy: The Date, The End, Brad Dharma", 1427),
        Episode(2, 7, "Liquid Television 207.mp4", "Speedbump the Roadkill Possum, Winter Steele, The Specialists", 1411),
        Episode(2, 8, "Liquid Television 208.mp4", "Brad Dharma: Case of the Mind Slugs, Stick Figure Theater, Æon Flux", 1420),
        Episode(2, 9, "Liquid Television 209.mp4", "Dog-Boy, Winter Steele, Crazy Daisy Ed, Megas Origins", 1430),
        Episode(2, 10, "Liquid Television 210.mp4", "Cut-Up Camera Part II, The Running Man, Season 2 Showcase", 1428),
        Episode(3, 1, "Liquid Television 301.mp4", "Headcandy, Cat & Muck, Stick Figure Theater: Psycho, Crazy Daisy Ed", 1365),
        Episode(3, 2, "Liquid Television 302.mp4", "Uncle Louie, Winter Steele: Reckless Hearts, Dog-Boy", 1335),
        Episode(3, 3, "Liquid Television 303.mp4", "Invisible Hands: Chapter 3, Bobby & Billy, Brad Dharma", 1337),
        Episode(3, 4, "Liqiud Television 304.mp4", "Bobby & Billy: School Daze, Stick Figure Theater, Dog-Boy", 1318),
        Episode(3, 5, "Liquid Television 305.mp4", "The Specialists Finale, Winter Steele, Crazy Daisy Ed", 1320),
        Episode(3, 6, "Liquid Television 306.mp4", "Liquid Television Grand Finale: The Best of Liquid TV & Indie Animation", 1308)
    )

    fun getRandomEpisode(): Episode {
        return episodes[Random.nextInt(episodes.size)]
    }

    /**
     * Continuous 24/7 virtual broadcast clock: aligns current episode based on epoch minutes
     */
    fun getCurrentScheduledEpisode(): Episode {
        val totalEpisodes = episodes.size
        // Approx 24 minutes per episode (1440 seconds)
        val epochMinutes = System.currentTimeMillis() / (1000L * 60L * 24L)
        val index = (epochMinutes % totalEpisodes).toInt().coerceIn(0, totalEpisodes - 1)
        return episodes[index]
    }

    fun getUpcomingSchedule(count: Int = 4): List<LiveTvProgramDto> {
        val current = getCurrentScheduledEpisode()
        val currentIndex = episodes.indexOf(current).let { if (it >= 0) it else 0 }
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowMillis = System.currentTimeMillis()

        return (1..count).map { offset ->
            val nextEp = episodes[(currentIndex + offset) % episodes.size]
            val startMillis = nowMillis + (offset * 24 * 60 * 1000L)
            val endMillis = startMillis + (24 * 60 * 1000L)
            val startTime = timeFormat.format(Date(startMillis))
            val endTime = timeFormat.format(Date(endMillis))

            LiveTvProgramDto(
                id = "liquid_${nextEp.season}_${nextEp.episode}_$offset",
                name = "${nextEp.fullTitle}",
                overview = "Featuring: ${nextEp.featuredSegments}",
                startDate = startTime,
                endDate = endTime,
                blockName = "Liquid TV",
                episodeNumber = nextEp.episode,
                formattedTime = "$startTime - $endTime"
            )
        }
    }
}

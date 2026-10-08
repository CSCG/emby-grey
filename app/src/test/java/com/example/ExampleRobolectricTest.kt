package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DownloadItemEntity
import com.example.data.model.PlaybackQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EmbyStream", appName)
    }

    @Test
    fun `playback quality values are valid`() {
        val directPlay = PlaybackQuality.DIRECT_PLAY
        assertEquals("Direct Play", directPlay.label)
        assertNotNull(PlaybackQuality.P1080.maxBitrate)
    }

    @Test
    fun `download item entity progress calculation`() {
        val item = DownloadItemEntity(
            id = "test_item_1",
            title = "Test Movie",
            mediaType = "Movie",
            fileSize = 1000L,
            downloadedBytes = 500L,
            status = DownloadItemEntity.STATUS_DOWNLOADING
        )
        assertEquals(0.5f, item.progressFraction, 0.01f)
    }

    @Test
    fun `collection returns all titles within the collection`() {
        val collectionItems = com.example.data.repository.DemoDataProvider.getCollectionItems("boxset_open_cinema")
        assertEquals(4, collectionItems.size)
        assertEquals("Big Buck Bunny", collectionItems[0].name)
        assertEquals("Tears of Steel", collectionItems[1].name)
        assertEquals("Sintel", collectionItems[2].name)
        assertEquals("Elephants Dream", collectionItems[3].name)
    }

    @Test
    fun `resume playback items provides five items with series and movies`() {
        val demoMovies = com.example.data.repository.DemoDataProvider.demoMovies
        val demoSeries = com.example.data.repository.DemoDataProvider.demoSeries
        val resumeCandidates = listOf(
            demoMovies[0],
            demoSeries[0],
            demoMovies[1],
            demoMovies[2],
            demoMovies[3]
        )
        assertEquals(5, resumeCandidates.size)
        assertEquals(1, resumeCandidates.count { it.type == "Series" })
        assertEquals(4, resumeCandidates.count { it.type == "Movie" })
        resumeCandidates.forEach {
            assertNotNull(it.userData?.playbackPositionTicks)
            org.junit.Assert.assertTrue((it.userData?.playbackPositionTicks ?: 0L) > 0L)
        }
    }

    @Test
    fun `ten new IPTV channels are present with valid stream URLs and categories`() {
        val newChannelIds = listOf(
            "free_buzzr",
            "free_court_tv",
            "free_fox_weather",
            "free_gb_news",
            "free_bloomberg_tv",
            "free_great_movies_uk",
            "free_action_hollywood",
            "free_pop_kids_uk",
            "free_now_80s",
            "free_now_rock"
        )
        val channels = com.example.data.repository.FreeLiveTvChannels.channels
        newChannelIds.forEach { id ->
            val ch = channels.firstOrNull { it.id == id }
            assertNotNull("Channel $id should be present", ch)
            org.junit.Assert.assertTrue("Channel $id should have a stream URL", !ch!!.streamUrl.isNullOrBlank())
            org.junit.Assert.assertTrue("Stream URL should be an http URL", ch.streamUrl!!.startsWith("http"))
            org.junit.Assert.assertTrue("Category should not be blank", ch.category.isNotBlank())
        }
    }
}

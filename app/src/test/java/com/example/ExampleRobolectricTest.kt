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
}

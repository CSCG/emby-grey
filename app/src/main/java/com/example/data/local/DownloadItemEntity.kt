package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mediaType: String, // Movie, Episode
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val overview: String? = null,
    val durationMs: Long = 0L,
    val fileSize: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: String = STATUS_QUEUED, // QUEUED, DOWNLOADING, COMPLETED, FAILED, PAUSED
    val localFilePath: String? = null,
    val posterUrl: String? = null,
    val downloadUrl: String = "",
    val addedTimestamp: Long = System.currentTimeMillis(),
    val completionTimestamp: Long? = null,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (fileSize > 0) (downloadedBytes.toFloat() / fileSize.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedSize: String
        get() {
            val bytes = if (status == STATUS_COMPLETED) fileSize else downloadedBytes
            val mb = bytes / (1024.0 * 1024.0)
            return if (mb >= 1000) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }

    val displaySubtitle: String
        get() = when {
            seriesName != null && seasonNumber != null && episodeNumber != null ->
                "$seriesName - S${seasonNumber}E${episodeNumber}"
            seriesName != null -> seriesName
            else -> mediaType
        }

    companion object {
        const val STATUS_QUEUED = "QUEUED"
        const val STATUS_DOWNLOADING = "DOWNLOADING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_PAUSED = "PAUSED"
    }
}

@Entity(tableName = "resume_points")
data class ResumePointEntity(
    @PrimaryKey val itemId: String,
    val positionTicks: Long,
    val durationTicks: Long,
    val lastWatchedTimestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    val progressFraction: Float
        get() = if (durationTicks > 0) (positionTicks.toFloat() / durationTicks.toFloat()).coerceIn(0f, 1f) else 0f
}

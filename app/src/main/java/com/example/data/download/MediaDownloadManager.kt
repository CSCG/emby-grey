package com.example.data.download

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadItemEntity
import com.example.data.model.EmbyItemDto
import com.example.data.repository.EmbyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MediaDownloadManager(
    private val context: Context,
    private val database: AppDatabase,
    private val repository: EmbyRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val downloadsFlow: Flow<List<DownloadItemEntity>> = database.downloadDao().getAllDownloads()

    fun getDownloadFlow(itemId: String): Flow<DownloadItemEntity?> {
        return database.downloadDao().getDownloadById(itemId)
    }

    suspend fun getDownload(itemId: String): DownloadItemEntity? = withContext(Dispatchers.IO) {
        database.downloadDao().getDownloadByIdSync(itemId)
    }

    fun startDownload(item: EmbyItemDto) {
        val downloadUrl = repository.buildStreamUrl(item.id)
        val targetDir = File(context.getExternalFilesDir(null), "downloads").apply { mkdirs() }
        val targetFile = File(targetDir, "${item.id}.mp4")

        scope.launch {
            val existing = database.downloadDao().getDownloadByIdSync(item.id)
            if (existing != null && existing.status == DownloadItemEntity.STATUS_COMPLETED && File(existing.localFilePath ?: "").exists()) {
                Log.d("DownloadManager", "Item already downloaded: ${item.name}")
                return@launch
            }

            val entity = DownloadItemEntity(
                id = item.id,
                title = item.name,
                mediaType = item.type,
                seriesName = item.seriesName,
                seasonNumber = item.parentIndexNumber,
                episodeNumber = item.indexNumber,
                overview = item.overview,
                durationMs = item.durationMinutes * 60 * 1000,
                fileSize = 0L,
                downloadedBytes = 0L,
                status = DownloadItemEntity.STATUS_DOWNLOADING,
                localFilePath = targetFile.absolutePath,
                posterUrl = repository.getImageUrl(item.id),
                downloadUrl = downloadUrl,
                addedTimestamp = System.currentTimeMillis()
            )
            database.downloadDao().insertOrUpdate(entity)

            val job = launch {
                downloadFile(item.id, downloadUrl, targetFile)
            }
            activeJobs[item.id] = job
        }
    }

    private suspend fun downloadFile(itemId: String, url: String, targetFile: File) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = downloadClient.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                database.downloadDao().updateCompletion(
                    id = itemId,
                    status = DownloadItemEntity.STATUS_FAILED,
                    localFilePath = null,
                    completionTimestamp = null,
                    errorMessage = "Server returned ${response.code}"
                )
                return@withContext
            }

            val body = response.body!!
            val totalBytes = body.contentLength()
            var downloaded = 0L

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var read: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 800) { // Throttle database progress updates to every 800ms
                            lastUpdate = now
                            database.downloadDao().updateProgress(
                                id = itemId,
                                downloadedBytes = downloaded,
                                fileSize = if (totalBytes > 0) totalBytes else downloaded,
                                status = DownloadItemEntity.STATUS_DOWNLOADING
                            )
                        }
                    }
                    output.flush()
                }
            }

            database.downloadDao().updateCompletion(
                id = itemId,
                status = DownloadItemEntity.STATUS_COMPLETED,
                localFilePath = targetFile.absolutePath,
                completionTimestamp = System.currentTimeMillis(),
                errorMessage = null
            )
            database.downloadDao().updateProgress(
                id = itemId,
                downloadedBytes = targetFile.length(),
                fileSize = targetFile.length(),
                status = DownloadItemEntity.STATUS_COMPLETED
            )
        } catch (e: Exception) {
            Log.e("DownloadManager", "Download error for $itemId", e)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            database.downloadDao().updateCompletion(
                id = itemId,
                status = DownloadItemEntity.STATUS_FAILED,
                localFilePath = null,
                completionTimestamp = null,
                errorMessage = e.localizedMessage ?: "Download interrupted"
            )
        } finally {
            activeJobs.remove(itemId)
        }
    }

    fun cancelOrDeleteDownload(itemId: String) {
        scope.launch {
            activeJobs[itemId]?.cancel()
            activeJobs.remove(itemId)
            val download = database.downloadDao().getDownloadByIdSync(itemId)
            if (download?.localFilePath != null) {
                val file = File(download.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            database.downloadDao().deleteDownload(itemId)
        }
    }

    fun getUsedStorageBytes(): Long {
        val targetDir = File(context.getExternalFilesDir(null), "downloads")
        if (!targetDir.exists()) return 0L
        return targetDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY addedTimestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadItemEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun getDownloadById(id: String): Flow<DownloadItemEntity?>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadByIdSync(id: String): DownloadItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadItemEntity)

    @Query("UPDATE downloads SET downloadedBytes = :downloadedBytes, fileSize = :fileSize, status = :status WHERE id = :id")
    suspend fun updateProgress(id: String, downloadedBytes: Long, fileSize: Long, status: String)

    @Query("UPDATE downloads SET status = :status, localFilePath = :localFilePath, completionTimestamp = :completionTimestamp, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateCompletion(id: String, status: String, localFilePath: String?, completionTimestamp: Long?, errorMessage: String?)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)
}

@Dao
interface ResumePointDao {
    @Query("SELECT * FROM resume_points WHERE itemId = :itemId LIMIT 1")
    fun getResumePoint(itemId: String): Flow<ResumePointEntity?>

    @Query("SELECT * FROM resume_points WHERE itemId = :itemId LIMIT 1")
    suspend fun getResumePointSync(itemId: String): ResumePointEntity?

    @Query("SELECT * FROM resume_points WHERE isCompleted = 0 AND positionTicks > 50000000 ORDER BY lastWatchedTimestamp DESC LIMIT 10")
    suspend fun getAllRecentResumePoints(): List<ResumePointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveResumePoint(resumePoint: ResumePointEntity)

    @Query("DELETE FROM resume_points WHERE itemId = :itemId")
    suspend fun clearResumePoint(itemId: String)
}

@Database(
    entities = [DownloadItemEntity::class, ResumePointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun resumePointDao(): ResumePointDao
}

package com.spowlo.musicdownloader.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for download history
 */
@Dao
interface DownloadDao {
    
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>
    
    @Query("SELECT * FROM downloads WHERE jobId = :jobId")
    suspend fun getDownloadById(jobId: String): DownloadEntity?
    
    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY timestamp DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)
    
    @Update
    suspend fun updateDownload(download: DownloadEntity)
    
    @Query("UPDATE downloads SET status = :status, progress = :progress, currentLine = :currentLine WHERE jobId = :jobId")
    suspend fun updateProgress(jobId: String, status: String, progress: Float, currentLine: String)
    
    @Query("UPDATE downloads SET status = :status, progress = :progress, currentLine = :currentLine, resultFile = :resultFile WHERE jobId = :jobId")
    suspend fun updateCompleted(jobId: String, status: String, progress: Float, currentLine: String, resultFile: String?)
    
    @Query("UPDATE downloads SET status = :status, error = :error WHERE jobId = :jobId")
    suspend fun updateFailed(jobId: String, status: String, error: String)
    
    @Delete
    suspend fun deleteDownload(download: DownloadEntity)
    
    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()
    
    @Query("SELECT COUNT(*) FROM downloads")
    suspend fun getDownloadCount(): Int
}

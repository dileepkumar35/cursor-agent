package com.spowlo.musicdownloader.data.repository

import com.spowlo.musicdownloader.data.local.DownloadDao
import com.spowlo.musicdownloader.data.local.DownloadEntity
import com.spowlo.musicdownloader.data.remote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * Repository for managing music downloads and metadata
 * Implements MVVM architecture pattern
 */
class MusicRepository(
    private val apiService: SpowloApiService,
    private val downloadDao: DownloadDao
) {
    
    // ===== Remote API Operations =====
    
    /**
     * Fetch metadata for a Spotify track
     */
    suspend fun getSpotifyMetadata(trackId: String): Result<TrackMetadata> {
        return handleApiCall { apiService.getSpotifyMetadata(trackId) }
    }
    
    /**
     * Fetch metadata for a JioSaavn song
     */
    suspend fun getJioSaavnMetadata(songId: String): Result<TrackMetadata> {
        return handleApiCall { apiService.getJioSaavnMetadata(songId) }
    }
    
    /**
     * Start a download job
     */
    suspend fun startDownload(request: DownloadRequest): Result<DownloadResponse> {
        return handleApiCall { apiService.startDownload(request) }
    }
    
    /**
     * Get job status
     */
    suspend fun getJobStatus(jobId: String): Result<JobProgressResponse> {
        return handleApiCall { apiService.getJobStatus(jobId) }
    }
    
    /**
     * Poll job status until completion
     */
    fun pollJobStatus(jobId: String): Flow<Result<JobProgressResponse>> = flow {
        while (true) {
            val result = getJobStatus(jobId)
            emit(result)
            
            // Check if job is terminal
            if (result.isSuccess) {
                val response = result.getOrNull()
                val status = response?.status
                if (status in listOf("completed", "failed", "cancelled")) {
                    break
                }
            } else {
                // If API call fails, emit error and stop polling
                break
            }
            
            // Wait before next poll
            kotlinx.coroutines.delay(1000) // Poll every second
        }
    }
    
    // ===== Local Database Operations =====
    
    /**
     * Get all downloads from history
     */
    fun getAllDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getAllDownloads()
    }
    
    /**
     * Get download by job ID
     */
    suspend fun getDownloadById(jobId: String): DownloadEntity? {
        return downloadDao.getDownloadById(jobId)
    }
    
    /**
     * Insert or update a download record
     */
    suspend fun insertDownload(download: DownloadEntity) {
        downloadDao.insertDownload(download)
    }
    
    /**
     * Update download progress
     */
    suspend fun updateDownloadProgress(
        jobId: String,
        status: String,
        progress: Float,
        currentLine: String
    ) {
        downloadDao.updateProgress(jobId, status, progress, currentLine)
    }
    
    /**
     * Update download as completed
     */
    suspend fun updateDownloadCompleted(
        jobId: String,
        status: String,
        progress: Float,
        currentLine: String,
        resultFile: String?
    ) {
        downloadDao.updateCompleted(jobId, status, progress, currentLine, resultFile)
    }
    
    /**
     * Update download as failed
     */
    suspend fun updateDownloadFailed(jobId: String, status: String, error: String) {
        downloadDao.updateFailed(jobId, status, error)
    }
    
    /**
     * Delete a download record
     */
    suspend fun deleteDownload(download: DownloadEntity) {
        downloadDao.deleteDownload(download)
    }
    
    /**
     * Clear all download history
     */
    suspend fun clearAllDownloads() {
        downloadDao.clearAllDownloads()
    }
    
    // ===== Helper Functions =====
    
    /**
     * Helper function to handle API calls and return Result
     */
    private suspend fun <T> handleApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Detect platform from URL
     */
    fun detectPlatform(url: String): Platform {
        return when {
            url.contains("spotify.com", ignoreCase = true) -> Platform.SPOTIFY
            url.contains("jiosaavn.com", ignoreCase = true) || 
            url.contains("saavn.com", ignoreCase = true) -> Platform.JIOSAAVN
            else -> Platform.YOUTUBE
        }
    }
    
    /**
     * Extract ID from URL based on platform
     */
    fun extractIdFromUrl(url: String, platform: Platform): String {
        return when (platform) {
            Platform.SPOTIFY -> {
                // Extract track ID from Spotify URL
                url.substringAfter("/track/").substringBefore("?")
            }
            Platform.JIOSAAVN -> {
                // Extract song ID from JioSaavn URL
                url.substringAfter("/song/").substringBefore("/")
            }
            else -> url // Return full URL for YouTube
        }
    }
}

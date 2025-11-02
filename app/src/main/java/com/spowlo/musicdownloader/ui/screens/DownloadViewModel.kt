package com.spowlo.musicdownloader.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spowlo.musicdownloader.data.repository.MusicRepository
import com.spowlo.musicdownloader.domain.DownloadItem
import com.spowlo.musicdownloader.domain.DownloadStatus
import com.spowlo.musicdownloader.util.toDomainModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Download Progress screen
 * Monitors active downloads and updates their status
 */
class DownloadViewModel(
    private val repository: MusicRepository
) : ViewModel() {
    
    // Active downloads (processing or pending)
    private val _activeDownloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val activeDownloads: StateFlow<List<DownloadItem>> = _activeDownloads.asStateFlow()
    
    init {
        // Observe all downloads from database
        viewModelScope.launch {
            repository.getAllDownloads()
                .map { entities -> 
                    entities
                        .filter { it.status in listOf("pending", "processing") }
                        .map { it.toDomainModel() }
                }
                .collect { downloads ->
                    _activeDownloads.value = downloads
                    
                    // Start polling for each active download
                    downloads.forEach { download ->
                        if (download.status in listOf(DownloadStatus.PENDING, DownloadStatus.PROCESSING)) {
                            pollDownloadProgress(download.jobId)
                        }
                    }
                }
        }
    }
    
    /**
     * Poll download progress for a specific job
     */
    private fun pollDownloadProgress(jobId: String) {
        viewModelScope.launch {
            repository.pollJobStatus(jobId).collect { result ->
                result.onSuccess { response ->
                    // Update database with latest progress
                    when (response.status.lowercase()) {
                        "completed" -> {
                            repository.updateDownloadCompleted(
                                jobId = jobId,
                                status = response.status,
                                progress = response.progress,
                                currentLine = response.currentLine,
                                resultFile = response.resultFile
                            )
                        }
                        "failed" -> {
                            repository.updateDownloadFailed(
                                jobId = jobId,
                                status = response.status,
                                error = response.error ?: "Unknown error"
                            )
                        }
                        else -> {
                            repository.updateDownloadProgress(
                                jobId = jobId,
                                status = response.status,
                                progress = response.progress,
                                currentLine = response.currentLine
                            )
                        }
                    }
                }.onFailure { error ->
                    // If polling fails, mark as failed
                    repository.updateDownloadFailed(
                        jobId = jobId,
                        status = "failed",
                        error = "Failed to fetch status: ${error.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Retry a failed download
     */
    fun retryDownload(jobId: String) {
        viewModelScope.launch {
            val download = repository.getDownloadById(jobId)
            if (download != null) {
                // Reset status to pending and restart polling
                repository.updateDownloadProgress(
                    jobId = jobId,
                    status = "pending",
                    progress = 0f,
                    currentLine = "Retrying download..."
                )
                pollDownloadProgress(jobId)
            }
        }
    }
    
    /**
     * Cancel a download
     */
    fun cancelDownload(jobId: String) {
        viewModelScope.launch {
            repository.updateDownloadProgress(
                jobId = jobId,
                status = "cancelled",
                progress = 0f,
                currentLine = "Download cancelled"
            )
        }
    }
}

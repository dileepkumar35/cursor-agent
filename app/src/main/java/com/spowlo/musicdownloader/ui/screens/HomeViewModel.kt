package com.spowlo.musicdownloader.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spowlo.musicdownloader.data.local.DownloadEntity
import com.spowlo.musicdownloader.data.remote.*
import com.spowlo.musicdownloader.data.repository.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Home/Search screen
 * Handles URL input, metadata fetching, and download initiation
 */
class HomeViewModel(
    private val repository: MusicRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    /**
     * Update URL input
     */
    fun updateUrl(url: String) {
        _uiState.update { it.copy(url = url, error = null) }
    }
    
    /**
     * Update selected quality
     */
    fun updateQuality(quality: Quality) {
        _uiState.update { it.copy(selectedQuality = quality) }
    }
    
    /**
     * Fetch metadata for the entered URL
     */
    fun fetchMetadata() {
        val url = _uiState.value.url.trim()
        
        if (url.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a URL") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val platform = repository.detectPlatform(url)
                val id = repository.extractIdFromUrl(url, platform)
                
                val result = when (platform) {
                    Platform.SPOTIFY -> repository.getSpotifyMetadata(id)
                    Platform.JIOSAAVN -> repository.getJioSaavnMetadata(id)
                    else -> {
                        // For YouTube, we can't fetch metadata without backend support
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "YouTube metadata fetching not supported. Please proceed with download."
                            )
                        }
                        return@launch
                    }
                }
                
                result.onSuccess { metadata ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            metadata = metadata,
                            detectedPlatform = platform,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to fetch metadata: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Start download with selected quality
     */
    fun startDownload() {
        val url = _uiState.value.url.trim()
        val quality = _uiState.value.selectedQuality
        val metadata = _uiState.value.metadata
        
        if (url.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a URL") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val request = DownloadRequest(
                    url = url,
                    quality = quality.value,
                    metadata = metadata
                )
                
                val result = repository.startDownload(request)
                
                result.onSuccess { response ->
                    // Save to local database
                    val downloadEntity = DownloadEntity(
                        jobId = response.jobId,
                        title = metadata?.title ?: "Unknown",
                        artist = metadata?.artists?.joinToString(", ") ?: "Unknown",
                        album = metadata?.album,
                        coverImageUrl = metadata?.thumbnailUrl,
                        quality = quality.value,
                        platform = metadata?.platform ?: repository.detectPlatform(url).value,
                        status = "pending",
                        progress = 0f,
                        currentLine = "Download started",
                        downloadUrl = url
                    )
                    repository.insertDownload(downloadEntity)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            downloadStarted = true,
                            jobId = response.jobId,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to start download: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Reset state after download started
     */
    fun resetAfterDownload() {
        _uiState.update { 
            HomeUiState(
                selectedQuality = it.selectedQuality // Keep quality selection
            )
        }
    }
}

/**
 * UI State for Home screen
 */
data class HomeUiState(
    val url: String = "",
    val selectedQuality: Quality = Quality.M4A_320,
    val metadata: TrackMetadata? = null,
    val detectedPlatform: Platform? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val downloadStarted: Boolean = false,
    val jobId: String? = null
)

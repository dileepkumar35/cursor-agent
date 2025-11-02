package com.spowlo.musicdownloader.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spowlo.musicdownloader.data.repository.MusicRepository
import com.spowlo.musicdownloader.domain.DownloadItem
import com.spowlo.musicdownloader.util.toDomainModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for History screen
 * Displays download history and allows management of records
 */
class HistoryViewModel(
    private val repository: MusicRepository
) : ViewModel() {
    
    // All downloads from history
    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadHistory()
    }
    
    /**
     * Load download history from database
     */
    private fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllDownloads()
                .map { entities -> entities.map { it.toDomainModel() } }
                .collect { downloadItems ->
                    _downloads.value = downloadItems
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Delete a download record
     */
    fun deleteDownload(downloadItem: DownloadItem) {
        viewModelScope.launch {
            val entity = repository.getDownloadById(downloadItem.jobId)
            if (entity != null) {
                repository.deleteDownload(entity)
            }
        }
    }
    
    /**
     * Clear all download history
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllDownloads()
        }
    }
    
    /**
     * Filter downloads by status
     */
    fun filterByStatus(status: String?): List<DownloadItem> {
        return if (status == null) {
            _downloads.value
        } else {
            _downloads.value.filter { it.status.name.lowercase() == status.lowercase() }
        }
    }
    
    /**
     * Search downloads by title or artist
     */
    fun searchDownloads(query: String): List<DownloadItem> {
        if (query.isBlank()) return _downloads.value
        
        val lowerQuery = query.lowercase()
        return _downloads.value.filter { 
            it.title.lowercase().contains(lowerQuery) || 
            it.artist.lowercase().contains(lowerQuery)
        }
    }
}

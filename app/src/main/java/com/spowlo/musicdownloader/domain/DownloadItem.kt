package com.spowlo.musicdownloader.domain

/**
 * Domain model for a download item
 * Used in UI layer to display download information
 */
data class DownloadItem(
    val jobId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val coverImageUrl: String?,
    val quality: String,
    val platform: String,
    val status: DownloadStatus,
    val progress: Float = 0f,
    val currentLine: String = "",
    val error: String? = null,
    val resultFile: String? = null,
    val timestamp: Long
)

/**
 * Download status enum
 */
enum class DownloadStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;
    
    companion object {
        fun fromString(status: String): DownloadStatus {
            return when (status.lowercase()) {
                "pending" -> PENDING
                "processing" -> PROCESSING
                "completed" -> COMPLETED
                "failed" -> FAILED
                "cancelled" -> CANCELLED
                else -> PENDING
            }
        }
    }
}

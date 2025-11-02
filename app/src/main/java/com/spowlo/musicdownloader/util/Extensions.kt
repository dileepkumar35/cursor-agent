package com.spowlo.musicdownloader.util

import com.spowlo.musicdownloader.data.local.DownloadEntity
import com.spowlo.musicdownloader.domain.DownloadItem
import com.spowlo.musicdownloader.domain.DownloadStatus

/**
 * Extension functions for converting between data and domain models
 */

/**
 * Convert DownloadEntity to DownloadItem (domain model)
 */
fun DownloadEntity.toDomainModel(): DownloadItem {
    return DownloadItem(
        jobId = jobId,
        title = title,
        artist = artist,
        album = album,
        coverImageUrl = coverImageUrl,
        quality = quality,
        platform = platform,
        status = DownloadStatus.fromString(status),
        progress = progress,
        currentLine = currentLine,
        error = error,
        resultFile = resultFile,
        timestamp = timestamp
    )
}

/**
 * Convert DownloadItem to DownloadEntity (database model)
 */
fun DownloadItem.toEntity(downloadUrl: String): DownloadEntity {
    return DownloadEntity(
        jobId = jobId,
        title = title,
        artist = artist,
        album = album,
        coverImageUrl = coverImageUrl,
        quality = quality,
        platform = platform,
        status = status.name.lowercase(),
        progress = progress,
        currentLine = currentLine,
        error = error,
        resultFile = resultFile,
        downloadUrl = downloadUrl,
        timestamp = timestamp
    )
}

/**
 * Format duration in seconds to MM:SS format
 */
fun Int.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Format file size in bytes to human-readable format
 */
fun Long.formatFileSize(): String {
    if (this < 1024) return "$this B"
    val kb = this / 1024.0
    if (kb < 1024) return String.format("%.2f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.2f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.2f GB", gb)
}

/**
 * Validate URL format
 */
fun String.isValidUrl(): Boolean {
    return this.startsWith("http://") || this.startsWith("https://")
}

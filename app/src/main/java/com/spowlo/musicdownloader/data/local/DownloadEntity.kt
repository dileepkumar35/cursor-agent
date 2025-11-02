package com.spowlo.musicdownloader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a download record in the history
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val jobId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val coverImageUrl: String?,
    val quality: String,
    val platform: String,
    val status: String, // pending, processing, completed, failed
    val progress: Float = 0f,
    val currentLine: String = "",
    val error: String? = null,
    val resultFile: String? = null,
    val downloadUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

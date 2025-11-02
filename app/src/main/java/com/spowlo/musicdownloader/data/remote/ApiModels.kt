package com.spowlo.musicdownloader.data.remote

import com.google.gson.annotations.SerializedName

/**
 * API request and response models
 */

// Track Metadata
data class TrackMetadata(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artists") val artists: List<String>,
    @SerializedName("album") val album: String?,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("platform") val platform: String
)

// Download Request
data class DownloadRequest(
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String,
    @SerializedName("metadata") val metadata: TrackMetadata? = null
)

// Download Response
data class DownloadResponse(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// Job Progress Response
data class JobProgressResponse(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("status") val status: String,
    @SerializedName("progress") val progress: Float,
    @SerializedName("current_line") val currentLine: String,
    @SerializedName("error") val error: String?,
    @SerializedName("result_file") val resultFile: String?
)

// Quality enum
enum class Quality(val value: String) {
    M4A_320("m4a_320"),
    OPUS_160("opus_160"),
    BEST("best")
}

// Platform enum
enum class Platform(val value: String) {
    SPOTIFY("spotify"),
    JIOSAAVN("jiosaavn"),
    YOUTUBE("youtube")
}

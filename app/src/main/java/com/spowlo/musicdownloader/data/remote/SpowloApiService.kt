package com.spowlo.musicdownloader.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service for Spowlo Music Downloader backend
 */
interface SpowloApiService {
    
    /**
     * Health check endpoint
     */
    @GET("/")
    suspend fun healthCheck(): Response<Map<String, Any>>
    
    /**
     * Get metadata for a Spotify track
     * @param trackId Spotify track ID or full URL
     */
    @GET("/api/metadata/spotify/{track_id}")
    suspend fun getSpotifyMetadata(
        @Path("track_id") trackId: String
    ): Response<TrackMetadata>
    
    /**
     * Get metadata for a JioSaavn song
     * @param songId JioSaavn song ID or full URL
     */
    @GET("/api/metadata/jiosaavn/{song_id}")
    suspend fun getJioSaavnMetadata(
        @Path("song_id") songId: String
    ): Response<TrackMetadata>
    
    /**
     * Start a download job
     * @param request Download request with URL, quality, and optional metadata
     */
    @POST("/api/download")
    suspend fun startDownload(
        @Body request: DownloadRequest
    ): Response<DownloadResponse>
    
    /**
     * Get status of a download job
     * @param jobId Job ID returned from startDownload
     */
    @GET("/api/job/{job_id}")
    suspend fun getJobStatus(
        @Path("job_id") jobId: String
    ): Response<JobProgressResponse>
}

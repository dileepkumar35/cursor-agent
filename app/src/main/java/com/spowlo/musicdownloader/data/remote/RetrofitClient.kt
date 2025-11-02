package com.spowlo.musicdownloader.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client builder for Spowlo API
 */
object RetrofitClient {
    
    // Default base URL - can be overridden
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/" // Android emulator localhost
    
    private var baseUrl: String = DEFAULT_BASE_URL
    
    /**
     * Set custom base URL for the API
     */
    fun setBaseUrl(url: String) {
        baseUrl = url.let { if (it.endsWith("/")) it else "$it/" }
    }
    
    /**
     * OkHttp client with logging and timeout configuration
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Get API service instance
     */
    val apiService: SpowloApiService by lazy {
        retrofit.create(SpowloApiService::class.java)
    }
}

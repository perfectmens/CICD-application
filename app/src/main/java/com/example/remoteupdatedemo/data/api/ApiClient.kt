package com.example.remoteupdatedemo.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton network factory managing Retrofit client instances, base URLs,
 * and OkHttp connection parameters.
 */
object ApiClient {

    /**
     * Default base URL pointing to the Docker backend container running on host port 8080
     * reachable across the LAN at 192.168.68.64.
     */
    const val DEFAULT_BASE_URL = "http://192.168.68.64:8080/"
    const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL

    @Volatile
    private var currentService: UpdateApiService? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Returns an active UpdateApiService bound to the current base URL.
     */
    fun getService(baseUrl: String = currentBaseUrl): UpdateApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (currentService == null || currentBaseUrl != normalizedUrl) {
            synchronized(this) {
                if (currentService == null || currentBaseUrl != normalizedUrl) {
                    currentBaseUrl = normalizedUrl
                    val retrofit = Retrofit.Builder()
                        .baseUrl(normalizedUrl)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    currentService = retrofit.create(UpdateApiService::class.java)
                }
            }
        }
        return currentService!!
    }

    /**
     * Dynamically updates the base URL (useful when switching between emulator and physical device IP).
     */
    fun setBaseUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        val normalized = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        synchronized(this) {
            currentBaseUrl = normalized
            currentService = null
        }
    }

    fun getBaseUrl(): String = currentBaseUrl
}

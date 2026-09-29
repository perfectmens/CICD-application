package com.example.remoteupdatedemo.data.api

import com.example.remoteupdatedemo.data.api.dto.HealthResponseDto
import com.example.remoteupdatedemo.data.api.dto.UpdateCheckResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit contract interface matching endpoints in api-mapping.yaml.
 */
interface UpdateApiService {

    /**
     * Contract ID: version.update.check
     * GET /api/v1/version/check?current_version={version}&version_code={code}
     */
    @GET("api/v1/version/check")
    suspend fun checkForUpdate(
        @Query("current_version") currentVersion: String,
        @Query("version_code") versionCode: Int
    ): UpdateCheckResponseDto

    /**
     * Contract ID: system.health.check
     * GET /api/v1/health
     */
    @GET("api/v1/health")
    suspend fun checkHealth(): HealthResponseDto
}

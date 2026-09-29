package com.example.remoteupdatedemo.data.repository

import com.example.remoteupdatedemo.data.api.ApiClient
import com.example.remoteupdatedemo.data.api.UpdateApiService
import com.example.remoteupdatedemo.data.api.dto.HealthResponseDto
import com.example.remoteupdatedemo.data.api.dto.UpdateCheckResponseDto
import com.example.remoteupdatedemo.data.model.ServerHealth
import com.example.remoteupdatedemo.data.model.UpdateInfo

/**
 * Concrete implementation of UpdateRepository.
 * Maps wire DTOs to clean domain entities and provides clean exception abstraction.
 */
class UpdateRepositoryImpl(
    private val apiServiceProvider: () -> UpdateApiService = { ApiClient.getService() }
) : UpdateRepository {

    override suspend fun checkForUpdate(
        currentVersionCode: Int,
        currentVersionName: String
    ): Result<UpdateInfo> {
        return runCatching {
            val responseDto: UpdateCheckResponseDto = apiServiceProvider().checkForUpdate(
                currentVersion = currentVersionName,
                versionCode = currentVersionCode
            )
            responseDto.toDomain()
        }
    }

    override suspend fun checkServerHealth(): Result<ServerHealth> {
        return runCatching {
            val healthDto: HealthResponseDto = apiServiceProvider().checkHealth()
            healthDto.toDomain()
        }
    }

    override fun updateBaseUrl(newUrl: String) {
        ApiClient.setBaseUrl(newUrl)
    }

    override fun getBaseUrl(): String {
        return ApiClient.getBaseUrl()
    }
}

/**
 * DTO to Domain model mappers.
 */
internal fun UpdateCheckResponseDto.toDomain(): UpdateInfo = UpdateInfo(
    latestVersionName = latestVersionName,
    latestVersionCode = latestVersionCode,
    hasUpdate = hasUpdate,
    isMandatory = isMandatory,
    downloadUrl = downloadUrl,
    sha256 = sha256,
    releaseNotes = releaseNotes,
    publishedAt = publishedAt
)

internal fun HealthResponseDto.toDomain(): ServerHealth = ServerHealth(
    isHealthy = status.equals("ok", ignoreCase = true),
    service = service,
    version = version,
    timestamp = timestamp
)

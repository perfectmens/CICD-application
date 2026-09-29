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

    override suspend fun downloadUpdateApk(
        downloadUrl: String,
        destinationFile: java.io.File,
        expectedSha256: String?,
        onProgress: (Float) -> Unit
    ): Result<java.io.File> {
        val downloader = com.example.remoteupdatedemo.data.api.ApkDownloader()
        return downloader.downloadApk(
            downloadUrl = downloadUrl,
            destinationFile = destinationFile,
            expectedSha256 = expectedSha256,
            onProgress = onProgress
        )
    }

    override suspend fun getRandomGreetings(count: Int): Result<List<com.example.remoteupdatedemo.data.model.Greeting>> {
        return runCatching {
            val responseDto = apiServiceProvider().getRandomGreetings(count = count)
            responseDto.greetings.map { it.toDomain() }
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

internal fun com.example.remoteupdatedemo.data.api.dto.GreetingItemDto.toDomain(): com.example.remoteupdatedemo.data.model.Greeting =
    com.example.remoteupdatedemo.data.model.Greeting(
        id = id,
        text = text,
        category = category,
        emoji = emoji
    )


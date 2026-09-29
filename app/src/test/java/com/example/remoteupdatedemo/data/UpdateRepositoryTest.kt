package com.example.remoteupdatedemo.data

import com.example.remoteupdatedemo.data.api.UpdateApiService
import com.example.remoteupdatedemo.data.api.dto.HealthResponseDto
import com.example.remoteupdatedemo.data.api.dto.UpdateCheckResponseDto
import com.example.remoteupdatedemo.data.repository.UpdateRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRepositoryTest {

    private class FakeApiService(
        var checkResponse: UpdateCheckResponseDto? = null,
        var healthResponse: HealthResponseDto? = null,
        var shouldThrow: Boolean = false
    ) : UpdateApiService {
        override suspend fun checkForUpdate(currentVersion: String, versionCode: Int): UpdateCheckResponseDto {
            if (shouldThrow) throw RuntimeException("Network timeout simulation")
            return checkResponse ?: UpdateCheckResponseDto(
                latestVersionName = "0.0.1",
                latestVersionCode = 1,
                hasUpdate = false,
                isMandatory = false,
                downloadUrl = "http://localhost:8080/api/v1/updates/download/latest.apk",
                sha256 = "dummy_sha",
                releaseNotes = "No update",
                publishedAt = "2026-09-29T10:00:00Z"
            )
        }

        override suspend fun checkHealth(): HealthResponseDto {
            if (shouldThrow) throw RuntimeException("Service unavailable")
            return healthResponse ?: HealthResponseDto(
                status = "ok",
                service = "remote-update-server",
                timestamp = "2026-09-29T10:00:00Z",
                version = "1.0.0"
            )
        }
    }

    @Test
    fun checkForUpdate_success_mapsDtoToDomainCorrectly() = runTest {
        val fakeService = FakeApiService(
            checkResponse = UpdateCheckResponseDto(
                latestVersionName = "0.0.2",
                latestVersionCode = 2,
                hasUpdate = true,
                isMandatory = false,
                downloadUrl = "http://example.com/apk",
                sha256 = "abc123sha",
                releaseNotes = "New features",
                publishedAt = "2026-09-29T12:00:00Z"
            )
        )
        val repository = UpdateRepositoryImpl(apiServiceProvider = { fakeService })

        val result = repository.checkForUpdate(currentVersionCode = 1, currentVersionName = "0.0.1")

        assertTrue(result.isSuccess)
        val updateInfo = result.getOrThrow()
        assertEquals("0.0.2", updateInfo.latestVersionName)
        assertEquals(2, updateInfo.latestVersionCode)
        assertTrue(updateInfo.hasUpdate)
        assertEquals("New features", updateInfo.releaseNotes)
    }

    @Test
    fun checkForUpdate_networkFailure_returnsFailureResult() = runTest {
        val fakeService = FakeApiService(shouldThrow = true)
        val repository = UpdateRepositoryImpl(apiServiceProvider = { fakeService })

        val result = repository.checkForUpdate(currentVersionCode = 1, currentVersionName = "0.0.1")

        assertTrue(result.isFailure)
        assertEquals("Network timeout simulation", result.exceptionOrNull()?.message)
    }

    @Test
    fun checkServerHealth_success_mapsToHealthyDomain() = runTest {
        val fakeService = FakeApiService(
            healthResponse = HealthResponseDto(
                status = "ok",
                service = "remote-update-server",
                timestamp = "2026-09-29T10:00:00Z",
                version = "1.0.0"
            )
        )
        val repository = UpdateRepositoryImpl(apiServiceProvider = { fakeService })

        val result = repository.checkServerHealth()

        assertTrue(result.isSuccess)
        val health = result.getOrThrow()
        assertTrue(health.isHealthy)
        assertEquals("remote-update-server", health.service)
    }
}

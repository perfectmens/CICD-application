package com.example.remoteupdatedemo.ui

import com.example.remoteupdatedemo.data.model.Greeting
import com.example.remoteupdatedemo.data.model.ServerHealth
import com.example.remoteupdatedemo.data.model.UpdateInfo
import com.example.remoteupdatedemo.data.repository.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeRepository(
        var checkResult: Result<UpdateInfo> = Result.success(
            UpdateInfo(
                latestVersionName = "0.0.1",
                latestVersionCode = 1,
                hasUpdate = false,
                isMandatory = false,
                downloadUrl = "http://localhost:8080/api/v1/updates/download/latest.apk",
                sha256 = "test_sha",
                releaseNotes = "Initial version",
                publishedAt = "2026-09-29T10:00:00Z"
            )
        ),
        var healthResult: Result<ServerHealth> = Result.success(
            ServerHealth(
                isHealthy = true,
                service = "remote-update-server",
                version = "1.0.0",
                timestamp = "2026-09-29T10:00:00Z"
            )
        ),
        var greetingsResult: Result<List<Greeting>> = Result.success(
            listOf(
                Greeting(1, "Hello from Docker", "Welcome", "👋"),
                Greeting(2, "Test greeting", "Testing", "🧪")
            )
        ),
        private var currentUrl: String = "http://10.0.2.2:8080/"
    ) : UpdateRepository {
        override suspend fun checkForUpdate(currentVersionCode: Int, currentVersionName: String): Result<UpdateInfo> {
            return checkResult
        }
        override suspend fun checkServerHealth(): Result<ServerHealth> {
            return healthResult
        }
        override suspend fun getRandomGreetings(count: Int): Result<List<Greeting>> {
            return greetingsResult
        }
        override fun updateBaseUrl(newUrl: String) {
            currentUrl = newUrl
        }
        override fun getBaseUrl(): String = currentUrl

        override suspend fun downloadUpdateApk(
            downloadUrl: String,
            destinationFile: java.io.File,
            expectedSha256: String?,
            onProgress: (Float) -> Unit
        ): Result<java.io.File> {
            onProgress(1.0f)
            return Result.success(destinationFile)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasCorrectVersionsAndIdleStatus() {
        val repo = FakeRepository()
        val viewModel = MainViewModel(
            repository = repo,
            initialVersionName = "0.0.1",
            initialVersionCode = 1
        )

        val state = viewModel.uiState.value
        assertEquals("0.0.1", state.currentVersionName)
        assertEquals(1, state.currentVersionCode)
        assertEquals(UpdateStatus.Idle, state.updateStatus)
        assertEquals("http://10.0.2.2:8080/", state.backendUrl)
    }

    @Test
    fun checkForUpdate_whenUpToDate_setsUpToDateStatus() = runTest {
        val repo = FakeRepository(
            checkResult = Result.success(
                UpdateInfo(
                    latestVersionName = "0.0.1",
                    latestVersionCode = 1,
                    hasUpdate = false,
                    isMandatory = false,
                    downloadUrl = "http://localhost:8080/download",
                    sha256 = "sha",
                    releaseNotes = "No update",
                    publishedAt = "2026-09-29T10:00:00Z"
                )
            )
        )
        val viewModel = MainViewModel(repo, "0.0.1", 1)
        advanceUntilIdle()

        viewModel.checkForUpdate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.updateStatus is UpdateStatus.UpToDate)
        val upToDate = state.updateStatus as UpdateStatus.UpToDate
        assertEquals("Remote update functionality will be added later.", upToDate.message)
        assertEquals("0.0.1", upToDate.latestVersionName)
    }

    @Test
    fun checkForUpdate_whenNewerVersionExists_setsUpdateAvailableStatus() = runTest {
        val repo = FakeRepository(
            checkResult = Result.success(
                UpdateInfo(
                    latestVersionName = "0.0.2",
                    latestVersionCode = 2,
                    hasUpdate = true,
                    isMandatory = false,
                    downloadUrl = "http://localhost:8080/download",
                    sha256 = "sha2",
                    releaseNotes = "Version 0.0.2 ready for release testing.",
                    publishedAt = "2026-09-29T12:00:00Z"
                )
            )
        )
        val viewModel = MainViewModel(repo, "0.0.1", 1)
        advanceUntilIdle()

        viewModel.checkForUpdate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.updateStatus is UpdateStatus.UpdateAvailable)
        val update = state.updateStatus as UpdateStatus.UpdateAvailable
        assertEquals("0.0.2", update.updateInfo.latestVersionName)
        assertEquals(2, update.updateInfo.latestVersionCode)
    }

    @Test
    fun checkForUpdate_whenNetworkFails_setsErrorStatus() = runTest {
        val repo = FakeRepository(
            checkResult = Result.failure(RuntimeException("Connection refused"))
        )
        val viewModel = MainViewModel(repo, "0.0.1", 1)
        advanceUntilIdle()

        viewModel.checkForUpdate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.updateStatus is UpdateStatus.Error)
        val error = state.updateStatus as UpdateStatus.Error
        assertTrue(error.message.contains("Remote update functionality will be added later"))
    }

    @Test
    fun fetchGreetings_onSuccess_populatesGreetingsList() = runTest {
        val repo = FakeRepository(
            greetingsResult = Result.success(
                listOf(
                    Greeting(1, "Broadcast 1", "System", "🐳"),
                    Greeting(2, "Broadcast 2", "DevOps", "🚀")
                )
            )
        )
        val viewModel = MainViewModel(repo, "0.0.1", 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.greetings.size)
        assertEquals("Broadcast 1", state.greetings[0].text)
        assertEquals(false, state.isLoadingGreetings)
        assertEquals(null, state.greetingsError)
    }

    @Test
    fun fetchGreetings_onFailure_setsGreetingsError() = runTest {
        val repo = FakeRepository(
            greetingsResult = Result.failure(RuntimeException("Docker backend timeout"))
        )
        val viewModel = MainViewModel(repo, "0.0.1", 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.greetings.isEmpty())
        assertEquals(false, state.isLoadingGreetings)
        assertEquals("Docker backend timeout", state.greetingsError)
    }
}


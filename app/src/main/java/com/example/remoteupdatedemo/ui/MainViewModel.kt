package com.example.remoteupdatedemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.remoteupdatedemo.BuildConfig
import com.example.remoteupdatedemo.data.repository.UpdateRepository
import com.example.remoteupdatedemo.data.repository.UpdateRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MainViewModel manages presentation logic and screen state for the Remote Update Demo screen.
 * Strictly adheres to mobile-mvvm-architecture:
 * - Zero HTTP package imports
 * - Zero raw API URLs
 * - Interacts exclusively with UpdateRepository
 */
class MainViewModel(
    private val repository: UpdateRepository = UpdateRepositoryImpl(),
    initialVersionName: String = BuildConfig.VERSION_NAME,
    initialVersionCode: Int = BuildConfig.VERSION_CODE
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            currentVersionName = initialVersionName,
            currentVersionCode = initialVersionCode,
            backendUrl = repository.getBaseUrl()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkBackendHealth()
        fetchGreetings()
    }

    /**
     * Fetches 10 random broadcast greetings from the backend.
     */
    fun fetchGreetings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGreetings = true, greetingsError = null) }
            val result = repository.getRandomGreetings(count = 10)
            result.fold(
                onSuccess = { greetingsList ->
                    _uiState.update {
                        it.copy(
                            greetings = greetingsList,
                            isLoadingGreetings = false,
                            greetingsError = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingGreetings = false,
                            greetingsError = error.localizedMessage ?: "Failed to fetch greetings"
                        )
                    }
                }
            )
        }
    }

    /**
     * Checks if the backend server is reachable and operational.
     */
    fun checkBackendHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(backendHealth = BackendHealthState.Checking) }
            val result = repository.checkServerHealth()
            result.fold(
                onSuccess = { health ->
                    _uiState.update {
                        it.copy(
                            backendHealth = BackendHealthState.Connected(
                                service = health.service,
                                version = health.version
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            backendHealth = BackendHealthState.Disconnected(
                                reason = error.localizedMessage ?: "Unable to connect to backend"
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * User action: Check for Update button pressed.
     */
    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.update { it.copy(updateStatus = UpdateStatus.Loading) }

            val currentCode = _uiState.value.currentVersionCode
            val currentName = _uiState.value.currentVersionName

            val result = repository.checkForUpdate(
                currentVersionCode = currentCode,
                currentVersionName = currentName
            )

            result.fold(
                onSuccess = { updateInfo ->
                    // App successfully communicated with backend
                    _uiState.update { state ->
                        state.copy(
                            backendHealth = BackendHealthState.Connected(
                                service = "remote-update-server",
                                version = "1.0.0"
                            ),
                            updateStatus = if (updateInfo.hasUpdate) {
                                UpdateStatus.UpdateAvailable(updateInfo)
                            } else {
                                UpdateStatus.UpToDate(
                                    message = "Great — you are on the latest version! Check back after the next GitHub Actions build.",
                                    latestVersionName = updateInfo.latestVersionName,
                                    latestVersionCode = updateInfo.latestVersionCode
                                )
                            }
                        )
                    }
                },
                onFailure = { error ->
                    // Backend offline or network failure
                    _uiState.update { state ->
                        state.copy(
                            backendHealth = BackendHealthState.Disconnected(
                                reason = error.localizedMessage ?: "Network unreachable"
                            ),
                            updateStatus = UpdateStatus.Error(
                                message = "Cannot check for updates — backend server is offline or unreachable.",
                                technicalDetail = error.localizedMessage
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Downloads the APK update from GitHub Releases or backend server.
     */
    fun startUpdateDownload(destinationFile: java.io.File, updateInfo: com.example.remoteupdatedemo.data.model.UpdateInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(updateStatus = UpdateStatus.Downloading(0f, updateInfo)) }
            val result = repository.downloadUpdateApk(
                downloadUrl = updateInfo.downloadUrl,
                destinationFile = destinationFile,
                expectedSha256 = updateInfo.sha256,
                onProgress = { progress ->
                    _uiState.update {
                        it.copy(updateStatus = UpdateStatus.Downloading(progress, updateInfo))
                    }
                }
            )
            result.fold(
                onSuccess = { file ->
                    _uiState.update {
                        it.copy(updateStatus = UpdateStatus.Downloaded(file, updateInfo))
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            updateStatus = UpdateStatus.Error(
                                message = "Failed to download update APK: ${error.localizedMessage}",
                                technicalDetail = error.message
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Resets the update status back to Idle.
     */
    fun resetUpdateStatus() {
        _uiState.update { it.copy(updateStatus = UpdateStatus.Idle) }
    }

    /**
     * Updates the backend target URL and re-probes connectivity.
     */
    fun updateBackendUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        if (trimmed.isNotBlank()) {
            repository.updateBaseUrl(trimmed)
            _uiState.update {
                it.copy(
                    backendUrl = repository.getBaseUrl(),
                    isSettingsDialogOpen = false,
                    updateStatus = UpdateStatus.Idle
                )
            }
            checkBackendHealth()
        }
    }

    fun openSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = true) }
    }

    fun dismissSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = false) }
    }

    /**
     * Factory for creating MainViewModel instances with custom dependencies (e.g. for testing).
     */
    companion object {
        fun provideFactory(
            repository: UpdateRepository = UpdateRepositoryImpl(),
            versionName: String = BuildConfig.VERSION_NAME,
            versionCode: Int = BuildConfig.VERSION_CODE
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, versionName, versionCode) as T
            }
        }
    }
}

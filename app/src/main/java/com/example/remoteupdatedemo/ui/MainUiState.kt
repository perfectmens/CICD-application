package com.example.remoteupdatedemo.ui

import com.example.remoteupdatedemo.data.model.Greeting
import com.example.remoteupdatedemo.data.model.UpdateInfo

/**
 * Sealed hierarchy of update check operation states.
 */
sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Loading : UpdateStatus
    data class UpToDate(
        val message: String = "Remote update functionality will be added later.",
        val latestVersionName: String,
        val latestVersionCode: Int
    ) : UpdateStatus
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateStatus
    data class Downloading(val progress: Float, val updateInfo: UpdateInfo) : UpdateStatus
    data class Downloaded(val file: java.io.File, val updateInfo: UpdateInfo) : UpdateStatus
    data class Installing(val file: java.io.File) : UpdateStatus
    data class Error(val message: String, val technicalDetail: String? = null) : UpdateStatus
}

/**
 * Backend server connection health status.
 */
sealed interface BackendHealthState {
    object Checking : BackendHealthState
    data class Connected(val service: String, val version: String) : BackendHealthState
    data class Disconnected(val reason: String) : BackendHealthState
}

/**
 * Screen state owned entirely by MainViewModel.
 */
data class MainUiState(
    val appTitle: String = "Remote Update Demo",
    val appDescription: String = "This application is a sample project used to learn Android CI/CD and remote APK updates.",
    val currentVersionName: String,
    val currentVersionCode: Int,
    val backendUrl: String,
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
    val backendHealth: BackendHealthState = BackendHealthState.Checking,
    val isSettingsDialogOpen: Boolean = false,
    val greetings: List<Greeting> = emptyList(),
    val isLoadingGreetings: Boolean = false,
    val greetingsError: String? = null
)

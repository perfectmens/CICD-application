package com.example.remoteupdatedemo.data.repository

import com.example.remoteupdatedemo.data.model.ServerHealth
import com.example.remoteupdatedemo.data.model.UpdateInfo

/**
 * Repository interface governing all version checking and backend health operations.
 */
interface UpdateRepository {

    /**
     * Checks for application updates against the remote backend manifest.
     */
    suspend fun checkForUpdate(
        currentVersionCode: Int,
        currentVersionName: String
    ): Result<UpdateInfo>

    /**
     * Checks the operational health and connectivity to the backend service.
     */
    suspend fun checkServerHealth(): Result<ServerHealth>

    /**
     * Updates the target backend API base URL.
     */
    fun updateBaseUrl(newUrl: String)

    /**
     * Returns the active backend API base URL.
     */
    fun getBaseUrl(): String
}

package com.example.remoteupdatedemo.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire DTO representing the response from GET /api/v1/version/check.
 * Governed strictly by api-mapping.yaml (Contract ID: version.update.check).
 */
data class UpdateCheckResponseDto(
    @SerializedName("latestVersionName")
    val latestVersionName: String,

    @SerializedName("latestVersionCode")
    val latestVersionCode: Int,

    @SerializedName("hasUpdate")
    val hasUpdate: Boolean,

    @SerializedName("isMandatory")
    val isMandatory: Boolean,

    @SerializedName("downloadUrl")
    val downloadUrl: String,

    @SerializedName("sha256")
    val sha256: String,

    @SerializedName("releaseNotes")
    val releaseNotes: String,

    @SerializedName("publishedAt")
    val publishedAt: String
)

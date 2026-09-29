package com.example.remoteupdatedemo.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire DTO representing system operational status from GET /api/v1/health.
 * Governed strictly by api-mapping.yaml (Contract ID: system.health.check).
 */
data class HealthResponseDto(
    @SerializedName("status")
    val status: String,

    @SerializedName("service")
    val service: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("version")
    val version: String
)

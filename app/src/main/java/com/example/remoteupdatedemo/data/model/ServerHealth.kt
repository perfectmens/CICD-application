package com.example.remoteupdatedemo.data.model

/**
 * Domain entity representing backend connectivity and health state.
 */
data class ServerHealth(
    val isHealthy: Boolean,
    val service: String,
    val version: String,
    val timestamp: String
)

package com.example.remoteupdatedemo.data.model

/**
 * Domain entity representing update evaluation results consumed by UI/ViewModel.
 * Decoupled from the wire DTO schema.
 */
data class UpdateInfo(
    val latestVersionName: String,
    val latestVersionCode: Int,
    val hasUpdate: Boolean,
    val isMandatory: Boolean,
    val downloadUrl: String,
    val sha256: String,
    val releaseNotes: String,
    val publishedAt: String
)

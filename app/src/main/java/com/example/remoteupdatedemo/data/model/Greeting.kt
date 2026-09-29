package com.example.remoteupdatedemo.data.model

/**
 * Domain entity representing a broadcast greeting message.
 */
data class Greeting(
    val id: Int,
    val text: String,
    val category: String,
    val emoji: String
)

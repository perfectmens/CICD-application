package com.example.remoteupdatedemo.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire DTO representing random broadcast messages from GET /api/v1/messages/random.
 * Governed strictly by api-mapping.yaml (Contract ID: messages.greetings.random).
 */
data class GreetingsResponseDto(
    @SerializedName("greetings")
    val greetings: List<GreetingItemDto>,

    @SerializedName("count")
    val count: Int,

    @SerializedName("timestamp")
    val timestamp: String
)

data class GreetingItemDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("text")
    val text: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("emoji")
    val emoji: String
)

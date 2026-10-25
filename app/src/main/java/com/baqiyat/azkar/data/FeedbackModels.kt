package com.baqiyat.azkar.data

data class FeedbackItem(
    val id: String = "",
    val type: String = "suggestion",
    val message: String = "",
    val status: String = "new",
    val userId: String = "",
    val createdAt: Long = 0L,
    val adminReply: String? = null
)

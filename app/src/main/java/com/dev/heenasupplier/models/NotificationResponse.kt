package com.dev.heenasupplier.models

data class NotificationResponse(
    val message: String,
    val notification: List<Notification>,
    val status: Int
)
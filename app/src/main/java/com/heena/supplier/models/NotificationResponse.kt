package com.heena.supplier.models

data class NotificationResponse(
    val message: String,
    val notification: List<Notification>,
    val status: Int
)
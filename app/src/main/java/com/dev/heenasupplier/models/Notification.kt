package com.dev.heenasupplier.models

data class Notification(
    val comment_id: Int,
    val create_at: String,
    val description: String,
    val is_new: Int,
    val notification_id: Int,
    val title: String,
    val user: User
)
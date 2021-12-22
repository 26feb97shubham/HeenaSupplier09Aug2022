package com.heena.supplier.models

data class OldMessagesResponse(
    val message: String,
    val messages: List<Message>,
    val status: Int
)
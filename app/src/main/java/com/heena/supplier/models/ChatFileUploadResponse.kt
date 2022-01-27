package com.heena.supplier.models

data class ChatFileUploadResponse(
    val chat_file: ChatFile,
    val message: String,
    val status: Int
)
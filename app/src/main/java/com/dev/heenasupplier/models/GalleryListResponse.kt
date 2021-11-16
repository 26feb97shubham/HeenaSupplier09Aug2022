package com.dev.heenasupplier.models

data class GalleryListResponse(
    val gallery: List<Gallery>,
    val message: String,
    val status: Int
)
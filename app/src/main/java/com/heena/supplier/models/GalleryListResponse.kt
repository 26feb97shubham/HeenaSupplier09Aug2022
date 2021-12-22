package com.heena.supplier.models

data class GalleryListResponse(
    val gallery: List<Gallery>,
    val message: String,
    val status: Int
)
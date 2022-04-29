package com.heena.supplier.models

import java.io.Serializable

data class GalleryListResponse(
    val gallery: List<Gallery>,
    val message: String,
    val status: Int
): Serializable
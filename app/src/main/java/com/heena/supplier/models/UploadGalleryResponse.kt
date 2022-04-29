package com.heena.supplier.models

import java.io.Serializable

data class UploadGalleryResponse(
    val message: String,
    val status: Int
): Serializable
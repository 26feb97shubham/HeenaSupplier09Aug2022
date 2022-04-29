package com.heena.supplier.models

import java.io.Serializable

data class OfferUpdateResponse(
    val message: String,
    val status: Int
): Serializable

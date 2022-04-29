package com.heena.supplier.models

import java.io.Serializable

data class OfferDeleteResponse(
        val message: String,
        val status: Int
): Serializable

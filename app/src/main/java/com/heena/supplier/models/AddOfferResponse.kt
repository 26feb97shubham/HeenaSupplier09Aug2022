package com.heena.supplier.models

import java.io.Serializable

data class AddOfferResponse(
        val message: String,
        val status: Int
): Serializable

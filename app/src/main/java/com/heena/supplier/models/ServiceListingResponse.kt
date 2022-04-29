package com.heena.supplier.models

import java.io.Serializable

data class ServiceListingResponse(
        val status: Int? = null,
        val message: String? = null,
        val service: List<Service?>? = null
): Serializable
package com.heena.supplier.models

import java.io.Serializable

data class FeaturedServicesListingResponse (
    val status: Int? = null,
    val message: String? = null,
    val service: List<Service?>? = null
): Serializable
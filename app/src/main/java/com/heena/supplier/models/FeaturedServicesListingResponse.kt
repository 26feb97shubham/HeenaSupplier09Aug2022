package com.heena.supplier.models

data class FeaturedServicesListingResponse (
    val status: Int? = null,
    val message: String? = null,
    val service: List<Service?>? = null
)
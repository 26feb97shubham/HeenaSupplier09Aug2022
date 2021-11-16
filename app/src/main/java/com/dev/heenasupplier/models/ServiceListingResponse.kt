package com.dev.heenasupplier.models

data class ServiceListingResponse(
        val status: Int? = null,
        val message: String? = null,
        val service: List<Service?>? = null
)
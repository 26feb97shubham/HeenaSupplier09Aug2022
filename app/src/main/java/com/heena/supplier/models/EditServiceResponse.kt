package com.heena.supplier.models

data class EditServiceResponse(
    val message: String,
    val service: List<Service>,
    val status: Int
)
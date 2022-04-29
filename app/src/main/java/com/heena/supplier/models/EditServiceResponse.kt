package com.heena.supplier.models

import java.io.Serializable

data class EditServiceResponse(
    val message: String,
    val service: List<Service>,
    val status: Int
): Serializable
package com.heena.supplier.models

import java.io.Serializable

data class DeleteServiceResponse(
    val message: String,
    val status: Int
): Serializable
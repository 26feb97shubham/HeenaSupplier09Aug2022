package com.heena.supplier.models

import java.io.Serializable

data class AddServiceResponse(
    val message: String,
    val status: Int
): Serializable
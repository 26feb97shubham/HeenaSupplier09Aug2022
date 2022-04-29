package com.heena.supplier.models

import java.io.Serializable

data class RegistrationVerifyResponse(
    val message: String? = null,
    val status: Int? = null
): Serializable

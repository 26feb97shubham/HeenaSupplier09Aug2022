package com.heena.supplier.models

import java.io.Serializable

data class ForgotPasswordResponse(
	val message: String? = null,
	val status: Int? = null,
	val error: Error? = null
): Serializable


package com.heena.supplier.models

data class LogoutResponse(
	val message: String? = null,
	val error: Error? = null,
	val status: Int? = null
)


package com.heena.supplier.models

data class SignUpResponse(
	val user : User? = null,
	val message: String? = null,
	val status: Int? = null,
	val token: String? = null,
	val error: Error? = null,
)


package com.heena.supplier.models

data class LoginResponse(
	val message: String? = null,
	val error: Error? = null,
	val email: String? = null,
	val status: Int? = null,
	val token: String? = null,
	val user : User? = null
)


package com.heena.supplier.models

import java.io.Serializable

data class LoginResponse(
	val message: String? = null,
	val error: Error? = null,
	val email: String? = null,
	val status: Int? = null,
	val token: String? = null,
	val user : User? = null
): Serializable


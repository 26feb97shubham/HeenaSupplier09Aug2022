package com.heena.supplier.models

import java.io.Serializable

data class SignUpResponse(
	val user : User? = null,
	val message: String? = null,
	val status: Int? = null,
	val token: String? = null,
	val error: Error? = null,
): Serializable


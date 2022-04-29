package com.heena.supplier.models

import java.io.Serializable

data class ProfileShowResponse(
	val profile: Profile? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable




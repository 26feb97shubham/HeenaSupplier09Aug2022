package com.heena.supplier.models

import java.io.Serializable

data class CountryResponse(
	val country: List<CountryItem?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable




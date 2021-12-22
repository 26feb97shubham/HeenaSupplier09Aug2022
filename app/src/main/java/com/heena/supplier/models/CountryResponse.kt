package com.heena.supplier.models

data class CountryResponse(
	val country: List<CountryItem?>? = null,
	val message: String? = null,
	val status: Int? = null
)




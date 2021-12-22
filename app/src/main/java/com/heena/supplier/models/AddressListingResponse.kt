package com.heena.supplier.models

data class AddressListingResponse(
	val address: List<AddressItem?>? = null,
	val message: String? = null,
	val status: Int? = null
)


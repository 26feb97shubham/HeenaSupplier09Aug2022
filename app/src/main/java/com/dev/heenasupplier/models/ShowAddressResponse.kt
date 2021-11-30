package com.dev.heenasupplier.models

data class ShowAddressResponse(
	val bank: Bank? = null,
	val address: AddressItem? = null,
	val message: String? = null,
	val status: Int? = null
)




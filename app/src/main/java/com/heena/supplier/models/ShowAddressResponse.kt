package com.heena.supplier.models

import java.io.Serializable

data class ShowAddressResponse(
	val bank: Bank? = null,
	val address: AddressItem? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable




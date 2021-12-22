package com.heena.supplier.models

data class ShowOfferResponse(
	val offer: Offer? = null,
	val message: String? = null,
	val status: Int? = null
)


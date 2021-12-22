package com.heena.supplier.models

data class OffersListingResponse(
	val offer: List<OfferItem?>? = null,
	val message: String? = null,
	val status: Int? = null
)



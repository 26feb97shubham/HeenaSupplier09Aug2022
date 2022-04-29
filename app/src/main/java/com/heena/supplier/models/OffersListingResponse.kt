package com.heena.supplier.models

import java.io.Serializable

data class OffersListingResponse(
	val offer: List<OfferItem?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable



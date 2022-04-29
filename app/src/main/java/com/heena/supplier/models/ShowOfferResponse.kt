package com.heena.supplier.models

import java.io.Serializable

data class ShowOfferResponse(
	val offer: Offer? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable


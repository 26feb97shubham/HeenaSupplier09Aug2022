package com.heena.supplier.models

import java.io.Serializable

data class BookingsListingResponse(
	val booking: List<BookingItem?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable



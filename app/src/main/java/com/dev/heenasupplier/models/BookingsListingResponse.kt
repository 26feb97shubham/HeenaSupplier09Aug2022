package com.dev.heenasupplier.models

data class BookingsListingResponse(
	val booking: List<BookingItem?>? = null,
	val message: String? = null,
	val status: Int? = null
)



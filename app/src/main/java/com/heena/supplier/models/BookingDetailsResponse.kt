package com.heena.supplier.models

data class BookingDetailsResponse(
	val booking: Booking? = null,
	val error: Error? = null,
	val message: String? = null,
	val status: Int? = null
)



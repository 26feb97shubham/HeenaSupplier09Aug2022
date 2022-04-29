package com.heena.supplier.models

import java.io.Serializable

data class BookingDetailsResponse(
	val booking: Booking? = null,
	val error: Error? = null,
	val message: String? = null,
	val status: Int? = null
):Serializable



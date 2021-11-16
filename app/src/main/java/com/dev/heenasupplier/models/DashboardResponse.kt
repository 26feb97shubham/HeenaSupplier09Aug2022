package com.dev.heenasupplier.models

data class DashboardResponse(
	val booking: List<BookingItem?>? = null,
	val service: List<Service?>? = null,
	val membership: MembershipX? = null,
	val message: String? = null,
	val status: Int? = null
)




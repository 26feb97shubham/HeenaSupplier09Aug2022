package com.heena.supplier.models

data class DashboardResponse(
	val booking: List<BookingItem?>? = null,
	val service: List<Service?>? = null,
	val membership: MembershipX? = null,
	val subscriptions: Subscriptions? = null,
	val message: String? = null,
	val status: Int? = null
)




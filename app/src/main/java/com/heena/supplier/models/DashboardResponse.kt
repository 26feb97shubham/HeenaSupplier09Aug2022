package com.heena.supplier.models

import java.io.Serializable

data class DashboardResponse(
	val booking: List<BookingItem?>? = null,
	val service: List<Service?>? = null,
	val membership: MembershipX? = null,
	val banks : BankY?=null,
	val subscriptions: Subscriptions? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable




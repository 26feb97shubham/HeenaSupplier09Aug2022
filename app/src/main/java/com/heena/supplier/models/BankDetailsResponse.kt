package com.heena.supplier.models

import java.io.Serializable

data class BankDetailsResponse(
	val bank: Bank? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable


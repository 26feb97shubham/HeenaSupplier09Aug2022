package com.heena.supplier.models

import java.io.Serializable

data class TransactionsListingResponse(
	val balance: Double? = null,
	val message: String? = null,
	val transaction: List<TransactionItem?>? = null,
	val status: Int? = null
): Serializable



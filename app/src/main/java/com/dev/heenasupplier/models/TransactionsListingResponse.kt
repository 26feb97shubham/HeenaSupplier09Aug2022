package com.dev.heenasupplier.models

data class TransactionsListingResponse(
	val balance: Int? = null,
	val message: String? = null,
	val transaction: List<TransactionItem?>? = null,
	val status: Int? = null
)



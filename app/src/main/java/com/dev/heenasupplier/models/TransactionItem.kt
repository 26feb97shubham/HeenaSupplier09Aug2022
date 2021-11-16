package com.dev.heenasupplier.models

data class TransactionItem(
        val transaction_id: Int? = null,
        val booking_id: Int? = null,
        val amount: String? = null,
        val service: Service? = null,
        val created_at: String? = null
)

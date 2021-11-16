package com.dev.heenasupplier.models

data class BookingItem(
        val booking_id: Int? = null,
        val date: String? = null,
        val booking_created_at: String? = null,
        val booking_date: String? = null,
        val booking_from: String? = null,
        val booking_to: String? = null,
        val bks_price: String? = null,
        val user: User? = null,
        val price: String? = null,
        val service: Service? = null,
        val created_at: String? = null,
        val payment: Int? = null,
        val status: Int? = null
)

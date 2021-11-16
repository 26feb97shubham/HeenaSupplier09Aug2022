package com.dev.heenasupplier.models

data class Offer(
        val price: String? = null,
        val child_price: String? = null,
        val percentage: String? = null,
        val service: Service? = null,
        val started_at: String? = null,
        val offer_id: String? = null,
        val ended_at: String? = null
)

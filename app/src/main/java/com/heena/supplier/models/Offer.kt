package com.heena.supplier.models

import java.io.Serializable

data class Offer(
        val price: String? = null,
        val offer_price: String? = null,
        val percentage: String? = null,
        val service: ServiceItemY? = null,
        val started_at: String? = null,
        val offer_id: String? = null,
        val ended_at: String? = null
): Serializable

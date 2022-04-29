package com.heena.supplier.models

import java.io.Serializable

data class PriceX(
        val total: String? = null,
        val base_price_lady: String? = null,
        val base_price_child: String? = null,
        val off_price_lady: String? = null,
        val off_price_child: String? = null,
): Serializable

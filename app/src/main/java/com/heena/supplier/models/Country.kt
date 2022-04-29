package com.heena.supplier.models

import java.io.Serializable

data class Country(
    val name: String? = null,
    val name_ar: String? = null,
    val country_id: Int? = null
): Serializable

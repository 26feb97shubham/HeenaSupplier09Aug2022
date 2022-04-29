package com.heena.supplier.models

import java.io.Serializable

data class AddressItem(
    val building_name: String? = null,
    val country: Country? = null,
    val user_id: String? = null,
    val flat: String? = null,
    val address_id: String? = null,
    val title: String? = null,
    val street: String? = null,
    val is_default: Int? = null,
    val created_at: String? = null
): Serializable

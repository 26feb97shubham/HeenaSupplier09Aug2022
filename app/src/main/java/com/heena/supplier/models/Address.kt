package com.heena.supplier.models

import java.io.Serializable

data class Address(
        val country: String? = null,
        val building_name: String? = null,
        val flat: String? = null,
        val street: String? = null,
        val title: String? = null
):Serializable

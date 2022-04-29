package com.heena.supplier.models

import java.io.Serializable

data class Location(
        val latitude: String? = null,
        val name: String? = null,
        val longitude: String? = null
): Serializable

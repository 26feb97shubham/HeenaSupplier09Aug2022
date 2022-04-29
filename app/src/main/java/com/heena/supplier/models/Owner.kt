package com.heena.supplier.models

import java.io.Serializable

data class Owner(
        val image: String? = null,
        val user_id: Int? = null,
        val name: String? = null,
        val username: String? = null,
): Serializable
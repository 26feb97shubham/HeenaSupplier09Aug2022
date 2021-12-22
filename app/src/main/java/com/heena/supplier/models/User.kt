package com.heena.supplier.models

data class User(
        val user_id: Int? = null,
        val have_membership: Int? = null,
        val have_subscription: Int? = null,
        val phone: String? = null,
        val id: Int? = null,
        val image: String? = null,
        val username: String? = null,
        val address: String? = null,
        val country: String? = null,
        val email: String? = null,
        val name: String,
)

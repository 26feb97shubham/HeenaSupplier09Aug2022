package com.heena.supplier.models

data class BuyMembership(
    val membership: Membership,
    val message: String,
    val status: Int
)
package com.heena.supplier.models

import java.io.Serializable

data class BuyMembership(
    val membership: Membership,
    val message: String,
    val status: Int
): Serializable
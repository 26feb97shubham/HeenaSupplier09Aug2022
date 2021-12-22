package com.heena.supplier.models

import java.io.Serializable

data class Membership(
    val day: Int,
    val description: String,
    val membership_id: Int,
    val name: String,
    val price: Int
) : Serializable
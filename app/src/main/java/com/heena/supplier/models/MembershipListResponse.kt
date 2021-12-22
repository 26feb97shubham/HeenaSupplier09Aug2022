package com.heena.supplier.models

data class MembershipListResponse(
    val membership: List<Membership>,
    val message: String,
    val status: Int
)
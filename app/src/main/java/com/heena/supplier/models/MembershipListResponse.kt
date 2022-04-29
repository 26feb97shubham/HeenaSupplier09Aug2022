package com.heena.supplier.models

import java.io.Serializable

data class MembershipListResponse(
    val membership: List<Membership>,
    val message: String,
    val status: Int
): Serializable
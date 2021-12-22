package com.heena.supplier.models

import java.io.Serializable

data class MembershipX(
    val day: Int,
    val have: Int,
    val id: Int,
    val name : String,
    val amount : Int,
    val end_date : String,
    val total_day : Int
) : Serializable
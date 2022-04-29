package com.heena.supplier.models

import java.io.Serializable

data class MembershipX(
    var day: Int?=null,
    var have: Int?=null,
    var id: Int?=null,
    var name : String?=null,
    var amount : Int?=null,
    var end_date : String?=null,
    var total_day : Int?=null
) : Serializable
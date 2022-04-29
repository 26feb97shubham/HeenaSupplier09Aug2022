package com.heena.supplier.models

import java.io.Serializable

data class Subscriptions(
    val days: Int?=null,
    val have: Int?=null,
    val id: Int?=null,
    val manager_id: Int?=null,
    val name : String?=null,
    val amount : Int?=null,
    val end_date : String?=null,
    val starting_at : String?=null,
    val ending_at : String?=null,
    val created_at : String?=null,
    val updated_at : String?=null,
    val ended_day : Int?=null
) : Serializable

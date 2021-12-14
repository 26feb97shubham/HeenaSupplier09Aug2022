package com.dev.heenasupplier.models

import java.io.Serializable

data class Subscriptions(
    val days: Int,
    val have: Int,
    val id: Int,
    val manager_id: Int,
    val name : String,
    val amount : Int,
    val end_date : String,
    val starting_at : String,
    val ending_at : String,
    val created_at : String,
    val updated_at : String,
    val ended_day : Int
) : Serializable

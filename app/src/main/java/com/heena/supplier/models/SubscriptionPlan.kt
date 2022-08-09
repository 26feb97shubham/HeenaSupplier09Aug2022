package com.heena.supplier.models

import java.io.Serializable

data class SubscriptionPlan(
    val amount: Int,
    val days: Int,
    val description: String,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val subscription_plans_id: Int
): Serializable
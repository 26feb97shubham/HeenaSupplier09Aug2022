package com.heena.supplier.models

import java.io.Serializable

data class SubscriptionPlan(
    val amount: Int,
    val days: Int,
    val description: String,
    val name: String,
    val subscription_plans_id: Int
): Serializable
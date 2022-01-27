package com.heena.supplier.models

data class SubscriptionPlansResponse(
    val message: String,
    val status: Int,
    val subscription_plans: ArrayList<SubscriptionPlan>
)
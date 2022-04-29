package com.heena.supplier.models

import java.io.Serializable

data class SubscriptionPlansResponse(
    val message: String,
    val status: Int,
    val subscription_plans: ArrayList<SubscriptionPlan>
): Serializable
package com.heena.supplier.models

import java.io.Serializable

data class DashHelpCategoryResponse(
    val help_category: List<HelpCategory>,
    val message: String,
    val admin_id: Int,
    val status: Int
): Serializable
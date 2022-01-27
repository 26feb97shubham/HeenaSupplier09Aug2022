package com.heena.supplier.models

data class DashHelpCategoryResponse(
    val help_category: List<HelpCategory>,
    val message: String,
    val admin_id: Int,
    val status: Int
)
package com.heena.supplier.models

data class DashHelpCategoryResponse(
    val help_category: List<HelpCategory>,
    val message: String,
    val status: Int
)
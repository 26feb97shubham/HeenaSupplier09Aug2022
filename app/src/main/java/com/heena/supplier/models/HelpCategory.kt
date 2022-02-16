package com.heena.supplier.models

import java.io.Serializable

data class HelpCategory(
    val help_sub_category: List<HelpSubCategory>,
    val id: Int,
    val parent_id: Any,
    val title: String,
    val title_ar: String,
    var isShow: Boolean = false
): Serializable
package com.heena.supplier.models

import java.io.Serializable

data class HelpSubCategory(
    val id: Int,
    val parent_id: Int,
    val title: String,
    val title_ar: String,
    val content : ArrayList<Content>
): Serializable
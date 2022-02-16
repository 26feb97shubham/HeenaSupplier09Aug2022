package com.heena.supplier.models

import java.io.Serializable

data class Content(
    val id : Int,
    val help_id : Int,
    val content : String,
    val content_ar: String
): Serializable
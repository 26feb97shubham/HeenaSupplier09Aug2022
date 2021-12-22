package com.heena.supplier.models

data class ViewCardResponse(
    val message: String? = null,
    val status: Int? = null,
    val cards : List<Cards?>?=null
)

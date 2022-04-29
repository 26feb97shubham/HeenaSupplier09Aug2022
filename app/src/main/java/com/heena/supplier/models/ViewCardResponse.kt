package com.heena.supplier.models

import java.io.Serializable

data class ViewCardResponse(
    val message: String? = null,
    val status: Int? = null,
    val cards : List<Cards?>?=null
): Serializable

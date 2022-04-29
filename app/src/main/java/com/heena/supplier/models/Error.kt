package com.heena.supplier.models

import java.io.Serializable

data class Error(	val any: Any? = null,
                     val userId: List<String?>? = null): Serializable

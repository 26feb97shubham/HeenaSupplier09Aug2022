package com.heena.supplier.models

import com.google.gson.annotations.SerializedName

data class TransactionURLResponse(
    @SerializedName("status"   ) var status   : Int?    = null,
    @SerializedName("error"    ) var error    : Error?  = Error(),
    @SerializedName("message"  ) var message  : String? = null,
    @SerializedName("url"      ) var url      : String? = null,
    @SerializedName("fileName" ) var fileName : String? = null
)

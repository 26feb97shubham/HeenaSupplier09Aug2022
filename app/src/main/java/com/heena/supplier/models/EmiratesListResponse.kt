package com.heena.supplier.models

import com.google.gson.annotations.SerializedName

data class EmiratesListResponse(
    @SerializedName("status"   ) var status   : Int?                = null,
    @SerializedName("error"    ) var error    : Error?              = Error(),
    @SerializedName("emirates" ) var emirates : ArrayList<Emirates> = arrayListOf(),
    @SerializedName("message"  ) var message  : String?             = null
)

data class Emirates (

    @SerializedName("country_id" ) var countryId : Int?    = null,
    @SerializedName("name"       ) var name      : String? = null,
    @SerializedName("name_ar"    ) var nameAr    : String? = null

)

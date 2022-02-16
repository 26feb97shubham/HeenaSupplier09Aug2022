package com.heena.supplier.models

import com.google.gson.annotations.SerializedName

data class SourceModel(
    @SerializedName("object" ) var myobject : String? = null,
    @SerializedName("id"     ) var id     : String? = null,
    @SerializedName("type"     ) var type     : String? = null
)

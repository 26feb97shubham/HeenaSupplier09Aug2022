package com.heena.supplier.models

data class Cards(
    var id : Int,
    var user_id : Int,
    var name : String,
    var number : String,
    var cvv : String,
    var expiry_date : String,
    var created_at : String,
    var updated_at : String
)

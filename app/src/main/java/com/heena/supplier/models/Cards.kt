package com.heena.supplier.models

import java.io.Serializable

data class Cards(
    var id : Int,
    var user_id : Int,
    var name : String,
    var number : String,
    var cvv : String,
    var expiry_date : String,
    var created_at : String,
    var updated_at : String,
    var card_id : String,
    var brand : String,
    var scheme : String,
    var bank : String,
    var country : String,
    var first_six : String
): Serializable

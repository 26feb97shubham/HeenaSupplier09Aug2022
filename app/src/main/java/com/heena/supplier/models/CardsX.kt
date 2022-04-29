package com.heena.supplier.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CardsX(
	@SerializedName("id"          ) var id          : String? = null,
	@SerializedName("created"          ) var created          : String? = null,
	@SerializedName("object"      ) var my_object      : String? = null,
	@SerializedName("live_mode"      ) var live_mode      : String? = null,
	@SerializedName("type"      ) var type      : String? = null,
	@SerializedName("used"      ) var used      : Boolean? = null,
	@SerializedName("card"      ) var CardY      : CardY? = null,

): Serializable

data class Issuer(
	@SerializedName("bank"    ) var bank    : String? = null,
	@SerializedName("country" ) var country : String? = null,
	@SerializedName("id"      ) var id      : String? = null
): Serializable

data class CardY(
	@SerializedName("id"          ) var id          : String? = null,
	@SerializedName("object"      ) var my_object      : String? = null,
	@SerializedName("customer"    ) var customer    : String? = null,
	@SerializedName("funding"     ) var funding     : String? = null,
	@SerializedName("fingerprint" ) var fingerprint : String? = null,
	@SerializedName("brand"       ) var brand       : String? = null,
	@SerializedName("scheme"      ) var scheme      : String? = null,
	@SerializedName("name"        ) var name        : String? = null,
	@SerializedName("issuer"      ) var issuer      : Issuer? = Issuer(),
	@SerializedName("exp_month"   ) var expMonth    : Int?    = null,
	@SerializedName("exp_year"    ) var expYear     : Int?    = null,
	@SerializedName("last_four"   ) var lastFour    : String? = null,
	@SerializedName("first_six"   ) var firstSix    : String? = null
): Serializable


package com.heena.supplier.models
import java.io.Serializable

data class Booking(
        val address: Address? = null,
        val booking_from: String? = null,
        val cancelled_by: Int? = null,
        val booking_date: String? = null,
        val message: String? = null,
        val commission: String? = null,
        val booking_created_at: String? = null,
        val address_id: Int? = null,
        val c_children: Int? = null,
        val booking_id: Int? = null,
        val booking_to: String? = null,
        val c_ladies: Int? = null,
        val user_id: Int? = null,
        val manager_id: Int? = null,
        val service: ServiceItemX? = null,
        val price: PriceX? = null,
        val service_id: Int? = null,
        val payment: Int? = null,
        val location: Location? = null,
        val user: User? = null,
        val gallery: List<String?>? = null,
        var status: Int? = null,
        val card: CardsX?=null
):Serializable


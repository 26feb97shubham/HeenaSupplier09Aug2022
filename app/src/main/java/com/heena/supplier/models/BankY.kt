package com.heena.supplier.models

import java.io.Serializable

data class BankY(
    val account_number: String? = null,
    val full_name: String? = null,
    val user_id: String? = null,
    val id: String? = null,
    val bank_id: String? = null,
    val iban: String? = null,
    val bank_name: String? = null
): Serializable

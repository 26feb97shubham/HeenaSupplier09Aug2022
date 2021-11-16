package com.dev.heenasupplier.models

data class ServiceItem(	val child_price: String? = null,
                           val price: String? = null,
                           val service_id: Int? = null,
                           val name: String? = null,
                           val category: CategoryItem? = null,
                           val gallery: List<String?>? = null)

package com.dev.heenasupplier.models

data class ShowServiceResponse(
	val bank: Bank? = null,
	val service: Service? = null,
	val message: String? = null,
	val category: List<CategoryItem?>? = null,
	val gallery: List<GalleryItem?>? = null,
	val status: Int? = null
)


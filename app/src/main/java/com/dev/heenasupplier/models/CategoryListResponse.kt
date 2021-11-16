package com.dev.heenasupplier.models

data class CategoryListResponse(
	val catgory: List<Any?>? = null,
	val message: String? = null,
	val category: List<CategoryItem?>? = null,
	val status: Int? = null
)



package com.heena.supplier.models

import java.io.Serializable

data class ShowCommentsResponse(
	val comments: List<CommentsItem?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable






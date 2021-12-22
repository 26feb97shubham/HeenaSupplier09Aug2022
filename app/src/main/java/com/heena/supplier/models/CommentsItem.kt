package com.heena.supplier.models

data class CommentsItem(
        val owner: Owner? = null,
        val business_user_id: Int? = null,
        val star: Int? = null,
        val user_id: Int? = null,
        val message: String? = null,
        val create_at: String? = null,
        val comment_id: Int? = null
)
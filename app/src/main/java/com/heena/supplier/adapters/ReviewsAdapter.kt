package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.models.CommentsItem
import kotlinx.android.synthetic.main.layout_reviews_items.view.*

class ReviewsAdapter(private val context: Context, private val commentsListing: ArrayList<CommentsItem>) : RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterVH>() {
    inner class ReviewsAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind(commentsItem: CommentsItem) {
            itemView.tv_naqasha_name.text = commentsItem.owner!!.name
            itemView.tv_service_date.text = commentsItem.create_at
            itemView.tv_service_desc.text = commentsItem.message
            itemView.txtRating.text = commentsItem.star.toString()
            itemView.ratingBar.rating = commentsItem.star!!.toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_reviews_items, parent, false)
        return ReviewsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ReviewsAdapterVH, position: Int) {
        val commentsItem = commentsListing[position]
        holder.bind(commentsItem)
    }

    override fun getItemCount(): Int {
       return commentsListing.size
    }
}
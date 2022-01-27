package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heena.supplier.R
import com.heena.supplier.models.CommentsItem
import kotlinx.android.synthetic.main.layout_reviews_items.view.*

class ReviewsAdapter(private val context: Context, private val commentsListing: ArrayList<CommentsItem>) : RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterVH>() {
    inner class ReviewsAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind(commentsItem: CommentsItem) {
            if(commentsItem.owner!!.name.isNullOrBlank()){
                itemView.tv_naqasha_name.text = commentsItem.owner.username
            }else{
                itemView.tv_naqasha_name.text = commentsItem.owner.name
            }
            Glide.with(context).load(commentsItem.owner.image).into(itemView.civ_profile)
            itemView.tv_service_date.text = commentsItem.create_at
            itemView.tv_service_desc.text = commentsItem.message
            if (commentsItem.star.toString().equals("")||commentsItem.star.toString()==null){
                itemView.txtRating.text = "0.0"
                itemView.ratingBar.rating = 0F
            }else{
                itemView.txtRating.text = String.format("%.1f",commentsItem.star.toString().toDouble())
                itemView.ratingBar.rating = commentsItem.star!!.toFloat()
            }
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
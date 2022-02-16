package com.heena.supplier.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.supplier.R
import com.heena.supplier.models.CommentsItem
import kotlinx.android.synthetic.main.fragment_my_profile.view.*
import kotlinx.android.synthetic.main.layout_reviews_items.view.*
import kotlinx.android.synthetic.main.layout_reviews_items.view.civ_profile
import kotlinx.android.synthetic.main.layout_reviews_items.view.ratingBar
import kotlinx.android.synthetic.main.layout_reviews_items.view.txtRating

class ReviewsAdapter(private val context: Context, private val commentsListing: ArrayList<CommentsItem>) : RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterVH>() {
    inner class ReviewsAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind(commentsItem: CommentsItem) {
            if(commentsItem.owner!!.name.isNullOrBlank()){
                itemView.tv_naqasha_name.text = commentsItem.owner.username
            }else{
                itemView.tv_naqasha_name.text = commentsItem.owner.name
            }
            val requestOption = RequestOptions().centerCrop()
            Glide.with(context).load(commentsItem.owner.image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                        Log.e("err", p0?.message.toString())
                        itemView.reviewCIVprofileprogressbar.visibility =
                            View.GONE
                        return false
                    }

                    override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                        itemView.reviewCIVprofileprogressbar.visibility =
                            View.GONE
                        return false
                    }
                }).placeholder(R.drawable.user)
                .apply(requestOption).into(itemView.civ_profile)
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
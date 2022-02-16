package com.heena.supplier.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.Gallery
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.gallery_posts.view.*


class GalleryStaggeredGridAdapter(
    private val context: Context,
    private val ImageUriList: ArrayList<Gallery>,
    private val onRecyclerItemClick: ClickInterface.OnGalleryItemClick
) :
    RecyclerView.Adapter<GalleryStaggeredGridAdapter.PostViewHolder>() {
    inner class PostViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        fun bind(imagePath: String, position: Int) {
            val requestOption = RequestOptions().error(R.drawable.def_henna).centerCrop()
            Glide.with(context).load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                       v.gallery_loader.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                        v.gallery_loader.visibility = View.GONE
                        return false
                    }
                }).apply(requestOption).into(v.gallery_images)

            v.delete_image.setSafeOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }

            v.setSafeOnClickListener {
                onRecyclerItemClick.onShowImage(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.gallery_posts, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(ImageUriList[position].image, position)
    }

    override fun getItemCount(): Int {
        return ImageUriList.size
    }
}
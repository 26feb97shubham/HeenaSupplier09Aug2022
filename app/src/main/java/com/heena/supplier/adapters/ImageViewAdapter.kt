package com.heena.supplier.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.heena.supplier.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_view_items.view.*
import java.lang.Exception


class ImageViewAdapter(
    private val context: Context,
    private val galleryList: ArrayList<String>
) :
    RecyclerView.Adapter<ImageViewAdapter.ImageViewAdapterVH>() {
    inner class ImageViewAdapterVH(private val imageViewItemView: View) : RecyclerView.ViewHolder(
        imageViewItemView
    ){
        fun bind(images: String) {
            Glide.with(context).load(images).listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageViewItemView.loadMapimage.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageViewItemView.loadMapimage.visibility = View.GONE
                    return false
                }

            }).into(imageViewItemView.viewMap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.image_view_items, parent, false)
        return ImageViewAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ImageViewAdapterVH, position: Int) {
        val images = galleryList[position]
        holder.bind(images)
    }

    override fun getItemCount(): Int {
        return galleryList.size
    }
}
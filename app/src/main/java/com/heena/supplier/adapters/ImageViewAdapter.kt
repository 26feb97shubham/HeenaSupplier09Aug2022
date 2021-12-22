package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
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
            Picasso.get().load(images).into(imageViewItemView.viewMap, object : Callback{
                override fun onSuccess() {
                    imageViewItemView.loadMapimage.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    Toast.makeText(context, "can not load this Images", Toast.LENGTH_SHORT).show()
                    imageViewItemView.loadMapimage.visibility = View.GONE
                }

            })
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
package com.dev.heenasupplier.adapters

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
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.models.DeleteGalleryImage
import com.dev.heenasupplier.models.Gallery
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.LogUtils
import kotlinx.android.synthetic.main.gallery_posts.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class GalleryStaggeredGridAdapter(
    private val context: Context,
    private val ImageUriList: ArrayList<Gallery>,
    private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
) :
    RecyclerView.Adapter<GalleryStaggeredGridAdapter.PostViewHolder>() {
    inner class PostViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        fun bind(imagePath: String, galleryId: Int, position: Int) {
            val requestOption = RequestOptions().centerCrop()
            Glide.with(context).load(imagePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                         Log.e("err", p0?.message.toString())
                            return false
                        }

                        override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {


                            return false
                        }
                    }).apply(requestOption).into(v.gallery_images)

            v.delete_image.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }

           /* Glide.with(context).load(imagePath)
                .into(v.gallery_images)*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.gallery_posts, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(ImageUriList[position].image,ImageUriList[position].gallery_id, position)
    }

    override fun getItemCount(): Int {
        return ImageUriList.size
    }
}
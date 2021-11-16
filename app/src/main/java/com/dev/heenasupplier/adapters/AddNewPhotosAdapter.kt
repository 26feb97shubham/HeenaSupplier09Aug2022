package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.photos_post_items.view.*

class AddNewPhotosAdapter(private val context: Context, private val photosList : ArrayList<String>, private val onCLickAction : ClickInterface.OnRecyclerItemClick) :
    RecyclerView.Adapter<AddNewPhotosAdapter.AddNewPhotosAdapterVH>() {
    inner class AddNewPhotosAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddNewPhotosAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.photos_post_items, parent, false)
        return AddNewPhotosAdapterVH(view)
    }

    override fun onBindViewHolder(holder: AddNewPhotosAdapterVH, position: Int) {
        Glide.with(context).load(photosList[position]).placeholder(R.drawable.user).into(holder.itemView.aciv_photos)
        holder.itemView.aciv_delete_photos.setOnClickListener {
            onCLickAction.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
        return photosList.size
    }
}
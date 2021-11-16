package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R

class AddNewFeaturedAdapter(private val context: Context) : RecyclerView.Adapter<AddNewFeaturedAdapter.AddNewFeaturedAdapterVH>() {
    inner class AddNewFeaturedAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddNewFeaturedAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.layout_added_new_featured_items, parent, false)
        return AddNewFeaturedAdapterVH(view)
    }

    override fun onBindViewHolder(holder: AddNewFeaturedAdapterVH, position: Int) {
    }

    override fun getItemCount(): Int {
     return 8
    }
}
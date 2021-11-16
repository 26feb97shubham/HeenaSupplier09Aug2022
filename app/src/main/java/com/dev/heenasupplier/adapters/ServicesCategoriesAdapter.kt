package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import kotlinx.android.synthetic.main.services_categories_items.view.*

class ServicesCategoriesAdapter(private val context: Context, private val categoriesList : ArrayList<String>,
                                private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick)
    : RecyclerView.Adapter<ServicesCategoriesAdapter.ServicesCategoriesAdapterVH>() {
    inner class ServicesCategoriesAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesCategoriesAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.services_categories_items, parent, false)
        return ServicesCategoriesAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ServicesCategoriesAdapterVH, position: Int) {
        holder.itemView.tv_services_categories.text = categoriesList[position]

        holder.itemView.setOnClickListener {
            onRecyclerItemClick.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }
}
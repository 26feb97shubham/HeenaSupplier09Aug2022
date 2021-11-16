package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import kotlinx.android.synthetic.main.services_categories_items.view.*

class ServiceListingAdapter(
        private val context: Context, private val serviceListing : ArrayList<String>,
        private val onRecyclerItemClick: ClickInterface.OnServiceClick
) : RecyclerView.Adapter<ServiceListingAdapter.ServicesListingAdapterVH>()  {
    var mSelectedItem = -1
    inner class ServicesListingAdapterVH(val serviceItemView : View) : RecyclerView.ViewHolder(serviceItemView) {
        fun bind(service: String, position: Int) {
            serviceItemView.tv_services_categories.text = serviceListing[position]
            serviceItemView.rb_address.isChecked = position == mSelectedItem
            serviceItemView.setOnClickListener {
                mSelectedItem = adapterPosition
                notifyDataSetChanged()
                onRecyclerItemClick.OnAddService(mSelectedItem, service)
            }

            serviceItemView.rb_address.setOnClickListener {
                mSelectedItem = adapterPosition
                notifyDataSetChanged()
                onRecyclerItemClick.OnAddService(mSelectedItem, service)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesListingAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.services_categories_items, parent, false)
        return ServicesListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ServicesListingAdapterVH, position: Int) {
        val service = serviceListing[position]
        holder.bind(service, position)
    }

    override fun getItemCount(): Int {
        return serviceListing.size
    }
}
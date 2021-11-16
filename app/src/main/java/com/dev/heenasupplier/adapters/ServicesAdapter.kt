package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.models.Service
import kotlinx.android.synthetic.main.layout_services_item.view.*

class ServicesAdapter(private val context: Context,
                      private val servicesListing : ArrayList<Service>,
                      private val onServicesItemClick: ClickInterface.onServicesItemClick) : RecyclerView.Adapter<ServicesAdapter.ServicesAdapterVH>() {
    inner class ServicesAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){
        fun onBind(service: Service, position: Int) {
            if (service.gallery?.size==0){
                itemView.img.setImageResource(R.drawable.user)
            }else{
                Glide.with(context).applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.user)).load(service.gallery!![0]).into(itemView.img)
            }
            itemView.tv_services.text = service.name
            itemView.tv_price.text = service.price!!.total
            itemView.tv_category_name.text = service.category!!.name

            itemView.iv_delete.setOnClickListener {
                onServicesItemClick.onServiceDele(position = position)
            }

            itemView.iv_edit.setOnClickListener {
                onServicesItemClick.onServiceEdit(position)
            }

            itemView.cv_image.setOnClickListener {
                onServicesItemClick.onServicClick(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.layout_services_item, parent, false)
        return ServicesAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ServicesAdapterVH, position: Int) {
        holder.onBind(servicesListing[position], position)
    }

    override fun getItemCount(): Int {
       return servicesListing.size
    }
}
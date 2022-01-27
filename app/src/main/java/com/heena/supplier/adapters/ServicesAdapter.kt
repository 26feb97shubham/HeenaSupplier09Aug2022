package com.heena.supplier.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.Service
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.layout_services_item.view.*

class ServicesAdapter(private val context: Context,
                      private val servicesListing : ArrayList<Service>,
                      private val subscription_id : Int,
                      private val onServicesItemClick: ClickInterface.onServicesItemClick) : RecyclerView.Adapter<ServicesAdapter.ServicesAdapterVH>() {
    inner class ServicesAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){
        @SuppressLint("CheckResult")
        fun onBind(service: Service, position: Int) {
            if (service.gallery?.size==0){
                itemView.img.setImageResource(R.drawable.user)
            }else{
                Glide.with(context)
                    .load(service.gallery!![0])
                    .apply(RequestOptions().placeholder(R.drawable.user).error(R.drawable.user))
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            itemView.img_loader.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            itemView.img_loader.visibility = View.GONE
                            return false
                        }

                    }).into(itemView.img)
            }

            itemView.tv_services.text = service.name
            val price =  "AED ${service.price!!.total}"
            itemView.tv_price.text = price
            itemView.tv_category_name.text = service.category!!.name

            if (service.subscription_id!!>0){
                itemView.iv_edit.setImageResource(R.drawable.star)
            }else{
                itemView.iv_edit.setImageResource(R.drawable.edit_icon)
            }

            itemView.iv_delete.setSafeOnClickListener {
                onServicesItemClick.onServiceDele(position = position)
            }

            itemView.iv_edit.setSafeOnClickListener {
                onServicesItemClick.onServiceEdit(position)
            }

            itemView.cv_image.setSafeOnClickListener {
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
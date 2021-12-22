package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface

class ServiceAdapter(private val context: Context, private val onCLickAction : ClickInterface.OnRecyclerItemClick) :
    RecyclerView.Adapter<ServiceAdapter.ServiceAdapterVH>() {
    inner class ServiceAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.naqashat_services_recycler_item, parent, false)
        return ServiceAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ServiceAdapterVH, position: Int) {
        holder.itemView.setOnClickListener {
            onCLickAction.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
       return 10
    }
}
package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.`interface`.ClickInterface


class BookingServicesAdapter(private val context: Context, private val layout : Int, private val onCLickAction : ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<BookingServicesAdapter.BookingServicesAdapterVH>() {
    inner class BookingServicesAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingServicesAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(layout, parent, false)
        return BookingServicesAdapterVH(view)
    }

    override fun onBindViewHolder(holder: BookingServicesAdapterVH, position: Int) {
        holder.itemView.setOnClickListener {
            onCLickAction.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
        return 6
    }
}
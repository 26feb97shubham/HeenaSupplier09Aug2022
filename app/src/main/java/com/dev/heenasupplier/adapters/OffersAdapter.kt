package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface

class OffersAdapter(private val context: Context, private val onCLickAction : ClickInterface.OnRecyclerItemClick) :
    RecyclerView.Adapter<OffersAdapter.OffersAdapterVH>() {
    inner class OffersAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OffersAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.offers_n_disc_listing_recycler_item, parent, false)
        return OffersAdapterVH(view)
    }

    override fun onBindViewHolder(holder: OffersAdapterVH, position: Int) {
      holder.itemView.setOnClickListener {
          onCLickAction.OnClickAction(position)
      }
    }

    override fun getItemCount(): Int {
       return 6
    }
}
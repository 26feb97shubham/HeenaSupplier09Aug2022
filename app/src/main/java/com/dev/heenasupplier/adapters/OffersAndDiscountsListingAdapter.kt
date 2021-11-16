package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface

class OffersAndDiscountsListingAdapter(private val context: Context, private val onCLickAction : ClickInterface.OnRecyclerItemClick) :
    RecyclerView.Adapter<OffersAndDiscountsListingAdapter.OffersAndDiscountsListingAdapterVH>() {
    inner class OffersAndDiscountsListingAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OffersAndDiscountsListingAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.filtered_offers_n_disc_recycler_item_listing, parent, false)
        return OffersAndDiscountsListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: OffersAndDiscountsListingAdapterVH, position: Int) {
        holder.itemView.setOnClickListener {
            onCLickAction.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
       return 8
    }
}
package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.CountryItem
import kotlinx.android.synthetic.main.country_list_items.view.*

class CountryListingAdapter(
        private val context: Context, private val countryListing : ArrayList<CountryItem>,
        private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
): RecyclerView.Adapter<CountryListingAdapter.CountryListingAdapterVH>()  {
    inner class CountryListingAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(country: CountryItem, position: Int) {
            itemView.tv_country_name.text = country.name
            itemView.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryListingAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.country_list_items, parent, false)
        return CountryListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: CountryListingAdapterVH, position: Int) {
        val country = countryListing[position]
        holder.bind(country,position)
    }

    override fun getItemCount(): Int {
        return countryListing.size
    }
}
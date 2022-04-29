package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.Country
import com.heena.supplier.models.CountryItem
import com.heena.supplier.models.Emirates
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.country_list_items.view.*

class CountryListingAdapter(
        private val context: Context,
        private val countryListing : ArrayList<CountryItem>?,
        private val emiratesListing : ArrayList<Emirates>?,
        private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
): RecyclerView.Adapter<CountryListingAdapter.CountryListingAdapterVH>()  {
    inner class CountryListingAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(country: CountryItem?, emirate : Emirates?, position: Int) {
            if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "").equals("ar")){
                if (country!=null){
                    itemView.tv_country_name.text = country!!.name_ar
                }else{
                    itemView.tv_country_name.text = emirate!!.nameAr
                }

            }else{
                if (country!=null){
                    itemView.tv_country_name.text = country!!.name
                }else{
                    itemView.tv_country_name.text = emirate!!.name
                }
            }

            itemView.setSafeOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryListingAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.country_list_items, parent, false)
        return CountryListingAdapterVH(view)
    }

    override fun onBindViewHolder(holder: CountryListingAdapterVH, position: Int) {
        var emirate : Emirates?= null
        var country : CountryItem?= null
        if(countryListing==null && emiratesListing!=null){
            emirate = emiratesListing?.get(position)!!
        }else{
            country = countryListing?.get(position)!!
        }
        if (country!=null && emirate==null){
            holder.bind(country,null,position)
        }else if (country==null && emirate!=null){
            holder.bind(null,emirate,position)
        }else{
            holder.bind(null,null,position)
        }
    }

    override fun getItemCount(): Int {
        if(countryListing!=null){
            return countryListing.size
        }else if(emiratesListing!=null){
            return emiratesListing.size
        }else{
            return 0
        }
    }
}
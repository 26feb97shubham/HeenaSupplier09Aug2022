package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.OfferItem
import kotlinx.android.synthetic.main.naqashat_offers_recycler_item.view.*

class OffersAndDiscountsAdapter(
    private val context: Context,
    private val offersListing: ArrayList<OfferItem>,
    private val onOffersItemClick: ClickInterface.onOffersItemClick
) :
    RecyclerView.Adapter<OffersAndDiscountsAdapter.OffersAndDiscountsAdapterVH>() {
    var favAdded : Boolean = true
    inner class OffersAndDiscountsAdapterVH(val offeritemView : View) : RecyclerView.ViewHolder(offeritemView){
        fun bind(offerItem: OfferItem, position: Int) {
            Glide.with(context).load(offerItem.gallery!![0]).into(offeritemView.img)
            offeritemView.tv_services.text = offerItem.name
            offeritemView.tv_original_price.text = "AED ${offerItem.price}"
            offeritemView.tv_discounted_price.text = "AED ${offerItem.offer_price}"
            offeritemView.iv_add_to_fav.setOnClickListener {
                if (favAdded){
                    favAdded = false
                    val drawable = context.resources.getDrawable(R.drawable.fav_added_icon)
                    offeritemView.iv_add_to_fav.setImageDrawable(drawable)
                }else{
                    val drawable = context.resources.getDrawable(R.drawable.add_to_fav_icon)
                    offeritemView.iv_add_to_fav.setImageDrawable(drawable)
                }
            }
            offeritemView.iv_edit.setOnClickListener {
                onOffersItemClick.onOfferEdit(position)
            }

            offeritemView.iv_delete.setOnClickListener {
                onOffersItemClick.onOfferDelete(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OffersAndDiscountsAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.naqashat_offers_recycler_item, parent, false)
        return OffersAndDiscountsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: OffersAndDiscountsAdapterVH, position: Int) {
        val offerItem = offersListing[position]
        holder.bind(offerItem, position)
    }

    override fun getItemCount(): Int {
        return offersListing.size
    }
}
package com.heena.supplier.adapters

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
import com.bumptech.glide.request.target.Target
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.OfferItem
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.naqashat_offers_recycler_item.view.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class OffersAndDiscountsAdapter(
    private val context: Context,
    private val offersListing: ArrayList<OfferItem>,
    private val onOffersItemClick: ClickInterface.onOffersItemClick
) :
    RecyclerView.Adapter<OffersAndDiscountsAdapter.OffersAndDiscountsAdapterVH>() {
    var favAdded : Boolean = true
    inner class OffersAndDiscountsAdapterVH(val offeritemView : View) : RecyclerView.ViewHolder(offeritemView){
        fun bind(offerItem: OfferItem, position: Int) {
            if (offerItem.gallery!!.isEmpty()){
                Glide.with(context).load(R.drawable.def_henna)
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            offeritemView.offers_loader.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            offeritemView.offers_loader.visibility = View.GONE
                            return false
                        }

                    }).placeholder(R.drawable.def_henna)
                    .into(offeritemView.img)
            }else{
                Glide.with(context).load(offerItem.gallery!![0])
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            offeritemView.offers_loader.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            offeritemView.offers_loader.visibility = View.GONE
                            return false
                        }

                    }).placeholder(R.drawable.def_henna)
                    .into(offeritemView.img)
            }

            offeritemView.tv_services.text = offerItem.name
            offeritemView.tv_original_price.text = "AED ${
                Utility.convertDoubleValueWithCommaSeparator(
                    offerItem.price!!.toDouble()
                )
            }"
            offeritemView.tv_discounted_price.text = "AED ${
                Utility.convertDoubleValueWithCommaSeparator(
                    offerItem.offer_price!!.toDouble()
                )
            }"
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
            offeritemView.iv_edit.setSafeOnClickListener {
                onOffersItemClick.onOfferEdit(position)
            }

            offeritemView.iv_delete.setSafeOnClickListener {
                onOffersItemClick.onOfferDelete(position)
            }

            offeritemView.setSafeOnClickListener {
                onOffersItemClick.onOfferView(position)
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
package com.dev.heenasupplier.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.models.BookingItem
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class CurrentBookingsAdapter(private val context: Context,
                             private val bookingList: ArrayList<BookingItem>,
                             private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<CurrentBookingsAdapter.CurrentBookingsAdapterVH>() {
    inner class CurrentBookingsAdapterVH(val bookingItemView : View) : RecyclerView.ViewHolder(bookingItemView){
        fun bind(bookingItem: BookingItem, position: Int, context: Context) {
            val bookingId = "BOOK#" + bookingItem.booking_id
            bookingItemView.tv_bookingId.text = bookingId
            bookingItemView.tv_booking_service.text = bookingItem.service!!.name
            bookingItemView.tv_price.text = "AED "+bookingItem.price.toString()
            bookingItemView.tv_service_date.text = "AED "+bookingItem.booking_date.toString()
            Glide.with(context).load(bookingItem.user!!.image).into(bookingItemView.civ_supplierImg)

            when(bookingItem.status){
                1 -> bookingItemView.tv_pending.apply {
                    this.text = context.getString(R.string.accepted)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                2 -> bookingItemView!!.tv_pending.apply {
                    this.text = context.getString(R.string.cancelled)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF0909"))
                }
                3 -> bookingItemView!!.tv_pending.apply {
                    this.text = context.getString(R.string.completed)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                4 -> bookingItemView!!.tv_pending.apply {
                    this.text = context.getString(R.string.pending)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF9F54"))
                }
            }

            bookingItemView.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentBookingsAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.current_booking_recycler_items_listing, parent, false)
        return CurrentBookingsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: CurrentBookingsAdapterVH, position: Int) {
        val bookingItem = bookingList[position]
        holder.bind(bookingItem, position, context)
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }
}
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
import kotlinx.android.synthetic.main.booking_history_recycler_items_listing.view.*

class BookingHistoryAdapter(private val context: Context,
                            private val bookingList: ArrayList<BookingItem>, private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<BookingHistoryAdapter.BookingHistoryAdapterVH>() {
    inner class BookingHistoryAdapterVH(val bookingHistoryItemView : View) : RecyclerView.ViewHolder(bookingHistoryItemView){
        fun bind(bookingItem: BookingItem, position: Int) {
            val bookingId = "BOOK#" + bookingItem.booking_id
            bookingHistoryItemView.tv_bookingId.text = bookingId
            bookingHistoryItemView.tv_service.text = bookingItem.service!!.name
            bookingHistoryItemView.tv_price.text = bookingItem.price
            bookingHistoryItemView.tv_date.text = bookingItem.booking_date

            when(bookingItem.status){
                1 -> bookingHistoryItemView.tv_pending.apply {
                    this.text = context.getString(R.string.accepted)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                2 -> bookingHistoryItemView.tv_pending.apply {
                    this.text = context.getString(R.string.cancelled)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF0909"))
                }
                3 -> bookingHistoryItemView.tv_pending.apply {
                    this.text = context.getString(R.string.completed)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                4 -> bookingHistoryItemView.tv_pending.apply {
                    this.text = context.getString(R.string.pending)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF9F54"))
                }
            }

            bookingHistoryItemView.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingHistoryAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.booking_history_recycler_items_listing, parent, false)
        return BookingHistoryAdapterVH(view)
    }

    override fun onBindViewHolder(holder: BookingHistoryAdapterVH, position: Int) {
        val bookingItem = bookingList[position]
        holder.bind(bookingItem, position)
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }
}
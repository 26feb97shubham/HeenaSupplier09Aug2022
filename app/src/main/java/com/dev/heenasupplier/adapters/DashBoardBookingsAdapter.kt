package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.models.BookingItem
import kotlinx.android.synthetic.main.layout_current_bokings_item.view.*

class DashBoardBookingsAdapter(
        private val context: Context,
        private val bookingList: ArrayList<BookingItem>,
        private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick
) : RecyclerView.Adapter<DashBoardBookingsAdapter.DashBoardBookingsAdapterVH>() {
    inner class DashBoardBookingsAdapterVH(val dashboarditemView : View) : RecyclerView.ViewHolder(dashboarditemView){
        fun bind(bookingItem: BookingItem, position: Int, context: Context) {
            val bookingId = "BOOK#" + bookingItem.booking_id
            dashboarditemView.tv_booking_id.text = bookingId
//            dashboarditemView.tv_service_name.text = bookingItem.service!!.name
            val price = "AED " + bookingItem.bks_price
            dashboarditemView.tv_service_price.text = price
            dashboarditemView.tv_service_date_booking.text = bookingItem.date
            Glide.with(context).load(bookingItem.user!!.image).into(dashboarditemView.civ_profile)

            dashboarditemView.setOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashBoardBookingsAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.layout_current_bokings_item, parent, false)
        return DashBoardBookingsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: DashBoardBookingsAdapterVH, position: Int) {
        val bookingItem = bookingList[position]
        holder.bind(bookingItem, position, context)
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }
}
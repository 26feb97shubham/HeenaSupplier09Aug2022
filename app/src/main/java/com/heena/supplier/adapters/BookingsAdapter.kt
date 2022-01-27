package com.heena.supplier.adapters

import android.content.Context
import android.graphics.Color
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
import com.heena.supplier.models.BookingItem
import com.heena.supplier.utils.Utility.Companion.booking_item_type
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.booking_history_recycler_items_listing.view.*
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.*
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.tv_bookingId
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.tv_pending
import kotlinx.android.synthetic.main.current_booking_recycler_items_listing.view.tv_price

class BookingsAdapter(private val context: Context,
                      private val bookingList: ArrayList<BookingItem>,
                      private val onRecyclerItemClick: ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (booking_item_type==1){
            val view  =
                    LayoutInflater.from(context).inflate(R.layout.current_booking_recycler_items_listing, parent, false)
            return CurrentBookingVH(view)
        }else if (booking_item_type == 2){
            val view  =
                    LayoutInflater.from(context).inflate(R.layout.booking_history_recycler_items_listing, parent, false)
            return HistoryBookingVH(view)
        }else{
            val view  =
                    LayoutInflater.from(context).inflate(R.layout.current_booking_recycler_items_listing, parent, false)
            return CurrentBookingVH(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bookingItem = bookingList[position]
        if (booking_item_type==1){
            val currentBookingHolder = holder as CurrentBookingVH
            currentBookingHolder.bind(bookingItem, position, context)
        }else if (booking_item_type==2){
            val historyBookingHolder = holder as HistoryBookingVH
            historyBookingHolder.bind(bookingItem, position)
        }else{
            val currentBookingHolder = holder as CurrentBookingVH
            currentBookingHolder.bind(bookingItem, position, context)
        }
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    inner class CurrentBookingVH(private  val current_booking_item_View : View) : RecyclerView.ViewHolder(current_booking_item_View){
        fun bind(bookingItem: BookingItem, position: Int, context: Context) {
            val bookingId = "BOOK#" + bookingItem.booking_id
            current_booking_item_View.tv_bookingId.text = bookingId
            current_booking_item_View.tv_booking_service.text = bookingItem.service!!.name
            current_booking_item_View.tv_price.text = "AED "+bookingItem.price.toString()
            current_booking_item_View.tv_service_date.text = bookingItem.booking_date.toString()
            Glide.with(context).load(bookingItem.user!!.image)
                .listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        current_booking_item_View.supplier_img_loader.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        current_booking_item_View.supplier_img_loader.visibility = View.GONE
                        return false
                    }

                })
                .into(current_booking_item_View.civ_supplierImg)

            when(bookingItem.status){
                1 -> current_booking_item_View.tv_pending.apply {
                    this.text = context.getString(R.string.accepted)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                2 -> current_booking_item_View.tv_pending.apply {
                    this.text = context.getString(R.string.cancelled)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF0909"))
                }
                3 -> current_booking_item_View.tv_pending.apply {
                    this.text = context.getString(R.string.completed)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                4 -> current_booking_item_View.tv_pending.apply {
                    this.text = context.getString(R.string.pending)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF9F54"))
                }
            }

            current_booking_item_View.setSafeOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }

    }

    inner class HistoryBookingVH(private val history_booking_item_view : View) : RecyclerView.ViewHolder(history_booking_item_view){
        fun bind(bookingItem: BookingItem, position: Int) {
            val bookingId = "BOOK#" + bookingItem.booking_id
            history_booking_item_view.tv_bookingId.text = bookingId
            history_booking_item_view.tv_service.text = bookingItem.service!!.name
            history_booking_item_view.tv_price.text = bookingItem.price
            history_booking_item_view.tv_date.text = bookingItem.booking_date

            when(bookingItem.status){
                1 -> history_booking_item_view.tv_pending.apply {
                    this.text = context.getString(R.string.accepted)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                2 -> history_booking_item_view.tv_pending.apply {
                    this.text = context.getString(R.string.cancelled)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF0909"))
                }
                3 -> history_booking_item_view.tv_pending.apply {
                    this.text = context.getString(R.string.completed)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#37CC37"))
                }
                4 -> history_booking_item_view.tv_pending.apply {
                    this.text = context.getString(R.string.pending)
                    this.isAllCaps = true
                    this.setTextColor(Color.parseColor("#FF9F54"))
                }
            }

            history_booking_item_view.setSafeOnClickListener {
                onRecyclerItemClick.OnClickAction(position)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return booking_item_type
    }
}
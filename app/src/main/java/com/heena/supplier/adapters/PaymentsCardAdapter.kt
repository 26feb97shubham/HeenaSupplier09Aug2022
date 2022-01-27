package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.Cards
import com.heena.supplier.utils.Utility.Companion.mSelectedItem
import kotlinx.android.synthetic.main.item_cards_list.view.*

class PaymentsCardAdapter(private val  context: Context, private val data:ArrayList<Cards>, private val click: ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<PaymentsCardAdapter.PaymentsCardAdapterVH>() {
    inner class PaymentsCardAdapterVH(itemVIew : View) : RecyclerView.ViewHolder(itemVIew){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsCardAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.item_cards_list, parent, false)
        return PaymentsCardAdapterVH(view)
    }

    override fun onBindViewHolder(holder: PaymentsCardAdapterVH, position: Int) {
        holder.itemView.tv_card_title.text = data[position].brand
        val card_ending = "Ending in "+data[position].number
        holder.itemView.tv_ending_card_number.text = card_ending
        holder.itemView.iv_selected_unselected.isChecked = position==mSelectedItem

        holder.itemView.iv_selected_unselected.setOnClickListener {
            mSelectedItem = holder.absoluteAdapterPosition
            click.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
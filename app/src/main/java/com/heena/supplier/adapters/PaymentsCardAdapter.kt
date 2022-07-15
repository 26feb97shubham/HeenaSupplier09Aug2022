package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.Cards
import com.heena.supplier.utils.Utility.mSelectedItem
import kotlinx.android.synthetic.main.item_cards_list.view.*

class PaymentsCardAdapter(private val  context: Context, private val data:ArrayList<Cards>, private val click: ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<PaymentsCardAdapter.PaymentsCardAdapterVH>() {
    private var lastCheckedPosition = -1
    inner class PaymentsCardAdapterVH(itemVIew : View) : RecyclerView.ViewHolder(itemVIew){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsCardAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.item_cards_list, parent, false)
        return PaymentsCardAdapterVH(view)
    }

    override fun onBindViewHolder(holder: PaymentsCardAdapterVH, position: Int) {
        holder.itemView.tv_card_title.text = data[position].brand
        val card_ending = context.getString(R.string.ending_in)+" "+data[position].number
        holder.itemView.tv_ending_card_number.text = card_ending
        holder.itemView.iv_selected_unselected.isChecked = position==lastCheckedPosition

        holder.itemView.iv_selected_unselected.setOnClickListener {
            val copyOfLastCheckedPosition = lastCheckedPosition
            lastCheckedPosition = holder.bindingAdapterPosition
            notifyItemChanged(copyOfLastCheckedPosition)
            notifyItemChanged(lastCheckedPosition)
            click.OnClickAction(lastCheckedPosition)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
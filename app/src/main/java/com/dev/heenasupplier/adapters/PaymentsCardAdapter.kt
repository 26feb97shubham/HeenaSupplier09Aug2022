package com.dev.heenasupplier.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import kotlinx.android.synthetic.main.item_cards_list.view.*

class PaymentsCardAdapter(private val  context: Context, private val click: ClickInterface.OnRecyclerItemClick) : RecyclerView.Adapter<PaymentsCardAdapter.PaymentsCardAdapterVH>() {
    inner class PaymentsCardAdapterVH(itemVIew : View) : RecyclerView.ViewHolder(itemVIew){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsCardAdapterVH {
        val view  =
                LayoutInflater.from(context).inflate(R.layout.item_cards_list, parent, false)
        return PaymentsCardAdapterVH(view)
    }

    override fun onBindViewHolder(holder: PaymentsCardAdapterVH, position: Int) {
        holder.itemView.setOnClickListener {
            val drawable = context.resources.getDrawable(R.drawable.selected_radio_button)
            holder.itemView.iv_selected_unselected.setImageDrawable(drawable)
            click.OnClickAction(position)
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}
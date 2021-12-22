package com.heena.supplier.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.cards_listing.view.*

class MyCardsAdapter(private val context: Context) :
    RecyclerView.Adapter<MyCardsAdapter.MyCardsAdapterVH>() {
    inner class MyCardsAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCardsAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.cards_listing, parent, false)
        return MyCardsAdapterVH(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: MyCardsAdapterVH, position: Int) {
        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "").equals("en")){
            val drawable = context.resources.getDrawable(R.drawable.card_bg)
            holder.itemView.ll_card.background = drawable
        }else{
            val drawable = context.resources.getDrawable(R.drawable.arabic_card)
            holder.itemView.ll_card.background = drawable
        }
    }

    override fun getItemCount(): Int {
       return 3
    }
}
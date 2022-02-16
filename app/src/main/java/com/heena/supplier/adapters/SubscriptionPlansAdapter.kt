package com.heena.supplier.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.SubscriptionPlan
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.layout_subscription_plan_items.view.*

class SubscriptionPlansAdapter(private val context : Context, private val subscriptionsPlansList : ArrayList<SubscriptionPlan>,
                               private val click: ClickInterface.OnRecyclerItemClick)
    : RecyclerView.Adapter<SubscriptionPlansAdapter.SubscriptionPlansAdapterVH>() {
    private var checkedPosition : Int = -1
    init {
        setHasStableIds(true)
    }
    inner class SubscriptionPlansAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.iv_selected_unselected_subscription.visibility = View.GONE
        }
        fun onBindData() {
            itemView.tv_subscription_title.text = subscriptionsPlansList[position].name
            itemView.tv_subscription_price.text = "AED "+ Utility.convertDoubleValueWithCommaSeparator(
                subscriptionsPlansList[position].amount!!.toDouble()
            )
            itemView.tv_subscription_desc.text = subscriptionsPlansList[position].description

            itemView.rlSubscription.setOnClickListener {
                itemView.iv_selected_unselected_subscription.visibility = View.VISIBLE
                if (checkedPosition!=adapterPosition){
                    notifyItemChanged(checkedPosition)
                    checkedPosition = adapterPosition
                }
                click.OnClickAction(position)
            }


            if(checkedPosition==-1){
                itemView.iv_selected_unselected_subscription.visibility = View.GONE
            }else{
                if (checkedPosition == adapterPosition){
                    itemView.iv_selected_unselected_subscription.visibility = View.VISIBLE
                }else{
                    itemView.iv_selected_unselected_subscription.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionPlansAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.layout_subscription_plan_items, parent, false)
        return SubscriptionPlansAdapterVH(view)
    }

    override fun onBindViewHolder(holder: SubscriptionPlansAdapterVH, position: Int) {
        holder.onBindData()
    }

    override fun getItemCount(): Int {
        return subscriptionsPlansList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getSelected() : SubscriptionPlan? {
        if (checkedPosition!=-1){
            Log.e("pos", ""+checkedPosition)
            return subscriptionsPlansList[checkedPosition]
        }
        return null
    }
}
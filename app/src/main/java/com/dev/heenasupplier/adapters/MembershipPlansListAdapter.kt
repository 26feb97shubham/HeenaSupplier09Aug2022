package com.dev.heenasupplier.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.extras.MembershipItemsDetailsLookUp
import com.dev.heenasupplier.models.Membership
import com.dev.heenasupplier.models.MembershipPlan
import kotlinx.android.synthetic.main.membership_plan_items.view.*

class MembershipPlansListAdapter(private val context: Context,
                                 private val membershipList: ArrayList<Membership>,
                                 private val click: ClickInterface.OnRecyclerItemClick)
    : RecyclerView.Adapter<MembershipPlansListAdapter.MembershipPlansListAdapterVH>() {
    private var checkedPosition : Int = -1
    init {
        setHasStableIds(true)
    }
    inner class MembershipPlansListAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.iv_selected_unselected.visibility = View.GONE
        }

        fun getMembershipDetails() : ItemDetailsLookup.ItemDetails<Long> = object :
            ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition

            override fun getSelectionKey(): Long?  = itemId

        }

        fun onBindData() {
            itemView.tv_membership_title.text = membershipList[position].name
            itemView.tv_membership_plan_price.text = membershipList[position].price.toString()
            itemView.tv_membership_desc.text = membershipList[position].description

            itemView.setOnClickListener {
                itemView.iv_selected_unselected.visibility = View.VISIBLE
                if (checkedPosition!=adapterPosition){
                    notifyItemChanged(checkedPosition)
                    checkedPosition = adapterPosition
                }
                click.OnClickAction(position)
            }


            if(checkedPosition==-1){
                itemView.iv_selected_unselected.visibility = View.GONE
            }else{
                if (checkedPosition == adapterPosition){
                    itemView.iv_selected_unselected.visibility = View.VISIBLE
                }else{
                    itemView.iv_selected_unselected.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembershipPlansListAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.membership_plan_items, parent, false)
        return MembershipPlansListAdapterVH(view)
    }

    override fun onBindViewHolder(holder: MembershipPlansListAdapterVH, position: Int) {
       holder.onBindData()
    }

    override fun getItemCount(): Int {
        return membershipList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getSelected() : Membership? {
        if (checkedPosition!=-1){
            Log.e("pos", ""+checkedPosition)
            return membershipList.get(checkedPosition)
        }
        return null
    }
}
package com.heena.supplier.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.bottomsheets.MembershipBottomSheetDialogFragment
import com.heena.supplier.models.Membership
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.membership_plan_items.view.*
import kotlin.collections.ArrayList

class MembershipPlansListAdapter(private val context: Context,
                                 private val membershipList: ArrayList<Membership>,
                                 private val click: ClickInterface.OnRecyclerItemClick)
    : RecyclerView.Adapter<MembershipPlansListAdapter.MembershipPlansListAdapterVH>() {
    var mSelectedItem = -1
    inner class MembershipPlansListAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.iv_selected_unselected.visibility = View.GONE
        }

        fun onBindData(position: Int) {
            itemView.tv_membership_title.text = membershipList[position].name
            itemView.tv_membership_plan_price.text = "AED "+ Utility.convertDoubleValueWithCommaSeparator(
                membershipList[position].price!!.toDouble()
            )
            itemView.tv_membership_desc.text = membershipList[position].description

            if (position == mSelectedItem){
                itemView.iv_selected_unselected.visibility = View.VISIBLE
            }else{
                itemView.iv_selected_unselected.visibility = View.GONE
            }

            itemView.setSafeOnClickListener {
                mSelectedItem =adapterPosition
                notifyDataSetChanged()
                click.OnClickAction(mSelectedItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembershipPlansListAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.membership_plan_items, parent, false)
        return MembershipPlansListAdapterVH(view)
    }

    override fun onBindViewHolder(holder: MembershipPlansListAdapterVH, position: Int) {
        holder.onBindData(position)
    }

    override fun getItemCount(): Int {
        return membershipList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getSelected() : Membership? {
        if (mSelectedItem!=-1){
            Log.e("pos", ""+mSelectedItem)
            return membershipList.get(mSelectedItem)
        }
        return null
    }
}
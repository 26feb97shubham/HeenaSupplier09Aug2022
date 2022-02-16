package com.heena.supplier.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.Membership
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.registration_membership_plan_items.view.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class RegistrationMembershipPlansListAdapter(private val context: Context,
                                             private val membershipList: ArrayList<Membership>,
                                             private val click: ClickInterface.OnRecyclerItemClick)
    : RecyclerView.Adapter<RegistrationMembershipPlansListAdapter.RegistrationMembershipPlansListAdapterVH>() {
    private var checkedPosition : Int = -1
    init {
        setHasStableIds(true)
    }
    inner class RegistrationMembershipPlansListAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.iv_selected_unselected_registration.visibility = View.GONE
        }

        fun getMembershipDetails() : ItemDetailsLookup.ItemDetails<Long> = object :
            ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition

            override fun getSelectionKey(): Long  = itemId

        }

        fun onBindData() {
            itemView.tv_registration_plan_title.text = membershipList[position].name
            itemView.tv_registration_plan_price.text = "AED " + Utility.convertDoubleValueWithCommaSeparator(
                membershipList[position].price!!.toDouble()
            )
            itemView.tv_registration_plan_description.text = membershipList[position].description

            itemView.setSafeOnClickListener {
                itemView.iv_selected_unselected_registration.visibility = View.VISIBLE
                if (checkedPosition!=adapterPosition){
                    notifyItemChanged(checkedPosition)
                    checkedPosition = adapterPosition
                }
                click.OnClickAction(position)
            }


            if(checkedPosition==-1){
                itemView.iv_selected_unselected_registration.visibility = View.GONE
            }else{
                if (checkedPosition == adapterPosition){
                    itemView.iv_selected_unselected_registration.visibility = View.VISIBLE
                }else{
                    itemView.iv_selected_unselected_registration.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RegistrationMembershipPlansListAdapterVH {
        val view  = LayoutInflater.from(context).inflate(R.layout.registration_membership_plan_items, parent, false)
        return RegistrationMembershipPlansListAdapterVH(view)
    }

    override fun onBindViewHolder(holder: RegistrationMembershipPlansListAdapterVH, position: Int) {
        holder.onBindData()
    }

    override fun getItemCount(): Int {
        return membershipList.size
    }

    fun getSelected() : Membership? {
        if (checkedPosition!=-1){
            Log.e("pos", ""+checkedPosition)
            return membershipList.get(checkedPosition)
        }
        return null
    }
}
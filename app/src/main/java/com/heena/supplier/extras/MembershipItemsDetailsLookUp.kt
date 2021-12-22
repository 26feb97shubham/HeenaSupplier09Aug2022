package com.heena.supplier.extras

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.adapters.MembershipPlansListAdapter

class MembershipItemsDetailsLookUp(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>(){
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view!=null){
            return (recyclerView.getChildViewHolder(view) as MembershipPlansListAdapter.MembershipPlansListAdapterVH).getMembershipDetails()
        }
        return null
    }
}
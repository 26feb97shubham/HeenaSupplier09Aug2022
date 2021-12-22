package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.HelpSubCategory
import kotlinx.android.synthetic.main.sub_category_items.view.*


class SubHelpCategoryAdapter(private val context : Context, private val subCategoryList : ArrayList<HelpSubCategory>, private val subhelpCategoryClicked: ClickInterface.subhelpCategoryClicked)
    : RecyclerView.Adapter<SubHelpCategoryAdapter.SubHelpCategoryAdapterVH>() {
    inner class SubHelpCategoryAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(helpSubCategory: HelpSubCategory, position: Int) {
            itemView.text_sub_category.text = helpSubCategory.title
            itemView.card_sub_category.setOnClickListener {
                subhelpCategoryClicked.subHelpCategory(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubHelpCategoryAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.sub_category_items, parent, false)
        return SubHelpCategoryAdapterVH(view)
    }

    override fun onBindViewHolder(holder: SubHelpCategoryAdapterVH, position: Int) {
        holder.bind(subCategoryList[position], position)
    }

    override fun getItemCount(): Int {
        return subCategoryList.size
    }
}
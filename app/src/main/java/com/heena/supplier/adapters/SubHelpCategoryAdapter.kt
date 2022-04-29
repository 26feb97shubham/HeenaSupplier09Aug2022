package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.HelpSubCategory
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.content
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.sub_category_items.view.*


class SubHelpCategoryAdapter(private val context : Context, private val subCategoryList : ArrayList<HelpSubCategory>, private val subhelpCategoryClicked: ClickInterface.subhelpCategoryClicked)
    : RecyclerView.Adapter<SubHelpCategoryAdapter.SubHelpCategoryAdapterVH>() {
    private var sub_help_category : HelpSubCategory?=null
    inner class SubHelpCategoryAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(helpSubCategory: HelpSubCategory, position: Int) {
            val subTitle = if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                helpSubCategory.title_ar
            }else{
                helpSubCategory.title
            }
            itemView.text_sub_category.text = subTitle
            itemView.card_sub_category.setSafeOnClickListener {
                sub_help_category = subCategoryList[position]
                content = sub_help_category!!.content
                Utility.helpSubCategory = sub_help_category
                subhelpCategoryClicked.subHelpCategory(position,sub_help_category!!.content)
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
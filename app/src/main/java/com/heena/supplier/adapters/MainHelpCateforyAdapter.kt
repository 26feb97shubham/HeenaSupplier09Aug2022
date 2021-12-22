package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.HelpCategory
import kotlinx.android.synthetic.main.main_category_list_item.view.*

class MainHelpCateforyAdapter(private val context: Context, private val mainHelpCategoryList : ArrayList<HelpCategory>, private var mainhelpCategoryClicked : ClickInterface.mainhelpCategoryClicked) :
    RecyclerView.Adapter<MainHelpCateforyAdapter.MainHelpCateforyAdapterVH>() {
    inner class MainHelpCateforyAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(helpCategory: HelpCategory, position: Int) {
            itemView.text_main_category.text = helpCategory.title
            itemView.card_main_category.setOnClickListener {
                mainhelpCategoryClicked.mainHelpCategory(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHelpCateforyAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.main_category_list_item, parent, false)
        return MainHelpCateforyAdapterVH(view)
    }

    override fun onBindViewHolder(holder: MainHelpCateforyAdapterVH, position: Int) {
        holder.bind(mainHelpCategoryList[position], position)
    }

    override fun getItemCount(): Int {
        return mainHelpCategoryList.size
    }
}
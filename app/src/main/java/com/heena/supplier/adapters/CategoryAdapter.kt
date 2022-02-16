package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.CategoryItem
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.spinner_layout.view.*

class CategoryAdapter(private val context: Context,
private val categoryList : ArrayList<CategoryItem>,
private val OnCategoryItemClick : ClickInterface.OnCategoryItemClick
) : RecyclerView.Adapter<CategoryAdapter.CategoryAdapterVH>() {
    inner class CategoryAdapterVH(private val itemView:View) : RecyclerView.ViewHolder(itemView){
        fun bind(categoryItem: CategoryItem, position: Int) {
            itemView.spinner_item.text = categoryItem.name
            itemView.setSafeOnClickListener {
                OnCategoryItemClick.OnClickAction(position, categoryItem)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.spinner_layout, parent, false)
        return CategoryAdapterVH(view)
    }

    override fun onBindViewHolder(holder: CategoryAdapterVH, position: Int) {
        val categoryItem = categoryList[position]
        holder.bind(categoryItem, position)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }
}
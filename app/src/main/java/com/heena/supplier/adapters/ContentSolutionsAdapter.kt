package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.Content
import com.heena.supplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.solutions_items.view.*

class ContentSolutionsAdapter(private val context: Context, private val solutionsList  :ArrayList<Content>) :
    RecyclerView.Adapter<ContentSolutionsAdapter.ContentSolutionsAdapterVH>() {
    inner class ContentSolutionsAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind(content: Content) {
            val content = if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                content.content_ar
            }else{
                content.content
            }
            itemView.tv_solutions.text=content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentSolutionsAdapterVH {
        val view  =
            LayoutInflater.from(context).inflate(R.layout.solutions_items, parent, false)
        return ContentSolutionsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: ContentSolutionsAdapterVH, position: Int) {
        holder.bind(solutionsList[position])
    }

    override fun getItemCount(): Int {
        return solutionsList.size
    }
}
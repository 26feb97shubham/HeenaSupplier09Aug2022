package com.heena.supplier.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.Content
import com.heena.supplier.models.HelpCategory
import com.heena.supplier.models.HelpSubCategory
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import kotlinx.android.synthetic.main.main_category_list_item.view.*

class MainHelpCateforyAdapter(
    private val context: Context,
    private val mainHelpCategoryList: ArrayList<HelpCategory>,
    private val admin_id: Int,
    private var mainhelpCategoryClicked: ClickInterface.mainhelpCategoryClicked,
    private val findNavController: NavController
) :
    RecyclerView.Adapter<MainHelpCateforyAdapter.MainHelpCateforyAdapterVH>() {
    private var sub_help_category_list = ArrayList<HelpSubCategory>()
    private var subHelpCategoryAdapter : SubHelpCategoryAdapter?=null
    private var openPosition = -1
    inner class MainHelpCateforyAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(helpCategory: HelpCategory, position: Int) {
            val title = if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                helpCategory.title_ar
            }else{
                helpCategory.title
            }
            itemView.text_main_category.text = title
            itemView.text_main_category.setOnClickListener {
                mainhelpCategoryClicked.mainHelpCategory(position)
                if (openPosition == position) {
                    openPosition = -1
                } else {
                    openPosition = position
                }
                notifyDataSetChanged()
            }

            if(position == openPosition) {
                if (helpCategory == null) {
                    itemView.card_sub_category.visibility = View.VISIBLE
                    itemView.text_no_sub_categories_found.visibility = View.VISIBLE
                    itemView.rv_sub_category.visibility = View.GONE
                } else {
                    itemView.card_sub_category.visibility = View.VISIBLE
                    if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang,"").equals("ar")){
                        val drawable = context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24)
                        itemView.text_main_category.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null, null)
                    }else{
                        val drawable = context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24)
                        itemView.text_main_category.setCompoundDrawablesWithIntrinsicBounds(null,null,drawable, null)
                    }
                    if (helpCategory.help_sub_category.isEmpty()) {
                        itemView.text_no_sub_categories_found.visibility = View.VISIBLE
                        itemView.rv_sub_category.visibility = View.GONE
                    } else {
                        itemView.text_no_sub_categories_found.visibility = View.GONE
                        itemView.rv_sub_category.visibility = View.VISIBLE
                    }
                }

            }else {
                if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang,"").equals("ar")){
                    val drawable = context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_right_24)
                    itemView.text_main_category.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null, null)
                    itemView.card_sub_category.visibility = View.GONE
                }else{
                    val drawable = context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_right_24)
                    itemView.text_main_category.setCompoundDrawablesWithIntrinsicBounds(null,null,drawable, null)
                    itemView.card_sub_category.visibility = View.GONE
                }
            }

            itemView.rv_sub_category.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            sub_help_category_list = helpCategory.help_sub_category as ArrayList<HelpSubCategory>
            subHelpCategoryAdapter = SubHelpCategoryAdapter(context,sub_help_category_list, object :
                ClickInterface.subhelpCategoryClicked{
                override fun subHelpCategory(position: Int, content: ArrayList<Content>) {
                    Utility.content = content
                    val bundle  = Bundle()
                    bundle.putInt("admin_id", admin_id)
                    findNavController.navigate(R.id.helpFragment2, bundle)
                }
            })
            itemView.rv_sub_category.adapter = subHelpCategoryAdapter

            if(sub_help_category_list.size==0){
                itemView.text_no_sub_categories_found.visibility = View.VISIBLE
                itemView.rv_sub_category.visibility = View.GONE
            }else{
                itemView.text_no_sub_categories_found.visibility = View.GONE
                itemView.rv_sub_category.visibility = View.VISIBLE
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
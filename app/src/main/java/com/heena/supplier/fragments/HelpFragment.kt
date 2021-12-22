package com.heena.supplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.ContentSolutionsAdapter
import com.heena.supplier.adapters.MainHelpCateforyAdapter
import com.heena.supplier.adapters.SubHelpCategoryAdapter
import com.heena.supplier.models.Content
import com.heena.supplier.models.DashHelpCategoryResponse
import com.heena.supplier.models.HelpCategory
import com.heena.supplier.models.HelpSubCategory
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import kotlinx.android.synthetic.main.fragment_help.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpFragment : Fragment() {
    private var mView : View?=null
    private var main_help_category_list = ArrayList<HelpCategory>()
    private var main_help_category : HelpCategory?=null
    private var sub_help_category_list = ArrayList<HelpSubCategory>()
    private var sub_help_category : HelpSubCategory?=null
    private var solutionsList = ArrayList<Content>()
    private var mainHelpCategoryAdapter : MainHelpCateforyAdapter?=null
    private var subHelpCategoryAdapter : SubHelpCategoryAdapter?=null
    private var contentSolutionsAdapter : ContentSolutionsAdapter?=null
    var room: String = ""
    var admin_id = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView =inflater.inflate(R.layout.fragment_help, container, false)
        mView!!.web_view_faq.visibility = View.GONE
        mView!!.rv_help_text.visibility = View.VISIBLE
        mView!!.card_sub_category_one.visibility = View.GONE
        mView!!.solutions_content_rv.visibility = View.GONE
        mView!!.ll_need_more_help.visibility = View.GONE
        mView!!.mtv_help_desc.text=requireContext().getString(R.string.good_day_nhow_can_we_help_you_today)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHelp()

        mView!!.faq_card.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("title", requireContext().getString(R.string.frequently_asked_questions))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        mView!!.admin_chat_card.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("helpCategory", main_help_category)
            bundle.putSerializable("subhelpCategory", sub_help_category)
            val user_id = SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]
            room = if (user_id > admin_id) {
                StringBuilder().append(admin_id).append('-')
                    .append(user_id).toString()
            } else {
                StringBuilder().append(user_id).append('-')
                    .append(admin_id).toString()
            }
            bundle.putString("room", room)
            bundle.putString("type", "direct")
            bundle.putString("admin_id", ""+admin_id)
            findNavController().navigate(R.id.chatWithAdminFragment, bundle)
        }
    }

    private fun showHelp() {
        mView!!.frag_help_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(arrayOf("user_id","lang"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(),
                SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.dashHelpCategory(builder.build())
        call!!.enqueue(object : Callback<DashHelpCategoryResponse?> {
            override fun onResponse(
                call: Call<DashHelpCategoryResponse?>,
                response: Response<DashHelpCategoryResponse?>
            ) {
                mView!!.frag_help_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        main_help_category_list = response.body()!!.help_category as ArrayList<HelpCategory>
                        setMainHelpCategoryAdapter(main_help_category_list)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), requireContext().getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<DashHelpCategoryResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.frag_help_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun setMainHelpCategoryAdapter(mainHelpCategoryList: ArrayList<HelpCategory>) {
        mView!!.rv_help_text.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL, false)
        mainHelpCategoryAdapter = MainHelpCateforyAdapter(requireContext(), mainHelpCategoryList, object : ClickInterface.mainhelpCategoryClicked{
            override fun mainHelpCategory(position: Int) {
                main_help_category = mainHelpCategoryList[position]
                for (i in 0 until mainHelpCategoryList.size){
                    if (!mainHelpCategoryList[i].isShow){
                        mView!!.rv_help_text.visibility = View.GONE
                        mView!!.card_sub_category_one.visibility = View.VISIBLE
                    }else{
                        mView!!.rv_help_text.visibility = View.VISIBLE
                        mView!!.card_sub_category_one.visibility = View.GONE
                    }
                }
                mView!!.sub_category_title.text = mainHelpCategoryList[position].title
                mView!!.rv_sub_category.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                sub_help_category_list = mainHelpCategoryList[position].help_sub_category as ArrayList<HelpSubCategory>
                subHelpCategoryAdapter = SubHelpCategoryAdapter(requireContext(),sub_help_category_list, object :
                    ClickInterface.subhelpCategoryClicked{
                    override fun subHelpCategory(position: Int) {
                        sub_help_category = sub_help_category_list[position]
                        mView!!.mtv_help_desc.text=requireContext().getString(R.string.sorry_to_hear_about_that)
                        solutionsList = sub_help_category_list[position].content
                        mView!!.rv_help_text.visibility = View.GONE
                        mView!!.card_sub_category_one.visibility = View.GONE
                        mView!!.solutions_content_rv.visibility = View.VISIBLE
                        mView!!.ll_need_more_help.visibility = View.VISIBLE
                        mView!!.solutions_content_rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        contentSolutionsAdapter = ContentSolutionsAdapter(requireContext(), solutionsList)
                        mView!!.solutions_content_rv.adapter = contentSolutionsAdapter
                        contentSolutionsAdapter!!.notifyDataSetChanged()

                    }
                })
                mView!!.rv_sub_category.adapter = subHelpCategoryAdapter
                subHelpCategoryAdapter!!.notifyDataSetChanged()
            }

        })
        mView!!.rv_help_text.adapter = mainHelpCategoryAdapter
        mainHelpCategoryAdapter!!.notifyDataSetChanged()

    }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}
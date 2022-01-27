package com.heena.supplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.MainHelpCateforyAdapter
import com.heena.supplier.models.DashHelpCategoryResponse
import com.heena.supplier.models.HelpCategory
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import com.heena.supplier.utils.Utility.Companion.helpCategory
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_help.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpFragment : Fragment() {
    private var mView : View?=null
    private var main_help_category_list = ArrayList<HelpCategory>()
    private var main_help_category : HelpCategory?=null
    private var mainHelpCategoryAdapter : MainHelpCateforyAdapter?=null
    var admin_id = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView =inflater.inflate(R.layout.fragment_help, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        mView!!.rv_help_text.visibility = View.VISIBLE
        mView!!.mtv_help_desc.text=requireContext().getString(R.string.good_day_nhow_can_we_help_you_today)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        showHelp()
    }

    private fun showHelp() {
        mView!!.frag_help_progressBar.visibility = View.VISIBLE
        val builder = APIClient.createBuilder(arrayOf("user_id","lang"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(),
                SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.dashHelpCategory(builder.build())
        call!!.enqueue(object : Callback<DashHelpCategoryResponse?>{
            override fun onResponse(
                call: Call<DashHelpCategoryResponse?>,
                response: Response<DashHelpCategoryResponse?>
            ) {
                mView!!.frag_help_progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        admin_id = response.body()!!.admin_id
                        main_help_category_list.clear()
                        main_help_category_list = response.body()!!.help_category as ArrayList<HelpCategory>
                        setMainHelpCategoryAdapter(main_help_category_list, admin_id)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), requireContext().getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<DashHelpCategoryResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.frag_help_progressBar.visibility = View.GONE
            }

        })
    }

    private fun setMainHelpCategoryAdapter(
        mainHelpCategoryList: ArrayList<HelpCategory>,
        admin_id: Int
    ) {
        mView!!.rv_help_text.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)
        mainHelpCategoryAdapter = MainHelpCateforyAdapter(requireContext(), mainHelpCategoryList,admin_id, object : ClickInterface.mainhelpCategoryClicked{
            override fun mainHelpCategory(position: Int) {
                main_help_category = mainHelpCategoryList[position]
                helpCategory = main_help_category
            }

        }, findNavController())
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
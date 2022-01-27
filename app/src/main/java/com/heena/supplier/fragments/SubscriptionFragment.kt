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
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.SubscriptionPlansAdapter
import com.heena.supplier.bottomsheets.MembershipBottomSheetDialogFragment
import com.heena.supplier.models.BuySubscriptionResponse
import com.heena.supplier.models.SubscriptionPlan
import com.heena.supplier.models.SubscriptionPlansResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.fragment_subscription.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SubscriptionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubscriptionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mView: View?=null
    private var subscription_plans : ArrayList<SubscriptionPlan>?=null
    private var subscriptionPlansAdapter : SubscriptionPlansAdapter?=null
    private var subscription_id = 0
    private var subscriptionPlan  :SubscriptionPlan?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_subscription, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    showSubscriptions()
                    noInternetDialog.dismiss()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Subscription Fragment")
        }else{
            showSubscriptions()
        }

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
        mView!!.tv_buy_subscription.setSafeOnClickListener {
            if (subscriptionPlansAdapter!!.getSelected()!=null){
                subscription_id = subscriptionPlansAdapter!!.getSelected()!!.subscription_plans_id
                buy_subscription()
            }else{
                LogUtils.shortToast(requireContext(), getString(R.string.please_select_the_membership_plan_first_to_continue))
            }
        }
    }

    private fun showSubscriptions() {
        mView!!.progressBar_subs.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.subscription_plans(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0],
        SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
        call.enqueue(object : Callback<SubscriptionPlansResponse?>{
            override fun onResponse(
                call: Call<SubscriptionPlansResponse?>,
                response: Response<SubscriptionPlansResponse?>
            ) {
                mView!!.progressBar_subs.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        subscription_plans!!.clear()
                        subscription_plans = response.body()!!.subscription_plans
                        mView!!.rv_subscription_plans.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        subscriptionPlansAdapter = SubscriptionPlansAdapter(requireContext(),
                            subscription_plans!!, object : ClickInterface.OnRecyclerItemClick{
                                override fun OnClickAction(position: Int) {
                                    subscription_id  = subscription_plans!![position].subscription_plans_id
                                    subscriptionPlan = subscription_plans!![position]
                                }
                            })

                        mView!!.rv_subscription_plans.adapter = subscriptionPlansAdapter
                        mView!!.pageIndicator_subscription_plans.attachTo(mView!!.rv_subscription_plans)
                        subscriptionPlansAdapter!!.notifyDataSetChanged()
                    }else{
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<SubscriptionPlansResponse?>, t: Throwable) {
                mView!!.progressBar_subs.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.longToast(requireContext(),getString(R.string.response_isnt_successful))
            }

        })
    }

    private fun buy_subscription() {
        mView!!.progressBar_subs.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(arrayOf("lang", "user_id"),
            arrayOf(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,""),
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString()))
        val call = apiInterface.buySubscription(builder.build())
        call?.enqueue(object : Callback<BuySubscriptionResponse?>{
            override fun onResponse(
                call: Call<BuySubscriptionResponse?>,
                response: Response<BuySubscriptionResponse?>
            ) {
                mView!!.progressBar_subs.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                        SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ISFEATURED, true)
                        findNavController().navigate(R.id.homeFragment)
                    }else{
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<BuySubscriptionResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.message)
                mView!!.progressBar_subs.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SubscriptionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SubscriptionFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
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
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.BuySubscriptionResponse
import com.heena.supplier.models.SubscriptionPlan
import com.heena.supplier.models.SubscriptionPlansResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.fragment_subscription.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubscriptionFragment : Fragment() {
    private var mView: View?=null
    private var subscription_plans = ArrayList<SubscriptionPlan>()
    private var subscriptionPlansAdapter : SubscriptionPlansAdapter?=null
    private var subscription_id = 0
    private var subscriptionPlan  :SubscriptionPlan?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_subscription, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
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
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }
        mView!!.tv_buy_subscription.setSafeOnClickListener {
            if (subscriptionPlansAdapter!!.getSelected()!=null){
                subscription_id = subscriptionPlansAdapter!!.getSelected()!!.subscription_plans_id
                val bundle = Bundle()
                bundle.putSerializable("membership", null)
                bundle.putSerializable("subscription", subscriptionPlan)
                bundle.putInt("direction", 2)
                findNavController().navigate(R.id.myPaymentFragment, bundle)
            }else{
                Utility.showSnackBarValidationError(mView!!.subscriptionFragmentConstraintLayout,
                    requireContext().getString(R.string.please_select_the_subscription_plan_first_to_continue),
                    requireContext())
            }
        }
    }

    private fun showSubscriptions() {
        mView!!.progressBar_subs.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.subscription_plans(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0],
        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call.enqueue(object : Callback<SubscriptionPlansResponse?>{
            override fun onResponse(
                call: Call<SubscriptionPlansResponse?>,
                response: Response<SubscriptionPlansResponse?>
            ) {
                mView!!.progressBar_subs.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        subscription_plans.clear()
                        subscription_plans = response.body()!!.subscription_plans
                        mView!!.rv_subscription_plans.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        subscriptionPlansAdapter = SubscriptionPlansAdapter(requireContext(),
                            subscription_plans, object : ClickInterface.OnRecyclerItemClick{
                                override fun OnClickAction(position: Int) {
                                    subscription_id  = subscription_plans[position].subscription_plans_id
                                    subscriptionPlan = subscription_plans[position]
                                }
                            })

                        mView!!.rv_subscription_plans.adapter = subscriptionPlansAdapter
                        mView!!.pageIndicator_subscription_plans.attachTo(mView!!.rv_subscription_plans)
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.subscriptionFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.subscriptionFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<SubscriptionPlansResponse?>, t: Throwable) {
                mView!!.progressBar_subs.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Utility.showSnackBarOnResponseError(mView!!.subscriptionFragmentConstraintLayout,
                    t.message.toString(),
                    requireContext())
            }

        })
    }
}
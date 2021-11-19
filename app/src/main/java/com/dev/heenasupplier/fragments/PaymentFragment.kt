package com.dev.heenasupplier.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.activities.HomeActivity.Companion.myuserId
import com.dev.heenasupplier.activities.LoginActivity
import com.dev.heenasupplier.adapters.PaymentsCardAdapter
import com.dev.heenasupplier.models.BuyMembership
import com.dev.heenasupplier.models.Membership
import com.dev.heenasupplier.models.MembershipX
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.heena.heenasupplier.bottomsheets.MembershipBottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.activity_payment_fragment.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.tv_subscribe
import kotlinx.android.synthetic.main.fragment_payment.*
import kotlinx.android.synthetic.main.fragment_payment.rv_cards_listing
import kotlinx.android.synthetic.main.fragment_payment.tv_add_new_card
import kotlinx.android.synthetic.main.fragment_payment.tv_membership_desc
import kotlinx.android.synthetic.main.fragment_payment.tv_membership_plan_price
import kotlinx.android.synthetic.main.fragment_payment.tv_membership_title
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentFragment : Fragment() {
    private var OnSubscribeCallback : OnSubscribeClick?= null
    lateinit var paymentsCardAdapter: PaymentsCardAdapter
    private var membership : Membership?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            membership = it.getSerializable("membership") as Membership?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        tv_membership_title.text = membership!!.name
        tv_membership_plan_price.text = "AED "+membership!!.price.toString()
        tv_membership_desc.text = membership!!.description

        tv_subscribe!!.setOnClickListener { // dismiss dialog
           purchaseMembership()
        }

        tv_add_new_card.setOnClickListener {
            findNavController().navigate(R.id.action_myPaymentFragment_to_addNewCardFragment)
        }

        rv_cards_listing.layoutManager =  LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        paymentsCardAdapter = PaymentsCardAdapter(requireContext(), object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
            }

        })
        rv_cards_listing.adapter = paymentsCardAdapter
        paymentsCardAdapter.notifyDataSetChanged()
    }

    private fun purchaseMembership() {
        if (Utility.isNetworkAvailable()){
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            frag_payment_progressBar.visibility= View.VISIBLE
            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
            val call = apiInterface.buyMembership(user_id = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString(),
                membership_id = membership!!.membership_id.toString())
            call!!.enqueue(object : Callback<BuyMembership?> {
                override fun onResponse(
                    call: Call<BuyMembership?>,
                    response: Response<BuyMembership?>
                ) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipId,response.body()!!.membership.membership_id)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipTimeLimit,response.body()!!.membership.day)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipName,response.body()!!.membership.name)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipPrice,response.body()!!.membership.price)
                            SharedPreferenceUtility.getInstance().saveMembershipInfo(requireContext(),response.body()!!.membership)
                            findNavController().navigate(R.id.homeFragment)
                        }else{
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    }else{
                        LogUtils.longToast(requireContext(),getString(R.string.response_isnt_successful))
                    }
                }

                override fun onFailure(call: Call<BuyMembership?>, throwable: Throwable) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    LogUtils.e("msg", throwable.message)
                    LogUtils.shortToast(requireContext(),throwable.localizedMessage)
                }

            })
        }
    }


    fun setSubscribeClickListenerCallback(OnSubscribeCallback: OnSubscribeClick){
        this.OnSubscribeCallback = OnSubscribeCallback
    }

    interface OnSubscribeClick{
        fun OnSubscribe()
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
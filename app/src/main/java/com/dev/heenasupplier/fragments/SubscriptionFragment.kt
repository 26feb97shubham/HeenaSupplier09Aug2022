package com.dev.heenasupplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.models.BuySubscriptionResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_bookings.view.*
import kotlinx.android.synthetic.main.fragment_my_bookings.view.fragment_bookings_progressBar
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
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Subscription Fragment")
        }

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }
        mView!!.tv_buy_subscription.setOnClickListener {
            buy_subscription()
        }
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
//                        findNavController().navigate(R.id.homeFragment)
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
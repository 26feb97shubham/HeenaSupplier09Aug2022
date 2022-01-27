package com.heena.supplier.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.PaymentsCardAdapter
import com.heena.supplier.models.BuyMembership
import com.heena.supplier.models.Cards
import com.heena.supplier.models.Membership
import com.heena.supplier.models.ViewCardResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import com.heena.supplier.utils.Utility.Companion.mSelectedItem
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.tv_subscribe
import kotlinx.android.synthetic.main.fragment_payment.*
import kotlinx.android.synthetic.main.fragment_payment.rv_cards_listing
import kotlinx.android.synthetic.main.fragment_payment.tv_add_new_card
import kotlinx.android.synthetic.main.fragment_payment.tv_membership_desc
import kotlinx.android.synthetic.main.fragment_payment.tv_membership_plan_price
import kotlinx.android.synthetic.main.fragment_payment.tv_membership_title
import kotlinx.android.synthetic.main.fragment_payment.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.ArrayList

class PaymentFragment : Fragment() {
    var mView : View?= null
    private var membership : Membership?=null
    private var card_id = ""
    lateinit var paymentCardsAdapter: PaymentsCardAdapter
    var cardsList = ArrayList<Cards>()
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
        mView = inflater.inflate(R.layout.fragment_payment, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
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

        mView!!.tv_membership_title.text = membership!!.name
        mView!!.tv_membership_plan_price.text = "AED "+membership!!.price.toString()
        mView!!.tv_membership_desc.text = membership!!.description

        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface {
                override fun retry() {
                    noInternetDialog.dismiss()
                    showCardsListing()
                }
            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Home Fragment")
        }else{
            showCardsListing()
        }

        mView!!.tv_subscribe!!.setSafeOnClickListener { // dismiss dialog
           purchaseMembership()
        }

        mView!!.tv_add_new_card.setSafeOnClickListener {
            findNavController().navigate(R.id.action_myPaymentFragment_to_addNewCardFragment)
        }

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

    private fun showCardsListing() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.frag_payment_progressBar.visibility= View.VISIBLE
        val call = apiInterface.showCards(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0))
        call!!.enqueue(object : Callback<ViewCardResponse?> {
            override fun onResponse(
                call: Call<ViewCardResponse?>,
                response: Response<ViewCardResponse?>
            ) {
                mView!!.frag_payment_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            mView!!.tv_no_cards_found_service_payment.visibility = View.GONE
                            mView!!.card_creditcard.visibility = View.VISIBLE
                            cardsList.clear()
                            cardsList = (response.body()!!.cards as ArrayList<Cards>?)!!
                            mView!!.rv_cards_listing.layoutManager= LinearLayoutManager(requireContext())
                            paymentCardsAdapter = PaymentsCardAdapter(requireContext(), cardsList!!,object : ClickInterface.OnRecyclerItemClick{
                                override fun OnClickAction(pos: Int) {
//                                    mSelectedItem = pos
                                    card_id = cardsList!![mSelectedItem].id.toString()
                                    paymentCardsAdapter.notifyDataSetChanged()
                                }

                            })
                            mView!!.rv_cards_listing.adapter=paymentCardsAdapter
                        }else{
                            mView!!.tv_no_cards_found_service_payment.visibility = View.VISIBLE
                            mView!!.card_creditcard.visibility = View.GONE
                        }
                    }else{
                        LogUtils.longToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ViewCardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.tv_no_cards_found_service_payment.visibility = View.VISIBLE
                mView!!.card_creditcard.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
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
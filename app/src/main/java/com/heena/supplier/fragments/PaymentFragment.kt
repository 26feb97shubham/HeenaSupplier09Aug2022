package com.heena.supplier.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.activities.PaymentDetailsActivity
import com.heena.supplier.activities.TapPaymentActivity
import com.heena.supplier.adapters.PaymentsCardAdapter
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.rest.APIUtils
import com.heena.supplier.rest.APIUtils.ServicePayment
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.convertDoubleValueWithCommaSeparator
import com.heena.supplier.utils.Utility.mSelectedItem
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.activity_membership_registration2.*
import kotlinx.android.synthetic.main.fragment_payment.*
import kotlinx.android.synthetic.main.fragment_payment.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.NumberFormat
import java.util.*

class PaymentFragment : Fragment() {
    var mView : View?= null
    private var membership : Membership?=null
    private var subscription : SubscriptionPlan?=null
    private var card_id = ""
    lateinit var paymentCardsAdapter: PaymentsCardAdapter
    var cardsList = ArrayList<Cards>()
    var url = ""
    var transactionURL = ""
    var redirectionURL = ""
    var tapID = ""
    var direction = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            membership = it.getSerializable("membership") as Membership?
            subscription = it.getSerializable("subscription") as SubscriptionPlan?
            direction = it.getInt("direction")
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
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        if (membership!=null){
            mView!!.tv_membership_title.text = membership!!.name
            mView!!.tv_membership_plan_price.text = "AED "+ convertDoubleValueWithCommaSeparator(membership!!.price!!.toDouble())
            mView!!.tv_membership_desc.text = membership!!.description
        }else if (subscription!=null){
            mView!!.tv_membership_title.text = subscription!!.name
            mView!!.tv_membership_plan_price.text = "AED "+ convertDoubleValueWithCommaSeparator(subscription!!.amount!!.toDouble())
            mView!!.tv_membership_desc.text = subscription!!.description
        }else{
            LogUtils.shortToast(requireContext(), "Invalid Data Found")
            findNavController().popBackStack()
        }

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

        mView!!.tv_subscribe!!.setSafeOnClickListener {
          /*  if (direction==1){
                purchaseMembership()
            }else if (direction==2){
                purchaseSubscriptions()
            }*/
            if (TextUtils.isEmpty(card_id)){
                Utility.showSnackBarValidationError(mView!!.paymentFragmentConstraintLayout,
                    requireContext().getString(R.string.please_select_a_card_to_continue),
                    requireContext())

            }else{
                if (direction==1){
                    purchaseMembership()
                }else if (direction==2){
                    purchaseSubscriptions()
                }

            }
        }

        mView!!.tv_add_new_card.setSafeOnClickListener {
//            findNavController().navigate(R.id.action_myPaymentFragment_to_addNewCardFragment)
            val bundle = Bundle()
            bundle.putString("title", "addCards")
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

    }

/*    private fun purchaseSubscriptions() {
        ServicePayment = true
        if (Utility.isNetworkAvailable()){
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            frag_payment_progressBar.visibility= View.VISIBLE
            val apiInterface = APIClient.getPaymentClient()!!.create(APIInterface::class.java)
            val builder = APIClient.createBuilder(arrayOf("user_id","membership_id", "membership_price", "type", "subscription_id", "subscription_price"),
                arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    "", "", "2", subscription!!.subscription_plans_id.toString(), subscription!!.amount.toString()))
            val call = apiInterface.paymentToken(builder.build())
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    try {
                        if(APIUtils.resultExplanationPaymentStatus){
                            if (response.isSuccessful){
                                if (APIUtils.ServicePaymentTOKEN.isEmpty()){
                                    Utility.showSnackBarOnResponseError(paymentFragmentConstraintLayout,
                                        getString(R.string.check_internet),
                                        requireContext())
                                }else{
                                    val bundle = Bundle()
                                    bundle.putString("TransToken", APIUtils.ServicePaymentTOKEN)
                                    bundle.putString("user_id",sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString())
                                    bundle.putString("type","2")
                                    bundle.putString("membership_id","")
                                    bundle.putString("membership_price", "")
                                    bundle.putString("subscription_id",subscription!!.subscription_plans_id.toString())
                                    bundle.putString("subscription_price",subscription!!.amount.toString())
                                    bundle.putString("days",subscription!!.days.toString())
                                    bundle.putString("starting_at",subscription!!.created_at)
                                    bundle.putString("ending_at",subscription!!.updated_at)
                                    bundle.putBoolean("isRegister", false)
                                    bundle.putBoolean("isPurchaseMembership", false)
                                    bundle.putBoolean("isPurchaseSubscriptions", true)
                                    startActivity(Intent(requireContext(), PaymentDetailsActivity::class.java).putExtras(bundle))
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                    getString(R.string.response_isnt_successful),
                                    requireContext())
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                APIUtils.resultExplanationPayment,
                                requireContext())
                        }
                    }catch (e : java.lang.Exception){
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                        throwable.message.toString(),
                        requireContext())
                }

            })
        }
    }

    private fun purchaseMembership() {
        ServicePayment = true
        if (Utility.isNetworkAvailable()){
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            frag_payment_progressBar.visibility= View.VISIBLE
            val apiInterface = APIClient.getPaymentClient()!!.create(APIInterface::class.java)
            val builder = APIClient.createBuilder(arrayOf("user_id","membership_id", "membership_price", "type", "subscription_id", "subscription_price"),
                arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    membership!!.membership_id.toString(), membership!!.price.toString(), "1", "", ""))
           *//* val builder = APIClient.createBuilder(arrayOf("user_id","membership_id", "card_id"),
                arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    membership!!.membership_id.toString(), card_id))*//*
            val call = apiInterface.paymentToken(builder.build())
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    *//*if (response.isSuccessful){
                        if(response.body()!=null){
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getInt("status")==1){
                                val data = jsonObject.getJSONObject("data")
                                if(data!=null){
                                    val source = Gson().fromJson(data.getJSONObject("source").toString(), SourceModel::class.java)
                                    val type = if (source.type==null){
                                        ""
                                    }else{
                                        source.type
                                    }
                                    if (type.equals("CARD_NOT_PRESENT")){
                                        Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                            requireContext().getString(R.string.card_is_not_valid),
                                            requireContext())
                                    }else{
                                        url = data.getJSONObject("transaction").getString("url")
                                        tapID = data.getString("id")
                                        redirectionURL = data.getJSONObject("redirect").getString("url")+"/"+tapID
                                        val bundle = Bundle()
                                        bundle.putString("url", url)
                                        bundle.putString("redirect_url", redirectionURL)
                                        bundle.putString("tap_id", tapID)
                                        bundle.putBoolean("isRegister", false)
                                        bundle.putBoolean("isPurchaseMembership", true)
                                        bundle.putBoolean("isPurchaseSubscriptions", false)
                                        requireActivity().startActivity(Intent(requireContext(), TapPaymentActivity::class.java).putExtras(bundle))
                                    }
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                    requireContext().getString(R.string.payment_failed),
                                    requireContext())
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                requireContext().getString(R.string.payment_failed),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())

                    }*//*


                    try {
                        if(APIUtils.resultExplanationPaymentStatus){
                            if (response.isSuccessful){
                                if (APIUtils.ServicePaymentTOKEN.isEmpty()){
                                    Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                                        getString(R.string.check_internet),
                                        requireContext())
                                }else{
                                    val bundle = Bundle()
                                    bundle.putString("TransToken", APIUtils.ServicePaymentTOKEN)
                                    bundle.putString("user_id",sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString())
                                    bundle.putString("type","1")
                                    bundle.putString("membership_id",membership!!.membership_id.toString())
                                    bundle.putString("membership_price", membership!!.price.toString())
                                    bundle.putString("subscription_id","")
                                    bundle.putString("subscription_price","")
                                    bundle.putString("days","")
                                    bundle.putString("starting_at","")
                                    bundle.putString("ending_at","")
                                    bundle.putBoolean("isRegister", false)
                                    bundle.putBoolean("isPurchaseMembership", true)
                                    bundle.putBoolean("isPurchaseSubscriptions", false)
                                    startActivity(Intent(requireContext(), PaymentDetailsActivity::class.java).putExtras(bundle))
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                    getString(R.string.response_isnt_successful),
                                    requireContext())
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                APIUtils.resultExplanationPayment,
                                requireContext())
                        }
                    }catch (e : java.lang.Exception){
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                        throwable.message.toString(),
                        requireContext())
                }

            })
        }
    }*/



    private fun purchaseSubscriptions() {
        if (Utility.isNetworkAvailable()){
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            frag_payment_progressBar.visibility= View.VISIBLE
            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
            val builder = APIClient.createBuilder(arrayOf("user_id","subscription_id", "card_id"),
                arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    subscription!!.subscription_plans_id.toString(), card_id))
            val call = apiInterface.createChargeSubscriptions(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""], builder.build())
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    if (response.isSuccessful){
                        if(response.body()!=null){
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getInt("status")==1){
                                val data = jsonObject.getJSONObject("data")
                                if(data!=null){
                                    val source = Gson().fromJson(data.getJSONObject("source").toString(), SourceModel::class.java)
                                    val type = if (source.type==null){
                                        ""
                                    }else{
                                        source.type
                                    }
                                    if (type.equals("CARD_NOT_PRESENT")){
                                        Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                            requireContext().getString(R.string.card_is_not_valid),
                                            requireContext())
                                    }else{
                                        url = data.getJSONObject("transaction").getString("url")
                                        tapID = data.getString("id")
                                        redirectionURL = data.getJSONObject("redirect").getString("url")+"/"+tapID
                                        val bundle = Bundle()
                                        bundle.putString("url", url)
                                        bundle.putString("redirect_url", redirectionURL)
                                        bundle.putString("tap_id", tapID)
                                        bundle.putBoolean("isRegister", false)
                                        bundle.putBoolean("isPurchaseMembership", false)
                                        bundle.putBoolean("isPurchaseSubscriptions", true)
                                        requireActivity().startActivity(Intent(requireContext(), TapPaymentActivity::class.java).putExtras(bundle))
                                    }
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                    requireContext().getString(R.string.payment_failed),
                                    requireContext())
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                requireContext().getString(R.string.payment_failed),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                        throwable.message.toString(),
                        requireContext())
                }

            })
        }
    }

    private fun purchaseMembership() {
        if (Utility.isNetworkAvailable()){
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            frag_payment_progressBar.visibility= View.VISIBLE
            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
            val builder = APIClient.createBuilder(arrayOf("user_id","membership_id", "card_id"),
                arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    membership!!.membership_id.toString(), card_id))
            val call = apiInterface.createCharge(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""], builder.build())
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    if (response.isSuccessful){
                        if(response.body()!=null){
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getInt("status")==1){
                                val data = jsonObject.getJSONObject("data")
                                if(data!=null){
                                    val source = Gson().fromJson(data.getJSONObject("source").toString(), SourceModel::class.java)
                                    val type = if (source.type==null){
                                        ""
                                    }else{
                                        source.type
                                    }
                                    if (type.equals("CARD_NOT_PRESENT")){
                                        Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                            requireContext().getString(R.string.card_is_not_valid),
                                            requireContext())
                                    }else{
                                        url = data.getJSONObject("transaction").getString("url")
                                        tapID = data.getString("id")
                                        redirectionURL = data.getJSONObject("redirect").getString("url")+"/"+tapID
                                        val bundle = Bundle()
                                        bundle.putString("url", url)
                                        bundle.putString("redirect_url", redirectionURL)
                                        bundle.putString("tap_id", tapID)
                                        bundle.putBoolean("isRegister", false)
                                        bundle.putBoolean("isPurchaseMembership", true)
                                        bundle.putBoolean("isPurchaseSubscriptions", false)
                                        requireActivity().startActivity(Intent(requireContext(), TapPaymentActivity::class.java).putExtras(bundle))
                                    }
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                    requireContext().getString(R.string.payment_failed),
                                    requireContext())
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                                requireContext().getString(R.string.payment_failed),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())

                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                    frag_payment_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                        throwable.message.toString(),
                        requireContext())
                }

            })
        }
    }

    private fun showCardsListing() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.frag_payment_progressBar.visibility= View.VISIBLE
        val call = apiInterface.showCards(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<ViewCardResponse?> {
            override fun onResponse(
                call: Call<ViewCardResponse?>,
                response: Response<ViewCardResponse?>
            ) {
                try {
                    if (response.isSuccessful){
                        mView!!.frag_payment_progressBar.visibility= View.GONE
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        if (response.body()!!.status==1){
                            mView!!.tv_no_cards_found_service_payment.visibility = View.GONE
                            mView!!.card_creditcard.visibility = View.VISIBLE
                            cardsList.clear()
                            cardsList = (response.body()!!.cards as ArrayList<Cards>?)!!
                            mView!!.rv_cards_listing.layoutManager= LinearLayoutManager(requireContext())
                            paymentCardsAdapter = PaymentsCardAdapter(requireContext(), cardsList,object : ClickInterface.OnRecyclerItemClick{
                                override fun OnClickAction(pos: Int) {
                                    card_id = cardsList[pos].id.toString()
                                    paymentCardsAdapter.notifyDataSetChanged()
                                }

                            })
                            mView!!.rv_cards_listing.adapter=paymentCardsAdapter
                        }else{
                            mView!!.tv_no_cards_found_service_payment.visibility = View.VISIBLE
                            mView!!.card_creditcard.visibility = View.GONE
                        }
                    }else{
                        mView!!.frag_payment_progressBar.visibility= View.GONE
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
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
                mView!!.tv_no_cards_found_service_payment.visibility = View.VISIBLE
                mView!!.frag_payment_progressBar.visibility = View.GONE
                Utility.showSnackBarOnResponseError(mView!!.paymentFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
                mView!!.card_creditcard.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }
}
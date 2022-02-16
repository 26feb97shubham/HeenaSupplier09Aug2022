package com.heena.supplier.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.PaymentsCardAdapter
import com.heena.supplier.models.Cards
import com.heena.supplier.models.Membership
import com.heena.supplier.models.SourceModel
import com.heena.supplier.models.ViewCardResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.ConstClass
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.convertDoubleValueWithCommaSeparator
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_payment_fragment.*
import kotlinx.android.synthetic.main.fragment_payment.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.ArrayList

class PaymentFragmentActivity : AppCompatActivity() {
    private lateinit var paymentCardsAdapter: PaymentsCardAdapter
    var emailaddress : String?= null
    private var membershipId : Int = 0
    private var myuserId : Int = 0
    var membership : Membership?=null
    private var card_id = ""
    var cardsList = ArrayList<Cards>()
    var url = ""
    var transactionURL = ""
    var redirectionURL = ""
    var tapID = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_fragment)
        Utility.changeLanguage(
            this,
            SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]
        )
        if (intent.extras!==null){
            emailaddress = intent.getStringExtra(ConstClass.EMAILADDRESS).toString()
            membership = intent.getSerializableExtra("membership") as Membership?
        }

        myuserId = SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]
        membershipId = membership!!.membership_id
        Log.e("userId", myuserId.toString())
        Log.e("membershipId", membershipId.toString())

        tv_membership_title_activity.text = membership!!.name
        tv_membership_plan_price_activity.text = "AED ${convertDoubleValueWithCommaSeparator(membership!!.price.toDouble())}"
        tv_membership_desc_activity.text = membership!!.description
        
        
        if (Utility.hasConnection(this)){
            showCardsListing()
        }else{
            LogUtils.shortToast(this, getString(R.string.no_internet))
        }

        tv_subscribe_activity!!.setSafeOnClickListener { // dismiss dialog
            if (TextUtils.isEmpty(card_id)){
                LogUtils.shortToast(this,getString(R.string.please_select_a_card_to_continue))
            }else{
                purchaseMembership()
            }
        }

        tv_add_new_card_activity.setSafeOnClickListener {
            startActivity(Intent(this, AddNewCardActivity::class.java))
        }
    }

    private fun showCardsListing() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        membership_payment_progressBar.visibility= View.VISIBLE
        val call = apiInterface.showCards(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0], SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<ViewCardResponse?> {
            override fun onResponse(
                call: Call<ViewCardResponse?>,
                response: Response<ViewCardResponse?>
            ) {
                membership_payment_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            tv_no_cards_found_service_payment_activity.visibility = View.GONE
                            card_creditcard_activity.visibility = View.VISIBLE
                            cardsList.clear()
                            cardsList = (response.body()!!.cards as ArrayList<Cards>?)!!
                            rv_cards_listing_activity.layoutManager= LinearLayoutManager(this@PaymentFragmentActivity)
                            paymentCardsAdapter = PaymentsCardAdapter(this@PaymentFragmentActivity, cardsList,object : ClickInterface.OnRecyclerItemClick{
                                override fun OnClickAction(position: Int) {
//                                    mSelectedItem = pos
                                    card_id = cardsList[Utility.mSelectedItem].id.toString()
                                    paymentCardsAdapter.notifyDataSetChanged()
                                }

                            })
                            rv_cards_listing_activity.adapter=paymentCardsAdapter
                        }else{
                            tv_no_cards_found_service_payment_activity.visibility = View.VISIBLE
                            card_creditcard_activity.visibility = View.GONE
                        }
                    }else{
                        LogUtils.longToast(this@PaymentFragmentActivity, getString(R.string.response_isnt_successful))
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
                LogUtils.shortToast(this@PaymentFragmentActivity, getString(R.string.check_internet))
                tv_no_cards_found_service_payment_activity.visibility = View.VISIBLE
                card_creditcard_activity.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun purchaseMembership() {
        if (Utility.isNetworkAvailable()){
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            membership_payment_progressBar.visibility= View.VISIBLE
            val builder = APIClient.createBuilder(arrayOf("user_id","membership_id", "card_id"),
                arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(),
                    membershipId.toString(), card_id))
            val call = apiInterface.createCharge(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""], builder.build())
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
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
                                        LogUtils.shortToast(this@PaymentFragmentActivity, getString(R.string.card_is_not_valid))
                                    }else{
                                        url = data.getJSONObject("transaction").getString("url")
                                        tapID = data.getString("id")
                                        redirectionURL = data.getJSONObject("redirect").getString("url")+"/"+tapID
                                        val bundle = Bundle()
                                        bundle.putString("url", url)
                                        bundle.putString("redirect_url", redirectionURL)
                                        bundle.putString("tap_id", tapID)
                                        bundle.putBoolean("isRegister", true)
                                        bundle.putBoolean("isPurchaseMembership", false)
                                        bundle.putBoolean("isPurchaseSubscriptions", false)
                                        startActivity(Intent(this@PaymentFragmentActivity, TapPaymentActivity::class.java).putExtras(bundle))
                                    }
                                }
                            }else{
                                Log.e("error", "Payment Failed")
                            }
                        }else{
                            Log.e("error", "Payment Failed")
                        }
                    }else{
                        LogUtils.longToast(this@PaymentFragmentActivity,getString(R.string.response_isnt_successful))
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    LogUtils.shortToast(this@PaymentFragmentActivity,throwable.localizedMessage)
                    membership_payment_progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }

            })
        }
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
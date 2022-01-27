package com.heena.supplier.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.PaymentsCardAdapter
import com.heena.supplier.models.BuyMembership
import com.heena.supplier.models.Cards
import com.heena.supplier.models.Membership
import com.heena.supplier.models.ViewCardResponse
import com.heena.supplier.utils.ConstClass
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_membership_registration2.*
import kotlinx.android.synthetic.main.activity_payment_fragment.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.fragment_payment.*
import kotlinx.android.synthetic.main.fragment_payment.view.*
import org.json.JSONException
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_fragment)
        Utility.changeLanguage(
            this,
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
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
        tv_membership_plan_price_activity.text = "AED "+ membership!!.price
        tv_membership_desc_activity.text = membership!!.description
        
        
        if (Utility.hasConnection(this)){
            showCardsListing()
        }else{
            LogUtils.shortToast(this, getString(R.string.no_internet))
        }

        tv_subscribe_activity!!.setSafeOnClickListener { // dismiss dialog
            purchaseMembership()
        }

        tv_add_new_card_activity.setSafeOnClickListener {
            startActivity(Intent(this, AddNewCardActivity::class.java))
            finish()
        }
    }

    private fun showCardsListing() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        membership_payment_progressBar.visibility= View.VISIBLE
        val call = apiInterface.showCards(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0))
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
                            tv_no_cards_found_service_payment.visibility = View.GONE
                            card_creditcard.visibility = View.VISIBLE
                            cardsList.clear()
                            cardsList = (response.body()!!.cards as ArrayList<Cards>?)!!
                            rv_cards_listing.layoutManager= LinearLayoutManager(this@PaymentFragmentActivity)
                            paymentCardsAdapter = PaymentsCardAdapter(this@PaymentFragmentActivity, cardsList,object : ClickInterface.OnRecyclerItemClick{
                                override fun OnClickAction(pos: Int) {
//                                    mSelectedItem = pos
                                    card_id = cardsList[Utility.mSelectedItem].id.toString()
                                    paymentCardsAdapter.notifyDataSetChanged()
                                }

                            })
                            rv_cards_listing.adapter=paymentCardsAdapter
                        }else{
                            tv_no_cards_found_service_payment.visibility = View.VISIBLE
                            card_creditcard.visibility = View.GONE
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
                tv_no_cards_found_service_payment.visibility = View.VISIBLE
                card_creditcard.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun purchaseMembership() {
        if (Utility.isNetworkAvailable()){
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            membership_payment_progressBar.visibility= View.VISIBLE
            val call = apiInterface.buyMembership(user_id = myuserId.toString(), membership_id = membershipId.toString())
            call!!.enqueue(object : Callback<BuyMembership?> {
                override fun onResponse(
                    call: Call<BuyMembership?>,
                    response: Response<BuyMembership?>
                ) {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            LogUtils.shortToast(this@PaymentFragmentActivity, response.body()!!.message)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsLogin, true)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipId,response.body()!!.membership.membership_id)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipTimeLimit,response.body()!!.membership.day)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipName,response.body()!!.membership.name)
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.MembershipPrice,response.body()!!.membership.price)
                            SharedPreferenceUtility.getInstance().saveMembershipInfo(this@PaymentFragmentActivity,response.body()!!.membership)
                            startActivity(Intent(this@PaymentFragmentActivity, LoginActivity::class.java))
                            finishAffinity()
                        }else{
                            LogUtils.longToast(this@PaymentFragmentActivity, response.body()!!.message)
                        }
                    }else{
                        LogUtils.longToast(this@PaymentFragmentActivity,getString(R.string.response_isnt_successful))
                    }
                }

                override fun onFailure(call: Call<BuyMembership?>, throwable: Throwable) {
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
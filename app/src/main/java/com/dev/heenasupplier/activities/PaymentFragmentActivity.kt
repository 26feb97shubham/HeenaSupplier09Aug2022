package com.dev.heenasupplier.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.PaymentsCardAdapter
import com.dev.heenasupplier.models.BuyMembership
import com.dev.heenasupplier.models.Membership
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.ConstClass
import com.dev.heenasupplier.utils.ConstClass.MEMBERSHIPID
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.activity_membership_registration2.*
import kotlinx.android.synthetic.main.activity_payment_fragment.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.tv_subscribe
import kotlinx.android.synthetic.main.fragment_payment.*
import kotlinx.android.synthetic.main.fragment_payment.rv_cards_listing
import kotlinx.android.synthetic.main.fragment_payment.tv_add_new_card
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentFragmentActivity : AppCompatActivity() {
    lateinit var paymentsCardAdapter: PaymentsCardAdapter
    var emailaddress : String?= null
    var membershipId : Int = 0
    var myuserId : Int = 0
    var membership : Membership?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_fragment)
        if (intent.extras!==null){
            emailaddress = intent.getStringExtra(ConstClass.EMAILADDRESS).toString()
            membership = intent.getSerializableExtra("membership") as Membership?
        }

        myuserId = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0)
        membershipId = membership!!.membership_id
        Log.e("userId", myuserId.toString())
        Log.e("membershipId", membershipId.toString())

        tv_membership_title_activity.text = membership!!.name
        tv_membership_plan_price_activity.text = "AED "+ membership!!.price
        tv_membership_desc_activity.text = membership!!.description

        tv_subscribe_activity!!.setOnClickListener { // dismiss dialog
           /* startActivity(Intent(this, OtpVerificationActivity::class.java).putExtra("ref", "1").putExtra(ConstClass.EMAILADDRESS, emailaddress))
            finishAffinity()*/
            purchaseMembership()
        }

        tv_add_new_card_activity.setOnClickListener {
            startActivity(Intent(this, AddNewCardActivity::class.java))
            finish()
        }

        rv_cards_listing_activity.layoutManager =  LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        paymentsCardAdapter = PaymentsCardAdapter(this, object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
            }
        })

        rv_cards_listing_activity.adapter = paymentsCardAdapter
        paymentsCardAdapter.notifyDataSetChanged()
    }

    private fun purchaseMembership() {
        if (Utility.isNetworkAvailable()){
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            membership_payment_progressBar.visibility= View.VISIBLE
            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
            val call = apiInterface.buyMembership(user_id = myuserId.toString(), membership_id = membershipId.toString())
            call!!.enqueue(object : Callback<BuyMembership?> {
                override fun onResponse(
                    call: Call<BuyMembership?>,
                    response: Response<BuyMembership?>
                ) {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            LogUtils.shortToast(this@PaymentFragmentActivity, response.body()!!.message)
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
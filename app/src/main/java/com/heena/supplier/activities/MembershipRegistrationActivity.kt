package com.heena.supplier.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.RegistrationMembershipPlansListAdapter
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.BuyMembership
import com.heena.supplier.models.Membership
import com.heena.supplier.models.MembershipListResponse
import com.heena.supplier.models.SourceModel
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.rest.APIUtils
import com.heena.supplier.rest.APIUtils.ServicePayment
import com.heena.supplier.rest.APIUtils.ServicePaymentTOKEN
import com.heena.supplier.rest.APIUtils.resultExplanationPayment
import com.heena.supplier.rest.APIUtils.resultExplanationPaymentStatus
import com.heena.supplier.utils.ConstClass
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.isNetworkAvailable
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_membership_registration2.*
import kotlinx.android.synthetic.main.activity_membership_registration2.btnSignUp
import kotlinx.android.synthetic.main.activity_membership_registration2.rv_membership_plans
import kotlinx.android.synthetic.main.activity_payment_fragment.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MembershipRegistrationActivity : AppCompatActivity() {
    var emailaddress : String?= null
    var membershipId : Int = 0
    var membershipPrice : Float = 0.0F
    private lateinit var registrationMembershipPlansListAdapter: RegistrationMembershipPlansListAdapter
    private var membershipList = ArrayList<Membership>()
    private var membership : Membership?=null
    private var mContext : Context?=null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = this
        setContentView(R.layout.activity_membership_registration2)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )

        if (intent.extras !== null) {
            emailaddress = intent.getStringExtra(ConstClass.EMAILADDRESS).toString()
        }

        getMembershipList()

        rv_membership_plans.apply {
            this.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            registrationMembershipPlansListAdapter = RegistrationMembershipPlansListAdapter(
                    mContext as MembershipRegistrationActivity,
                    membershipList,
                    object : ClickInterface.OnRecyclerItemClick {
                        override fun OnClickAction(position: Int) {
                        }
                    })
            this.adapter =registrationMembershipPlansListAdapter
        }

        registrationMembershipPlansListAdapter.notifyDataSetChanged()

        btnSignUp.setSafeOnClickListener {
            if (registrationMembershipPlansListAdapter.getSelected() != null) {
                membershipId = registrationMembershipPlansListAdapter.getSelected()!!.membership_id
                membership = registrationMembershipPlansListAdapter.getSelected()
                if (sharedPreferenceInstance!![SharedPreferenceUtility.MembershipId, 0] == membershipId
                ) {
                    Utility.showSnackBarValidationError(membershipRegistrationActivityConstraintLayout,
                        getString(R.string.plan_already_purchased),
                        this)

                } else {
                    startActivity(
                        Intent(this, PaymentFragmentActivity::class.java).putExtra(
                            ConstClass.EMAILADDRESS,
                            emailaddress
                        ).putExtra("membership", membership)
                    )
                    finish()

//                    purchaseMembership()
                }
            } else {
                Utility.showSnackBarValidationError(membershipRegistrationActivityConstraintLayout,
                    getString(R.string.please_select_the_membership_plan_first_to_continue),
                    this)

            }
        }
    }

    private fun getMembershipList() {
        if(isNetworkAvailable()){
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            membership_registration_progressBar.visibility= View.VISIBLE
            val call = apiInterface.membershipList(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0], sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
            call!!.enqueue(object : Callback<MembershipListResponse?>{
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<MembershipListResponse?>, response: Response<MembershipListResponse?>) {
                    membership_registration_progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if (response.isSuccessful){
                            membership_card.visibility = View.VISIBLE
                            btnSignUp.visibility = View.VISIBLE
                            if (response.body()!=null){
                                if(response.body()!!.status==1){
                                    membershipList = response.body()!!.membership as ArrayList<Membership>
                                    rv_membership_plans.layoutManager = LinearLayoutManager(this@MembershipRegistrationActivity, LinearLayoutManager.HORIZONTAL, false)
                                    registrationMembershipPlansListAdapter = RegistrationMembershipPlansListAdapter(this@MembershipRegistrationActivity,membershipList, object : ClickInterface.OnRecyclerItemClick{
                                        override fun OnClickAction(position: Int) {
                                            membershipId  = membershipList[position].membership_id
                                            membershipPrice  = membershipList[position].price.toFloat()

                                        }
                                    })
                                    rv_membership_plans.adapter = registrationMembershipPlansListAdapter
                                    pageIndicator2_activity.attachTo(rv_membership_plans)
                                    registrationMembershipPlansListAdapter.notifyDataSetChanged()
                                }else{
                                    Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                                        response.body()!!.message.toString(),
                                        this@MembershipRegistrationActivity)
                                }

                            }else{
                                Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                                    response.message(),
                                    this@MembershipRegistrationActivity)

                            }
                        }else{
                            membership_card.visibility = View.GONE
                            btnSignUp.visibility = View.GONE
                            Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                                getString(R.string.response_isnt_successful),
                                this@MembershipRegistrationActivity)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<MembershipListResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                        getString(R.string.check_internet),
                        this@MembershipRegistrationActivity)
                    membership_card.visibility = View.GONE
                    btnSignUp.visibility = View.GONE
                    membership_registration_progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }

            })
        }
    }


    private fun purchaseMembership() {
        ServicePayment = true
        if (Utility.isNetworkAvailable()){
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            membership_registration_progressBar.visibility= View.VISIBLE
            val builder = APIClient.createBuilder(arrayOf("user_id","membership_id", "membership_price", "type", "subscription_id", "subscription_price"),
                arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    membershipId.toString(), membershipPrice.toString(), "1", "", ""))
            val apiInterface = APIClient.getPaymentClient()!!.create(APIInterface::class.java)
            val call = apiInterface.paymentToken(builder.build())

            call.enqueue(object : Callback<ResponseBody?>{
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    membership_registration_progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if(resultExplanationPaymentStatus){
                            if (response.isSuccessful){
                                if (ServicePaymentTOKEN.isEmpty()){
                                    Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                                        getString(R.string.check_internet),
                                        this@MembershipRegistrationActivity)
                                }else{
                                    val bundle = Bundle()
                                    bundle.putString("TransToken",ServicePaymentTOKEN)
                                    bundle.putString("user_id",sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString())
                                    bundle.putString("type","1")
                                    bundle.putString("membership_id",membershipId.toString())
                                    bundle.putString("membership_price",membershipPrice.toString())
                                    bundle.putString("days","")
                                    bundle.putString("starting_at","")
                                    bundle.putString("ending_at","")
                                    bundle.putString("subscription_id","")
                                    bundle.putString("subscription_price","")
                                    bundle.putBoolean("isRegister", true)
                                    bundle.putBoolean("isPurchaseMembership", false)
                                    bundle.putBoolean("isPurchaseSubscriptions", false)
                                    startActivity(Intent(this@MembershipRegistrationActivity, PaymentDetailsActivity::class.java).putExtras(bundle))
                                }
                            }else{
                                Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                                    getString(R.string.response_isnt_successful),
                                    this@MembershipRegistrationActivity)
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                                resultExplanationPayment,
                                this@MembershipRegistrationActivity)
                        }
                    }catch (e : java.lang.Exception){
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    membership_registration_progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    Utility.showSnackBarOnResponseError(membershipRegistrationActivityConstraintLayout,
                        getString(R.string.check_internet),
                        this@MembershipRegistrationActivity)
                }

            })
        }
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }
}
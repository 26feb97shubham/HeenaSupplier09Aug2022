package com.dev.heenasupplier.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.SkuDetails
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.RegistrationMembershipPlansListAdapter
import com.dev.heenasupplier.billing.BillingClientWrapper
import com.dev.heenasupplier.models.Membership
import com.dev.heenasupplier.models.MembershipListResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.ConstClass
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import com.dev.heenasupplier.utils.Utility.Companion.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_membership_registration2.*
import kotlinx.android.synthetic.main.activity_membership_registration2.btnSignUp
import kotlinx.android.synthetic.main.activity_membership_registration2.rv_membership_plans
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class MembershipRegistrationActivity : AppCompatActivity() {
    var emailaddress : String?= null
    var membershipId : Int = 0
    private lateinit var registrationMembershipPlansListAdapter: RegistrationMembershipPlansListAdapter
    private var membershipList = ArrayList<Membership>()
    private var membership : Membership?=null
    private var mContext : Context?=null

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_membership_registration2)

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

        btnSignUp.setOnClickListener {
            if (registrationMembershipPlansListAdapter.getSelected() != null) {
                membershipId = registrationMembershipPlansListAdapter.getSelected()!!.membership_id
                membership = registrationMembershipPlansListAdapter.getSelected()
                if (SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.MembershipId, 0] == membershipId
                ) {
                    LogUtils.shortToast(this, getString(R.string.plan_already_purchased))
                } else {
                    startActivity(
                        Intent(this, PaymentFragmentActivity::class.java).putExtra(
                            ConstClass.EMAILADDRESS,
                            emailaddress
                        ).putExtra("membership", membership)
                    )
                    finish()
                }
            } else {
                LogUtils.shortToast(
                    this,
                    getString(R.string.please_select_the_membership_plan_first_to_continue)
                )
            }
        }
    }

    private fun getMembershipList() {
        if(isNetworkAvailable()){
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            membership_registration_progressBar.visibility= View.VISIBLE
            val call = apiInterface.membershipList(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0])
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
                                        }
                                    })
                                    rv_membership_plans.adapter = registrationMembershipPlansListAdapter
                                    registrationMembershipPlansListAdapter.notifyDataSetChanged()
                                }else{
                                    LogUtils.longToast(this@MembershipRegistrationActivity, response.body()!!.message)
                                }

                            }
                        }else{
                            membership_card.visibility = View.GONE
                            btnSignUp.visibility = View.GONE
                            LogUtils.longToast(this@MembershipRegistrationActivity,getString(R.string.response_isnt_successful))
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
                    LogUtils.shortToast(this@MembershipRegistrationActivity,throwable.localizedMessage)
                    membership_card.visibility = View.GONE
                    btnSignUp.visibility = View.GONE
                    membership_registration_progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }

            })
        }
    }

    private fun displayProducts() {
        billingClientWrapper.queryProducts(object : BillingClientWrapper.OnQueryProductsListener {
            override fun onSuccess(products: List<SkuDetails>) {
                /*products.forEach { product ->
                    purchaseButtonsMap[product.sku]?.apply {
                        text = "${product.description} for ${product.price}"
                        setOnClickListener {
                            billingClientWrapper.purchase(this@PaywallActivity, product) //will be declared below
                        }
                    }
                }*/
            }

            override fun onFailure(error: BillingClientWrapper.Error) {
                //handle error
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
package com.heena.supplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.heena.supplier.R
import com.heena.supplier.models.BuyMembership
import com.heena.supplier.models.BuySubscriptionResponse
import com.heena.supplier.models.Membership
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import kotlinx.android.synthetic.main.activity_tap_payment.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TapPaymentActivity : AppCompatActivity() {
    private var paymentURL : String?=null
    private var redirectURL : String?=null
    private var tap_id : String?=null
    private var status = 0
    private var isRegister : Boolean = false
    private var isPurchaseMembership : Boolean = false
    private var isPurchaseSubscriptions : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tap_payment)
        Utility.changeLanguage(
            this,
            SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]
        )

        if (intent.extras!=null){
            paymentURL = intent.extras!!.getString("url")
            redirectURL = intent.extras!!.getString("redirect_url")
            tap_id = intent.extras!!.getString("tap_id")
            isRegister = intent.extras!!.getBoolean("isRegister")
            isPurchaseMembership = intent.extras!!.getBoolean("isPurchaseMembership")
            isPurchaseSubscriptions = intent.extras!!.getBoolean("isPurchaseSubscriptions")
        }

        tapWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                super.onProgressChanged(view, progress)
                paymentgatewayProgressbar.visibility= View.VISIBLE
                if(progress>=80){
                    paymentgatewayProgressbar.visibility= View.GONE
                }

            }
        }
        tapWebView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                paymentgatewayProgressbar.visibility= View.VISIBLE
                return if(!redirectURL.equals("")){
                    if(redirectURL!!.contains("https://alniqasha.ae/api/manager/subsciptions")){
                        redirectOne(tap_id!!)
                    }else{
                        this@TapPaymentActivity.redirect(tap_id!!)
                    }

                    true
                }else{
                    true
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                paymentgatewayProgressbar.visibility= View.GONE
            }
        }
        tapWebView.settings.javaScriptEnabled=true
        tapWebView.settings.allowContentAccess=true
//        webView.settings.builtInZoomControls=true
        tapWebView.settings.loadWithOverviewMode=true
        tapWebView.settings.useWideViewPort=true
        tapWebView.settings.loadsImagesAutomatically=true
        tapWebView.loadUrl(paymentURL.toString())
    }

    private fun redirect(tapId : String){
        paymentgatewayProgressbar.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(arrayOf("lang","tap_id"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].toString(),
                tapId))
        val call = apiInterface.buyMembershipPlan(builder.build())
        call!!.enqueue(object : Callback<BuyMembership?>{
            override fun onResponse(
                call: Call<BuyMembership?>,
                response: Response<BuyMembership?>
            ) {
                paymentgatewayProgressbar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            if (isRegister && !isPurchaseMembership && !isPurchaseSubscriptions){
                              /*  membership = response.body()!!.membership
                                SharedPreferenceUtility.getInstance().saveMembershipInfo(this@TapPaymentActivity,response.body()!!.membership)*/
                                showMembershipConfirmationDialog(response.body()!!.message)
                                startActivity(Intent(this@TapPaymentActivity, LoginActivity::class.java))
                                finish()
                            }else if (!isRegister && isPurchaseMembership && !isPurchaseSubscriptions){
                                showMembershipConfirmationDialog(response.body()!!.message)
                                startActivity(Intent(this@TapPaymentActivity, HomeActivity::class.java))
                                finish()
                            }else{
                                showMembershipConfirmationDialog(response.body()!!.message)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ISFEATURED, true)
                                startActivity(Intent(this@TapPaymentActivity, HomeActivity::class.java))
                                finish()
                            }
                        }else{
                            showMembershipConfirmationDialog(response.body()!!.message)
                            onBackPressed()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<BuyMembership?>, t: Throwable) {
                paymentgatewayProgressbar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                t.message?.let { showMembershipConfirmationDialog(it) }
                onBackPressed()
            }

        })
    }

    private fun redirectOne(tapId: String){
        paymentgatewayProgressbar.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(arrayOf("lang","tap_id"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].toString(),
                tapId))

        val call = apiInterface.buySubscription(builder.build())
        call!!.enqueue(object : Callback<BuySubscriptionResponse?>{
            override fun onResponse(
                call: Call<BuySubscriptionResponse?>,
                response: Response<BuySubscriptionResponse?>
            ) {
                paymentgatewayProgressbar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ISFEATURED, true)
                            startActivity(Intent(this@TapPaymentActivity, HomeActivity::class.java))
                            finish()
                        }else{
                            onBackPressed()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<BuySubscriptionResponse?>, t: Throwable) {
                paymentgatewayProgressbar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                t.message?.let { showMembershipConfirmationDialog(it) }
                onBackPressed()
            }

        })
    }

    private fun showMembershipConfirmationDialog(message: String) {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(applicationContext)
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        if (status==0) {
            customToastLayout.tv_booking_confirmed_message.text = message
        }else{
            customToastLayout.tv_booking_confirmed_message.text = message
        }
        customToast.show()
    }
}
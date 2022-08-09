package com.heena.supplier.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.application.MyApp
import com.heena.supplier.extras.MyWebViewClient
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.rest.APIUtils.ServicePayment
import com.heena.supplier.rest.APIUtils.ServicePaymentTOKEN
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.DPOPaymentRedirectURL
import kotlinx.android.synthetic.main.activity_payment_details.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class PaymentDetailsActivity : AppCompatActivity() {
    var TransToken = ""
    var TransID = ""
    var TransactionToken = ""
    var user_id = ""
    var membership_id = ""
    var membership_price = ""
    var subscription_id = ""
    var subscription_price = ""
    var days = ""
    var starting_at = ""
    var ending_at = ""
    var type = ""
    private var isRegister : Boolean = false
    private var isPurchaseMembership : Boolean = false
    private var isPurchaseSubscriptions : Boolean = false
    private var mContext : Context?=null
    private lateinit var cancelPaymentDialog: AlertDialog.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_payment_details)
        Utility.changeLanguage(
            this,
            MyApp.sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )

        if (intent.extras!=null){
            TransToken = intent.getStringExtra("TransToken").toString()
            user_id = intent.getStringExtra("user_id").toString()
            membership_id = intent.getStringExtra("membership_id").toString()
            membership_price = intent.getStringExtra("membership_price").toString()
            subscription_id = intent.getStringExtra("subscription_id").toString()
            type = intent.getStringExtra("type").toString()
            subscription_price = intent.getStringExtra("subscription_price").toString()
            days = intent.getStringExtra("days").toString()
            starting_at = intent.getStringExtra("starting_at").toString()
            ending_at = intent.getStringExtra("ending_at").toString()
            isRegister = intent.getBooleanExtra("isRegister", false)
            isPurchaseMembership = intent.getBooleanExtra("isPurchaseMembership", false)
            isPurchaseSubscriptions = intent.getBooleanExtra("isPurchaseSubscriptions", false)
        }

        if (!Utility.hasConnection(this)){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    webView()
                }
            })
            noInternetDialog.show(supportFragmentManager, "Payment Fragment")
        }else{
            webView()
        }
    }

    private fun webView() {
        webViewPayment.webViewClient = MyWebViewClient()
        webViewPayment.settings.javaScriptEnabled = true
        webViewPayment.settings.setSupportZoom(true)
        webViewPayment.settings.builtInZoomControls = true
        //Enable Multitouch if supported by ROM
        webViewPayment.settings.useWideViewPort = true
        webViewPayment.settings.loadWithOverviewMode = false
        webViewPayment.setBackgroundColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= 11) webViewPayment?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
        webViewPayment.loadUrl(Utility.paymentURL +"${ServicePaymentTOKEN}")

        webViewPayment.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                super.onProgressChanged(view, progress)
                paymentFragmentProgressBar.visibility = View.GONE

            }
        }
        webViewPayment.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                paymentFragmentProgressBar.visibility = View.GONE
                return if(!url.equals("")){
                    if(url!!.contains(TransToken)){
                        ServicePayment = false
                        webViewPayment.loadUrl(url)

                        webViewPayment.webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, progress: Int) {
                                super.onProgressChanged(view, progress)
                                paymentFragmentProgressBar.visibility = View.GONE

                            }
                        }
                        webViewPayment.webViewClient = object : WebViewClient(){
                            override fun shouldOverrideUrlLoading(
                                view: WebView, url: String?
                            ): Boolean {
                                paymentFragmentProgressBar.visibility = View.GONE
                                Log.e("url", url.toString())
                                if (url.toString().contains("$DPOPaymentRedirectURL?TransID=")){
                                    ServicePayment = false
                                    TransID =url!!.split("TransID=")[1].split("&")[0]
                                    TransactionToken = url.split("TransID=")[1].split("&")[3].split("TransactionToken=")[1]
                                    if(type.equals("1")){
                                        successtransaction()
                                    }else{
                                        successtransactionSub()
                                    }

                                }else{
                                    true
                                }
                                return true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                paymentFragmentProgressBar.visibility = View.GONE
                                super.onPageFinished(view, url)
                            }
                        }
                    }
                    true
                }else{
                    true
                }
                return true
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                paymentFragmentProgressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }
    }


    fun successtransaction(){
        paymentFragmentProgressBar.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(
            arrayOf(
                "user_id","membership_id", "membership_price", "TransactionToken", "TransID"
            ),
            arrayOf(
                user_id,
                membership_id,
                membership_price, TransactionToken, TransID)
        )



        val call = Utility.apiInterface.successtransaction(builder.build())
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                paymentFragmentProgressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful) {
                        if (response.body() != null) {
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getInt("status") == 1) {
                                if (isRegister && !isPurchaseMembership && !isPurchaseSubscriptions){
                                    /*  membership = response.body()!!.membership
                                      sharedPreferenceInstance!!.saveMembershipInfo(this@TapPaymentActivity,response.body()!!.membership)*/
                                    showMembershipConfirmationDialog(getString(R.string.membership_status),getString(R.string.membership_purchased_successfully))
                                    startActivity(Intent(this@PaymentDetailsActivity, LoginActivity::class.java))
                                    finish()
                                }else if (!isRegister && isPurchaseMembership && !isPurchaseSubscriptions){
                                    showMembershipConfirmationDialog(getString(R.string.membership_status),getString(R.string.membership_purchased_successfully))
                                    startActivity(Intent(this@PaymentDetailsActivity, HomeActivity::class.java))
                                    finish()
                                }else{
                                    showMembershipConfirmationDialog(getString(R.string.subscription_status),getString(R.string.subscription_purchased_successfully))
                                    MyApp.sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISFEATURED, true)
                                    startActivity(Intent(this@PaymentDetailsActivity, HomeActivity::class.java))
                                    finish()
                                }
                            } else {
                                showMembershipConfirmationDialog(getString(R.string.membership_status),jsonObject.getInt("message").toString())
                                onBackPressed()
                            }


                        } else {
                            Utility.showSnackBarOnResponseError(paymentFragmentDPOConstraintLayout,
                                getString(R.string.payment_failed),
                                this@PaymentDetailsActivity)

                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController(R.id.homeFragment)
                            }, 3000)
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(paymentFragmentDPOConstraintLayout,
                            getString(R.string.payment_failed),
                            this@PaymentDetailsActivity)

                        Handler(Looper.getMainLooper()).postDelayed({
                            findNavController(R.id.homeFragment)
                        }, 3000)
                    }

                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error", t.message.toString())
                Utility.showSnackBarOnResponseError(paymentFragmentDPOConstraintLayout,
                    getString(R.string.payment_failed),
                    this@PaymentDetailsActivity)

                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController(R.id.homeFragment)
                }, 3000)
            }

        })
    }

    fun successtransactionSub(){
        paymentFragmentProgressBar.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(
            arrayOf(
                "user_id","subscription_id", "subscription_price", "TransactionToken", "TransID", "days", "starting_at", "ending_at"
            ),
            arrayOf(
                user_id,
                subscription_id,
                subscription_price, TransactionToken, TransID,days, starting_at, ending_at )
        )



        val call = Utility.apiInterface.successtransactionSub(builder.build())
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                paymentFragmentProgressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful) {
                        if (response.body() != null) {
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getInt("status") == 1) {
                                if (isRegister && !isPurchaseMembership && !isPurchaseSubscriptions){
                                    /*  membership = response.body()!!.membership
                                      sharedPreferenceInstance!!.saveMembershipInfo(this@TapPaymentActivity,response.body()!!.membership)*/
                                    showMembershipConfirmationDialog(getString(R.string.membership_status),getString(R.string.membership_purchased_successfully))
                                    startActivity(Intent(this@PaymentDetailsActivity, LoginActivity::class.java))
                                    finish()
                                }else if (!isRegister && isPurchaseMembership && !isPurchaseSubscriptions){
                                    showMembershipConfirmationDialog(getString(R.string.membership_status),getString(R.string.membership_purchased_successfully))
                                    startActivity(Intent(this@PaymentDetailsActivity, HomeActivity::class.java))
                                    finish()
                                }else{
                                    showMembershipConfirmationDialog(getString(R.string.subscription_status),getString(R.string.subscription_purchased_successfully))
                                    MyApp.sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISFEATURED, true)
                                    startActivity(Intent(this@PaymentDetailsActivity, HomeActivity::class.java))
                                    finish()
                                }
                            } else {
                                showMembershipConfirmationDialog(getString(R.string.membership_status),jsonObject.getInt("message").toString())
                                onBackPressed()
                            }


                        } else {
                            Utility.showSnackBarOnResponseError(paymentFragmentDPOConstraintLayout,
                                getString(R.string.payment_failed),
                                this@PaymentDetailsActivity)

                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController(R.id.homeFragment)
                            }, 3000)
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(paymentFragmentDPOConstraintLayout,
                            getString(R.string.payment_failed),
                            this@PaymentDetailsActivity)

                        Handler(Looper.getMainLooper()).postDelayed({
                            findNavController(R.id.homeFragment)
                        }, 3000)
                    }

                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error", t.message.toString())
                Utility.showSnackBarOnResponseError(paymentFragmentDPOConstraintLayout,
                    getString(R.string.payment_failed),
                    this@PaymentDetailsActivity)

                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController(R.id.homeFragment)
                }, 3000)
            }

        })
    }

    private fun showMembershipConfirmationDialog(title: String,message: String) {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(applicationContext)
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        customToastLayout.tv_booking_confirmed.text = title
        customToastLayout.tv_booking_confirmed_message.text = message
        customToast.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        cancelPaymentDialog = AlertDialog.Builder(this)

        cancelPaymentDialog.setTitle(R.string.cancel_payment)
        cancelPaymentDialog.setMessage(R.string.cancel_payment_message)
        cancelPaymentDialog.setCancelable(false)
        cancelPaymentDialog.setPositiveButton(R.string.yes
        ) { _, _ ->
            finish()
            showMembershipConfirmationDialog(R.string.payment_status.toString(),R.string.cancel_message.toString())
            startActivity(Intent(this@PaymentDetailsActivity, HomeActivity::class.java))
        }

        cancelPaymentDialog.setNegativeButton(R.string.no
        ) { p0, _ -> p0!!.cancel() }


        val cancelAlert = cancelPaymentDialog.create()
        cancelAlert.setTitle(R.string.cancel_payment)
        cancelAlert.show()
    }
}
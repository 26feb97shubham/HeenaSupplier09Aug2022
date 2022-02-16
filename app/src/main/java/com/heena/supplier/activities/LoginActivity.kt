package com.heena.supplier.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.broadcastreceiver.ConnectivityReceiver
import com.heena.supplier.models.LoginResponse
import com.heena.supplier.models.RegisterVerifyResendResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.getFCMToken
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.activity_login2.progressBar
import kotlinx.android.synthetic.main.activity_login2.tv_login
import kotlinx.android.synthetic.main.activity_otp_verification2.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class LoginActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var remembered=false
    private var username = ""
    private var password = ""
    private var emailaddress = ""
    private var myuserId = 0
    private var showPass = false
    private lateinit var networkChangeReceiver : ConnectivityReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)
        tv_signup.text = getString(R.string.sign_up)
        if(!this::networkChangeReceiver.isInitialized){
            networkChangeReceiver = ConnectivityReceiver()
            networkChangeReceiver.NetworkChangeReceiver(this)
        }else{
            networkChangeReceiver.NetworkChangeReceiver(this)
        }
        setUpViews()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpViews() {
        if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.IsRemembered, false]){
            remembered = true
            if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
            }
            username=SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.Username, ""]
            password=SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.Password, ""]
            edtUsername.setText(username)
            edtPass.setText(password)
        }else{
            remembered = false
            edtUsername.setText("")
            edtPass.setText("")
            if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ellipse, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ellipse, 0)
            }
        }

        iv_pass_show_hide_login.setSafeOnClickListener {
            if (showPass){
                showPass = false
                edtPass.transformationMethod = null
                iv_pass_show_hide_login.setImageResource(R.drawable.visible)
            }else{
                showPass = true
                edtPass.transformationMethod = PasswordTransformationMethod()
                iv_pass_show_hide_login.setImageResource(R.drawable.invisible)
            }
        }

        chkRememberMe.setOnClickListener {
            if(remembered){
                remembered=false
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.Username, username)
                if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]=="en") {
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ellipse, 0, 0, 0)
                }
                else{
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ellipse, 0)
                }

            }
            else{
                remembered=true
                if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]=="en") {
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0)
                }
                else{
                    chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
                }
            }
        }


        txtForgotPass.setSafeOnClickListener {
            txtForgotPass.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }


        btnLogin.setSafeOnClickListener {
            btnLogin.startAnimation(AlphaAnimation(1f, 0.5f))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this, btnLogin)
            if(!Utility.hasConnection(this)){
                val noInternetDialog = NoInternetDialog()
                noInternetDialog.isCancelable = false
                noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                    override fun retry() {
                        noInternetDialog.dismiss()
                        validateAndLogin()
                    }

                })
                noInternetDialog.show(supportFragmentManager, "Login Activity")
            }else{
                validateAndLogin()
            }

        }

        tv_signup.setSafeOnClickListener {
            tv_login.startAnimation(AlphaAnimation(1f,0.5f))
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        scrollViewlogin.setOnTouchListener { _, _ ->
            edtUsername.clearFocus()
            edtPass.clearFocus()
            false
        }

    }


    private fun validateAndLogin() {
        username = edtUsername.text.toString().trim()
        password= edtPass.text.toString().trim()

        when {
            TextUtils.isEmpty(username) -> {
                edtUsername.requestFocus()
                edtUsername.error=getString(R.string.please_enter_your_username)

            }
            TextUtils.isEmpty(password) -> {
                edtPass.requestFocus()
                edtPass.error=getString(R.string.please_enter_your_password)
            }
            else -> {
                if(remembered){
                    SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsRemembered, remembered)
                    SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.Username, username)
                    SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.Password, password)
                } else{
                    SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsRemembered, remembered)
                    SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.Username, "")
                    SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.Password, "")
                }
                getLogin()
            }
        }
    }

    private fun getLogin() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        progressBar.visibility= View.VISIBLE

        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        val builder = APIClient.createBuilder(arrayOf("username", "password", "device_token","device_type","lang"),
                arrayOf(username.trim { it <= ' ' },
                        password.trim { it <= ' ' },
                        SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.FCMTOKEN, ""]
                            .toString(),
                        "1",
                        SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]
                            .toString()))

        val call = apiInterface.login(builder.build())
        call!!.enqueue(object : Callback<LoginResponse?> {
            override fun onResponse(call: Call<LoginResponse?>, response: Response<LoginResponse?>) {
                progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        when (response.body()!!.status) {
                            1 -> {
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsLogin, true)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsVerified, true)
                                myuserId = response.body()!!.user!!.user_id!!
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserId, myuserId)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ProfilePic, response.body()!!.user!!.image)
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java).putExtra(SharedPreferenceUtility.UserId, myuserId))
                            }
                            2 -> {
                                emailaddress = response.body()!!.user!!.email!!
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserEmail, emailaddress)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsLogin, false)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsVerified, false)
                                sendOTP()
                                startActivity(Intent(this@LoginActivity, OtpVerificationActivity::class.java).putExtra("ref", "1").putExtra("emailaddress",
                                    SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserEmail, ""]
                                ))
                            }
                            3 -> {
                                emailaddress = response.body()!!.user!!.email!!
                                myuserId = response.body()!!.user!!.user_id!!
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserEmail, emailaddress)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserId, myuserId)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsLogin, false)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsVerified, false)
                                LogUtils.shortToast(this@LoginActivity, getString(R.string.inactive_account))
                            }
                            4 -> {
                                emailaddress = response.body()!!.user!!.email!!
                                myuserId = response.body()!!.user!!.user_id!!
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserEmail, emailaddress)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsLogin, false)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsVerified, true)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserId, myuserId)
                                startActivity(Intent(this@LoginActivity, MembershipRegistrationActivity::class.java).putExtra("emailaddress",
                                    SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserEmail, ""]
                                ))
                            }
                            else -> {
                                LogUtils.longToast(this@LoginActivity, response.body()!!.message)
                            }
                        }
                    }else{
                        LogUtils.longToast(this@LoginActivity, getString(R.string.response_isnt_successful))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(this@LoginActivity, getString(R.string.check_internet))
                progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun sendOTP() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        progressBar.visibility= View.VISIBLE


        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        val builder = APIClient.createBuilder(arrayOf("email", "lang"),
            arrayOf(emailaddress, SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.registerverivyresend(builder.build())

        call!!.enqueue(object : Callback<RegisterVerifyResendResponse?>{
            override fun onResponse(call: Call<RegisterVerifyResendResponse?>, response: Response<RegisterVerifyResendResponse?>) {
                progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsResend, true)
                        }else{
                            LogUtils.longToast(this@LoginActivity, response.body()!!.message)
                        }
                    }else{
                        LogUtils.longToast(this@LoginActivity, getString(R.string.response_isnt_successful))
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<RegisterVerifyResendResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(this@LoginActivity, getString(R.string.check_internet))
                progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
        if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.IsRemembered, false]){
            if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
            }
            username=SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.Username, ""]
            password=SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.Password, ""]
            edtUsername.setText(username)
            edtPass.setText(password)
        }else{
            edtUsername.setText("")
            edtPass.setText("")
            if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]=="en") {
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ellipse, 0, 0, 0)
            }
            else{
                chkRememberMe.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ellipse, 0)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver)
        }
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
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

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        getFCMToken()
    }
}
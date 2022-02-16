package com.heena.supplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.widget.doOnTextChanged
import com.heena.supplier.R
import com.heena.supplier.models.ForgotPasswordResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_forgot_password2.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {
    var emailAddress: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password2)
        setUpViews()
    }

    private fun setUpViews() {
        edtemailaddress.doOnTextChanged { _, _, _, _ ->
            if (!TextUtils.isEmpty(emailAddress) && !SharedPreferenceUtility.getInstance().isEmailValid(emailAddress!!)) {
                scrollView.scrollTo(0, 210)
                edtemailaddress_signup.requestFocus()
                edtemailaddress_signup.error=getString(R.string.please_enter_valid_email)
            }
        }

        btnSubmit.setSafeOnClickListener {
            btnSubmit.startAnimation(AlphaAnimation(1f, 0.5f))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this, btnSubmit)
            validateAndForgot()
        }
    }

    private fun validateAndForgot() {
        emailAddress = edtemailaddress.text.toString().trim()

        if(TextUtils.isEmpty(emailAddress)){
            edtemailaddress.requestFocus()
            edtemailaddress.error=getString(R.string.please_enter_valid_email)
            LogUtils.longToast(this, getString(R.string.please_enter_valid_email))
        }else{
            forgotPassword()
        }
    }

    private fun forgotPassword() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        frgt_pass_progressBar.visibility = View.VISIBLE

        val builder = APIClient.createBuilder(arrayOf("email", "lang"),
                arrayOf(emailAddress!!.trim { it <= ' ' },
                        SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]
                            .toString()))

        val call = apiInterface.forgotPassword(builder.build())
        call!!.enqueue(object : Callback<ForgotPasswordResponse?> {
            override fun onResponse(call: Call<ForgotPasswordResponse?>, response: Response<ForgotPasswordResponse?>) {
                frgt_pass_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        if (response.body()!!.status == 1){
                            LogUtils.longToast(this@ForgotPasswordActivity, response.body()!!.message)
                            startActivity(
                                Intent(this@ForgotPasswordActivity, OtpVerificationActivity::class.java).putExtra("ref", "2").putExtra("emailaddress", emailAddress))
                            finishAffinity()
                        }else{
                            LogUtils.longToast(this@ForgotPasswordActivity, response.body()!!.message)
                        }
                    }else{
                        LogUtils.longToast(this@ForgotPasswordActivity, getString(R.string.response_isnt_successful))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ForgotPasswordResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(this@ForgotPasswordActivity, getString(R.string.check_internet))
                progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
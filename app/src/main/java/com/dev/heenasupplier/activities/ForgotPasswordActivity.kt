package com.dev.heenasupplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.dev.heenasupplier.R
import com.dev.heenasupplier.models.ForgotPasswordResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
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
        edtemailaddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(emailAddress) && !SharedPreferenceUtility.getInstance().isEmailValid(emailAddress!!)) {
                    scrollView.scrollTo(0, 210)
                    edtemailaddress_signup.requestFocus()
                    edtemailaddress_signup.error=getString(R.string.please_enter_valid_email)
                }

            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        btnSubmit.setOnClickListener {
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

        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        val builder = APIClient.createBuilder(arrayOf("email", "lang"),
                arrayOf(emailAddress!!.trim({ it <= ' ' }),
                        SharedPreferenceUtility.getInstance()
                            .get(SharedPreferenceUtility.SelectedLang, "")
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
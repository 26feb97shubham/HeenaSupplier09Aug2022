package com.dev.heenasupplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.dev.heenasupplier.R
import com.dev.heenasupplier.models.ResetPasswordResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.ConstClass
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.activity_sign_up2.progressBar
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {
    lateinit var apiInterface: APIInterface
    lateinit var builder : FormBody.Builder
    var password : String?= null
    var cnfrmpass : String? = null
    var emailaddress : String?=null
    var otp : String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        if (intent.extras!=null){
            emailaddress = intent.getStringExtra(ConstClass.EMAILADDRESS).toString()
            otp = intent.getStringExtra("otp").toString()
        }

        btnSubmit.setOnClickListener {
            validation()
        }

    }

    private fun validation() {
        password = edtPassword.text.toString().trim()
        cnfrmpass = edtConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(password.toString())) {
            edtPassword.requestFocus()
            edtPassword.error=getString(R.string.please_enter_your_password)
            LogUtils.shortToast(this, getString(R.string.please_enter_your_password))
        }
        else if (password.toString()!!.length < 6) {
            edtPassword.requestFocus()
            edtPassword.error=getString(R.string.verify_password_length_valid)
            LogUtils.shortToast(this, getString(R.string.verify_password_length_valid))

        }
        else if (!SharedPreferenceUtility.getInstance().isPasswordValid(password.toString()!!)) {
            edtPassword.requestFocus()
            edtPassword.error=getString(R.string.password_length_valid)
            LogUtils.shortToast(this, getString(R.string.password_length_valid))
        }
        else if (TextUtils.isEmpty(cnfrmpass.toString())) {
            edtConfirmPassword.requestFocus()
            edtConfirmPassword.error=getString(R.string.please_verify_your_password)
            LogUtils.shortToast(this, getString(R.string.please_verify_your_password))
        }
        else if (!cnfrmpass.toString().equals(password.toString())) {
            edtConfirmPassword.requestFocus()
            edtConfirmPassword.error=getString(R.string.password_doesnt_match_with_verify_password)
            LogUtils.shortToast(this, getString(R.string.password_doesnt_match_with_verify_password))
        }else{
            getResetPass()
        }
    }

    private fun getResetPass() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        reset_pass_progressBar.visibility= View.VISIBLE

        apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        builder = APIClient.createBuilder(arrayOf("email", "otp", "lang", "password"),
        arrayOf(emailaddress!!,otp!!, SharedPreferenceUtility.getInstance()
            .get(SharedPreferenceUtility.SelectedLang, "")
            .toString(),cnfrmpass!!))

        val call = apiInterface.resetpassword(builder.build())
        call!!.enqueue(object : retrofit2.Callback<ResetPasswordResponse?> {
            override fun onResponse(
                call: Call<ResetPasswordResponse?>,
                response: Response<ResetPasswordResponse?>
            ) {
                reset_pass_progressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        startActivity(
                            Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                    }else{
                        LogUtils.longToast(this@ResetPasswordActivity, response.body()!!.message)
                    }
                }else{
                    LogUtils.longToast(this@ResetPasswordActivity, getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse?>, t: Throwable) {
                LogUtils.e("msg", t.message)
                LogUtils.shortToast(this@ResetPasswordActivity,t.localizedMessage)
                reset_pass_progressBar.visibility = View.GONE
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
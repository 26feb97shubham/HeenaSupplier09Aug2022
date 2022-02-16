package com.heena.supplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.heena.supplier.R
import com.heena.supplier.models.ResetPasswordResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.ConstClass
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {
    lateinit var builder : FormBody.Builder
    var password : String?= null
    private var cnfrmpass : String? = null
    var emailaddress : String?=null
    private var otp : String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        if (intent.extras!=null){
            emailaddress = intent.getStringExtra(ConstClass.EMAILADDRESS).toString()
            otp = intent.getStringExtra("otp").toString()
        }

        btnSubmit.setSafeOnClickListener {
            validation()
        }

    }

    private fun validation() {
        password = edtPassword.text.toString().trim()
        cnfrmpass = edtConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(password.toString())) {
            edtPassword.requestFocus()
            edtPassword.error=getString(R.string.please_enter_your_password)
        }
        else if (password.toString().length < 6) {
            edtPassword.requestFocus()
            edtPassword.error=getString(R.string.verify_password_length_valid)
        }
        else if (!SharedPreferenceUtility.getInstance().isPasswordValid(password.toString())) {
            edtPassword.requestFocus()
            edtPassword.error=getString(R.string.password_length_valid)
        }
        else if (TextUtils.isEmpty(cnfrmpass.toString())) {
            edtConfirmPassword.requestFocus()
            edtConfirmPassword.error=getString(R.string.please_verify_your_password)
        }
        else if (cnfrmpass.toString() != password.toString()) {
            edtConfirmPassword.requestFocus()
            edtConfirmPassword.error=getString(R.string.password_doesnt_match_with_verify_password)
        }else{
            getResetPass()
        }
    }

    private fun getResetPass() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        reset_pass_progressBar.visibility= View.VISIBLE

        builder = APIClient.createBuilder(arrayOf("email", "otp", "lang", "password"),
        arrayOf(emailaddress!!,otp!!, SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]
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
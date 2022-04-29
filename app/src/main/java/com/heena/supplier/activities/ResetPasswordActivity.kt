package com.heena.supplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.ResetPasswordResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.ConstClass
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
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
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
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
            Utility.showSnackBarValidationError(resetPasswordActivityConstraintLayout,
                getString(R.string.please_enter_your_password),
                this)
        }
        else if (password.toString().length < 6) {
            Utility.showSnackBarValidationError(resetPasswordActivityConstraintLayout,
                getString(R.string.password_length_valid),
                this)
        }
        else if (!sharedPreferenceInstance!!.isPasswordValid(password.toString())) {
            Utility.showSnackBarValidationError(resetPasswordActivityConstraintLayout,
                getString(R.string.invalid_password),
                this)
        }
        else if (TextUtils.isEmpty(cnfrmpass.toString())) {
            edtConfirmPassword.requestFocus()
            edtConfirmPassword.error=getString(R.string.please_enter_your_confirm_password)
            Utility.showSnackBarValidationError(resetPasswordActivityConstraintLayout,
                getString(R.string.please_enter_your_password),
                this)
        }
        else if (cnfrmpass.toString() != password.toString()) {
            Utility.showSnackBarValidationError(resetPasswordActivityConstraintLayout,
                getString(R.string.password_doesnt_match_with_verify_password),
                this)
        }else{
            getResetPass()
        }
    }

    private fun getResetPass() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        reset_pass_progressBar.visibility= View.VISIBLE

        builder = APIClient.createBuilder(arrayOf("email", "otp", "lang", "password"),
        arrayOf(emailaddress!!,otp!!, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
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
                        Utility.showSnackBarOnResponseSuccess(resetPasswordActivityConstraintLayout,
                            response.body()!!.message.toString(),
                            this@ResetPasswordActivity)
                        Handler().postDelayed({
                            startActivity(
                                Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                        }, 1200)

                    }else{
                        Utility.showSnackBarOnResponseError(resetPasswordActivityConstraintLayout,
                            response.body()!!.message.toString(),
                            this@ResetPasswordActivity)
                    }
                }else{
                    Utility.showSnackBarOnResponseError(resetPasswordActivityConstraintLayout,
                        getString(R.string.response_isnt_successful),
                        this@ResetPasswordActivity)
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse?>, t: Throwable) {
                LogUtils.e("msg", t.message)
                Utility.showSnackBarOnResponseError(resetPasswordActivityConstraintLayout,
                    getString(R.string.check_internet),
                    this@ResetPasswordActivity)
                reset_pass_progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }
}
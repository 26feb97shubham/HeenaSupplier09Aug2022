package com.heena.supplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.widget.doOnTextChanged
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.ForgotPasswordVerifyResponse
import com.heena.supplier.models.RegisterVerifyResendResponse
import com.heena.supplier.models.RegistrationVerifyResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.ConstClass
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_otp_verification2.*
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.Callback
import org.json.JSONException
import retrofit2.Response
import java.io.IOException


class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var ref: String
    private lateinit var pin: String
    lateinit var emailaddress : String
    lateinit var mobileNumber : String
    lateinit var builder : FormBody.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification2)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        setUpViews()
    }

    private fun setUpViews() {
        if(intent.extras!=null){
            emailaddress = intent.getStringExtra(ConstClass.EMAILADDRESS).toString()
            mobileNumber = intent.getStringExtra("mobile_no").toString()
            ref = intent.getStringExtra("ref").toString()
        }

        pin = ""

        btnVerify.isEnabled=false
        btnVerify.alpha=0.5f

        firstPinView.doOnTextChanged { text, _, _, _ ->
            if(text!!.length==4){
                btnVerify.isEnabled=true
                btnVerify.alpha=1f
                sharedPreferenceInstance!!.hideSoftKeyBoard(this@OtpVerificationActivity, firstPinView)
            }
            else{
                btnVerify.isEnabled=false
                btnVerify.alpha=0.5f
            }
        }

        pin = firstPinView.text.toString().trim()


        btnVerify.setSafeOnClickListener {
            btnVerify.startAnimation(AlphaAnimation(1f, 0.5f))
            validateAndVerification()

        }

        resend.setSafeOnClickListener {
            resend.startAnimation(AlphaAnimation(1f, 0.5f))
            firstPinView.setText("")
            sharedPreferenceInstance!!.hideSoftKeyBoard(this@OtpVerificationActivity, it)
            resendOtp()
        }
    }

    private fun validateAndVerification() {
        pin = firstPinView.text.toString().trim()
        if (TextUtils.isEmpty(pin)) {
            Utility.showSnackBarValidationError(otpVerificationActivityConstraintLayout,
                getString(R.string.please_enter_your_otp),
                this)
        }
        else if ((pin.length < 4)) {
            Utility.showSnackBarValidationError(otpVerificationActivityConstraintLayout,
                getString(R.string.otp_length_valid),
                this)
        }

        else {
            if(ref=="1"){
                verifyAccount()
            }
            else{
                forgotPassVerify()
            }
        }

    }

    private fun forgotPassVerify() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otpVerificationprogressBar.visibility= View.VISIBLE
        builder = APIClient.createBuilder(arrayOf("email", "otp", "lang"),
            arrayOf(emailaddress, pin.trim { it <= ' ' }, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
                .toString()))


        val call = apiInterface.forgotpassverifyotp(builder.build())
        call!!.enqueue(object : Callback<ForgotPasswordVerifyResponse?>{
            override fun onResponse(
                call: Call<ForgotPasswordVerifyResponse?>,
                response: Response<ForgotPasswordVerifyResponse?>
            ) {
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        Utility.showSnackBarOnResponseSuccess(otpVerificationActivityConstraintLayout,
                            response.body()!!.message.toString(),
                            this@OtpVerificationActivity)

                        startActivity(
                            Intent(this@OtpVerificationActivity, ResetPasswordActivity::class.java).
                            putExtra(ConstClass.EMAILADDRESS, emailaddress).
                            putExtra("otp",pin))
                    }else{
                        Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                            response.body()!!.message.toString(),
                            this@OtpVerificationActivity)
                    }
                }else{
                    Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                        getString(R.string.response_isnt_successful),
                        this@OtpVerificationActivity)
                }
            }

            override fun onFailure(call: Call<ForgotPasswordVerifyResponse?>, t: Throwable) {
                LogUtils.e("msg", t.message)
                Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                    getString(R.string.response_isnt_successful),
                    this@OtpVerificationActivity)
                otpVerificationprogressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun verifyAccount() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otpVerificationprogressBar.visibility= View.VISIBLE
        builder = APIClient.createBuilder(arrayOf("email", "otp", "lang"),
                arrayOf(emailaddress, pin.trim { it <= ' ' }, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
                    .toString()))
         val call = apiInterface.verifyotp(builder.build())
         call!!.enqueue(object : Callback<RegistrationVerifyResponse?> {
             override fun onResponse(call: Call<RegistrationVerifyResponse?>, response: Response<RegistrationVerifyResponse?>) {
                 otpVerificationprogressBar.visibility= View.GONE
                 window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                 try {
                     if (response.isSuccessful){
                         if (response.body()!!.status==1){
                             sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsVerified, true)
                             sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsLogin, false)
                             Utility.showSnackBarOnResponseSuccess(otpVerificationActivityConstraintLayout,
                                 getString(R.string.thank_you_for_registration),
                                 this@OtpVerificationActivity)
                             Handler(Looper.getMainLooper()).postDelayed({ startActivity(Intent(this@OtpVerificationActivity, LoginActivity::class.java)) }, 1000)
                         }else{
                             Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                                 response.body()!!.message.toString(),
                                 this@OtpVerificationActivity)
                         }
                     }else{
                         Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                             getString(R.string.response_isnt_successful),
                             this@OtpVerificationActivity)
                     }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<RegistrationVerifyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                    getString(R.string.check_internet),
                    this@OtpVerificationActivity)
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }
    private fun resendOtp() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otpVerificationprogressBar.visibility= View.VISIBLE
        builder = APIClient.createBuilder(arrayOf("email", "lang", "country_code", "phone"),
                arrayOf(emailaddress, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""], "+971", sharedPreferenceInstance!![SharedPreferenceUtility.PhoneNumber, ""]))
        val call = apiInterface.registerverivyresend(builder.build())

        call!!.enqueue(object : Callback<RegisterVerifyResendResponse?>{
            override fun onResponse(call: Call<RegisterVerifyResendResponse?>, response: Response<RegisterVerifyResendResponse?>) {
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsResend, true)
                            Utility.showSnackBarOnResponseSuccess(otpVerificationActivityConstraintLayout,
                                response.body()!!.message.toString(),
                                this@OtpVerificationActivity)
                        }else{
                            Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                                response.body()!!.message.toString(),
                                this@OtpVerificationActivity)
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                            getString(R.string.response_isnt_successful),
                            this@OtpVerificationActivity)
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
                Utility.showSnackBarOnResponseError(otpVerificationActivityConstraintLayout,
                    getString(R.string.check_internet),
                    this@OtpVerificationActivity)
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }


    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }
}
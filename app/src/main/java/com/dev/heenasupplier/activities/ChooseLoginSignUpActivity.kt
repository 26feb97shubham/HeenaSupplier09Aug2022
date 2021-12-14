package com.dev.heenasupplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.Toast
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_choose_login_sign_up2.*

class ChooseLoginSignUpActivity : AppCompatActivity() {
    private var doubleClick:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_login_sign_up2)
        setUpViews()
    }
    private fun setUpViews() {
        btnLogin.setOnClickListener {
            btnLogin.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, LoginActivity::class.java))
        }
        btnSignUp.setOnClickListener {
            btnSignUp.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    override fun onBackPressed() {
        exitApp()
    }
    private fun exitApp() {
        val toast = Toast.makeText(
            this,
            getString(R.string.please_click_back_again_to_exist),
            Toast.LENGTH_SHORT
        )


        if(doubleClick){
            finishAffinity()
            doubleClick=false
        }
        else{

            doubleClick=true
            Handler(Looper.getMainLooper()).postDelayed({
                toast.show()
                doubleClick=false
            }, 500)
        }
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
package com.heena.supplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AlphaAnimation
import com.heena.supplier.R
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_choose_login_sign_up2.*

class ChooseLoginSignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_login_sign_up2)
        setUpViews()
    }
    private fun setUpViews() {
        btnLogin.setSafeOnClickListener {
            btnLogin.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, LoginActivity::class.java))
        }
        btnSignUp.setSafeOnClickListener {
            btnSignUp.startAnimation(AlphaAnimation(1f, 0.5f))
            startActivity(Intent(this, SignUpActivity::class.java))
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

}
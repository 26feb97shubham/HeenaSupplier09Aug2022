package com.heena.supplier.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.heena.supplier.R
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.SharedPreferenceUtility.Companion.FIRSTTIME
import com.heena.supplier.utils.Utility
import com.google.firebase.FirebaseApp
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        FirebaseApp.initializeApp(this)
        Utility.deviceId(this)
        setUpViews()
    }
    private fun setUpViews() {

        if (Utility.getLanguage().isEmpty()){
            Utility.changeLanguage(this,"ar")
        }else{
            Utility.changeLanguage(this,Utility.getLanguage())
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(!sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && !(sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, ChooseLangActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if((sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                    && !(sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, IntroductionActivity::class.java)
                    startActivity(intent)
                    finish()
                }else if((sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                    && (sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }else if((sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                    && (sharedPreferenceInstance!!.get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && (sharedPreferenceInstance!!.get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            },
            2000,
        )
    }
}
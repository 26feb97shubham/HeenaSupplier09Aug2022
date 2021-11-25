package com.dev.heenasupplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.SharedPreferenceUtility.Companion.FIRSTTIME
import com.dev.heenasupplier.utils.Utility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var selectLang:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseApp.initializeApp(this)
        Utility.deviceId(this)
        setUpViews()
    }
    private fun setUpViews() {

        if (Utility.getLanguage().isNullOrEmpty()){
            Utility.changeLanguage(this,"ar")
        }else{
            Utility.changeLanguage(this,Utility.getLanguage())
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(SharedPreferenceUtility.getInstance().get(FIRSTTIME,false)
                    && SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, ChooseLangActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, ChooseLoginSignUpActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val intent = Intent(this, ChooseLangActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            },
            2000,
        )
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
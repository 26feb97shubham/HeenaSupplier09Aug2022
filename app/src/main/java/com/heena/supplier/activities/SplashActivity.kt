package com.heena.supplier.activities

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

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
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
                if(!SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, ChooseLangActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if((SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, IntroductionActivity::class.java)
                    startActivity(intent)
                    finish()
                }else if((SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }else if((SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false))
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
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
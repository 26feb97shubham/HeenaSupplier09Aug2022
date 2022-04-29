package com.heena.supplier.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.heena.supplier.R
import com.heena.supplier.adapters.ScreenSlidePagerAdapter
import com.heena.supplier.utils.SharedPreferenceUtility
import com.google.android.material.tabs.TabLayoutMediator
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.Utility
import kotlinx.android.synthetic.main.activity_introduction2.*

class IntroductionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction2)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        setUpViews()
    }

    private fun setUpViews() {
        sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsWelcomeShow, true)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        intro_view_pager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, intro_view_pager){ _, _ ->
        }.attach()
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }

}
package com.dev.heenasupplier.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dev.heenasupplier.R
import com.dev.heenasupplier.adapters.ScreenSlidePagerAdapter
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_introduction2.*

class IntroductionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction2)
        setUpViews()
    }

    private fun setUpViews() {
        SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsWelcomeShow, true)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        intro_view_pager.adapter = pagerAdapter

        //Attaching tab to view pager
        TabLayoutMediator(tabLayout,   intro_view_pager){ _, _ ->
        }.attach()
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
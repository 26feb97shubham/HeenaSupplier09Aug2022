package com.dev.heenasupplier.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.animation.AlphaAnimation
import android.widget.Toast
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.activity_choose_lang2.*

class ChooseLangActivity : AppCompatActivity() {
    private var doubleClick:Boolean=false
    private var selectLang:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_lang2)
        setUpViews()
    }
    private fun setUpViews() {
        if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""] =="en"){
            selectEnglish()
        }

        else if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""] =="ar"){
            selectArabic()
        }

        arabicView.setOnClickListener {
            if(selectLang != "ar") {
                arabicView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectArabic()
            }
        }
        englishView.setOnClickListener {
            if(selectLang != "en") {
                englishView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectEnglish()
            }
        }

        btnContinue.setOnClickListener {
            btnContinue.startAnimation(AlphaAnimation(1f, 0.5f))
            if(TextUtils.isEmpty(selectLang)){
                LogUtils.shortToast(this, getString(R.string.please_choose_your_language))
            }
            else{
                Utility.changeLanguage(this, selectLang)
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.FIRSTTIME, true)
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ISSELECTLANGUAGE, true)
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
                startActivity(Intent(this, IntroductionActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun selectArabic() {
        selectLang = "ar"
        Utility.changeLanguage(this, selectLang)
        setTextForLang()
        SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
        arabic_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        english_sub_view.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun selectEnglish() {
        selectLang = "en"
        Utility.changeLanguage(this, selectLang)
        setTextForLang()
        SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
        english_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        arabic_sub_view.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setTextForLang(){
        tv_choose_lang.text = getString(R.string.choose_language)
        txtArabic.text = getString(R.string.arabic)
        btnContinue.text = getString(R.string.continue_btn)
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
}
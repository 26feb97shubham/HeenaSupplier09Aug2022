package com.heena.supplier.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.animation.AlphaAnimation
import android.widget.Toast
import com.heena.supplier.R
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_choose_lang2.*

class ChooseLangActivity : AppCompatActivity() {
    private var selectLang:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_lang2)
        setUpViews()
    }
    private fun setUpViews() {
        selectLang = "ar"
        if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""] =="en"){
            selectLang = SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]
            selectEnglish()
        }

        else if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""] =="ar"){
            selectLang = SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]
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

        btnContinue.setSafeOnClickListener {
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
        btnContinue.text = getString(R.string.continue_btn)
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
    }
}
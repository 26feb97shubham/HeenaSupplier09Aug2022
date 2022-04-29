package com.heena.supplier.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.animation.AlphaAnimation
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_choose_lang2.*

class ChooseLangActivity : AppCompatActivity() {
    private var selectLang:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_lang2)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        setUpViews()
    }
    private fun setUpViews() {
        selectLang = "ar"
        if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] =="en"){
            selectLang = sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
            selectEnglish()
        }

        else if(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] =="ar"){
            selectLang = sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
            selectArabic()
        }

        arabicView.setSafeOnClickListener {
            if(selectLang != "ar") {
                arabicView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectArabic()
            }
        }

        englishView.setSafeOnClickListener {
            if(selectLang != "en") {
                englishView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectEnglish()
            }
        }

        btnContinue.setSafeOnClickListener {
            btnContinue.startAnimation(AlphaAnimation(1f, 0.5f))
            if(TextUtils.isEmpty(selectLang)){
                Utility.showSnackBarValidationError(chooseLanguageActivityConstraintLayout,
                    getString(R.string.please_choose_your_language),
                    this)

            }
            else{
                Utility.changeLanguage(this, selectLang)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.FIRSTTIME, true)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.ISSELECTLANGUAGE, true)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.SelectedLang, selectLang)
                startActivity(Intent(this, IntroductionActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun selectArabic() {
        selectLang = "ar"
        setTextForLang()
        arabic_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        english_sub_view.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun selectEnglish() {
        selectLang = "en"
        setTextForLang()
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
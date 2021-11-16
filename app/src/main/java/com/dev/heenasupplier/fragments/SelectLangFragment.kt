package com.dev.heenasupplier.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.dev.heenasupplier.R
import com.dev.heenasupplier.activities.HomeActivity
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_select_lang.view.*
import java.util.*

class SelectLangFragment : Fragment() {
    var mView : View?=null
    private var selectLang:String=""
    var locale = Locale("ar")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_select_lang, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }

        if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "") =="en"){
            selectEnglish()

        }
        else if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "") =="ar"){
            selectArabic()

        }

        mView!!.arabicView.setOnClickListener {
            if(selectLang != "ar") {
                mView!!.arabicView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectArabic()
            }
        }
        mView!!.englishView.setOnClickListener {
            if(selectLang != "en") {
                mView!!.englishView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectEnglish()
            }
        }

        mView!!.btnContinue.setOnClickListener {
            /* btnContinue.startAnimation(AlphaAnimation(1f, 0.5f))
             startActivity(Intent(this, ChooseLoginSignUpActivity::class.java))*/

            mView!!.btnContinue.startAnimation(AlphaAnimation(1f, 0.5f))
            if(TextUtils.isEmpty(selectLang)){
                LogUtils.shortToast(requireContext(), getString(R.string.please_choose_your_language))
            }
            else{
                Utility.changeLanguage(requireContext(), selectLang)
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
                requireContext().startActivity(Intent(requireContext(), HomeActivity::class.java))
            }
        }
    }

    private fun selectArabic() {
        /*    selectLang = "ar"
            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
            arabic_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
            english_sub_view.setBackgroundColor(resources.getColor(android.R.color.transparent))*/
        selectLang = "ar"
        Utility.changeLanguage(requireContext(), selectLang)
        setTextForLang()
        SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
        mView!!.arabic_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        mView!!.english_sub_view.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun selectEnglish() {
        /*   selectLang = "en"
           SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
           english_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
           arabic_sub_view.setBackgroundColor(resources.getColor(android.R.color.transparent))*/

        selectLang = "en"
        Utility.changeLanguage(requireContext(), selectLang)
        setTextForLang()
        SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
        mView!!.english_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        mView!!.arabic_sub_view.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun setTextForLang(){
        mView!!.tv_choose_lang.text = getString(R.string.choose_language)
        mView!!.txtArabic.text = getString(R.string.arabic)
        mView!!.btnContinue.text = getString(R.string.continue_btn)
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
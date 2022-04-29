package com.heena.supplier.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.activities.HomeActivity
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_select_lang.view.*
import java.util.*

class SelectLangFragment : Fragment() {
    var mView : View?=null
    private var selectLang:String=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_select_lang, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }


        if(sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "") =="en"){
            selectEnglish()
        }

        else if(sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "") =="ar"){
            selectArabic()
        }

        mView!!.arabicView.setSafeOnClickListener {
            if(selectLang != "ar") {
                mView!!.arabicView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectArabic()
            }
        }
        mView!!.englishView.setSafeOnClickListener {
            if(selectLang != "en") {
                mView!!.englishView.startAnimation(AlphaAnimation(1f, 0.5f))
                selectEnglish()
            }
        }

        mView!!.btnContinue.setSafeOnClickListener {
            mView!!.btnContinue.startAnimation(AlphaAnimation(1f, 0.5f))
            if(TextUtils.isEmpty(selectLang)){
                LogUtils.shortToast(requireContext(), getString(R.string.please_choose_your_language))
            }
            else{
                Utility.changeLanguage(requireContext(), selectLang)
                sharedPreferenceInstance!!.save(SharedPreferenceUtility.SelectedLang, selectLang)
                requireContext().startActivity(Intent(requireContext(), HomeActivity::class.java))
            }
        }
    }

    private fun selectArabic() {
        selectLang = "ar"
//        Utility.changeLanguage(requireContext(), selectLang)
        setTextForLang()
//        sharedPreferenceInstance!!.save(SharedPreferenceUtility.SelectedLang, selectLang)
        mView!!.arabic_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        mView!!.english_sub_view.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun selectEnglish() {

        selectLang = "en"
//        Utility.changeLanguage(requireContext(), selectLang)
        setTextForLang()
//        sharedPreferenceInstance!!.save(SharedPreferenceUtility.SelectedLang, selectLang)
        mView!!.english_sub_view.setBackgroundResource(R.drawable.curved_white_filled_rect_box)
        mView!!.arabic_sub_view.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun setTextForLang(){
        mView!!.tv_choose_lang.text = getString(R.string.choose_language)
        mView!!.btnContinue.text = getString(R.string.continue_btn)
    }
}
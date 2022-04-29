package com.heena.supplier.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.webkit.WebView
import androidx.navigation.fragment.findNavController
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.extras.MyWebViewClient
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_c_m_s.view.*

class CMSFragment : Fragment() {
    var lang = ""
    var mView : View?=null
    var title = ""
    var about_us_url = ""
    var faq_url = ""
    var privacy_policy_url = ""
    var tnc_url = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title", "")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_c_m_s, container, false)
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

        when {
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] == "en" -> {
                about_us_url = "https://alniqasha.ae/about-us/en"
                privacy_policy_url = "https://alniqasha.ae/privacy-policy/en"
                tnc_url = "https://alniqasha.ae/terms-and-conditions/en"
                faq_url = "https://alniqasha.ae/faq/en"
            }
            else -> {
                about_us_url = "https://alniqasha.ae/about-us/ar"
                privacy_policy_url = "https://alniqasha.ae/privacy-policy/ar"
                tnc_url = "https://alniqasha.ae/terms-and-conditions/ar"
                faq_url = "https://alniqasha.ae/faq/ar"
            }
        }

        lang = sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang,"").toString()
        Utility.setLanguage(requireContext(),lang)

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    webView()
                }
            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "CMS Fragment")
        }else{
            webView()
        }

    }

    private fun webView(){
        mView!!.web_view.webViewClient = MyWebViewClient()
        mView!!.web_view.settings.javaScriptEnabled = true
        //Enable Multitouch if supported by ROM
        mView!!.web_view.getSettings().setUseWideViewPort(true)
        mView!!.web_view.getSettings().setLoadWithOverviewMode(false)
        mView!!.web_view.setBackgroundColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= 11) mView!!.web_view?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
        mView!!.tv_title.text = title
        when {
            title.equals(getString(R.string.about_us)) -> {
                mView!!.web_view.loadUrl(about_us_url)
            }
            title.equals(getString(R.string.privacy_and_policy)) -> {
                mView!!.web_view.loadUrl(privacy_policy_url)
            }
            title.equals(getString(R.string.terms_and_conditions)) -> {
                mView!!.web_view.loadUrl(tnc_url)
            }
            title.equals(getString(R.string.frequently_asked_questions)) -> {
                mView!!.web_view.loadUrl(faq_url)
            }
            else -> {
                mView!!.web_view.loadUrl(about_us_url)
            }
        }
    }
}
package com.dev.heenasupplier.fragments

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
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.extras.MyWebViewClient
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_c_m_s.view.*
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class CMSFragment : Fragment() {
    var lang = ""
    var mView : View?=null
    var title = ""
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
        setUpViews()
        return mView
    }

    private fun setUpViews() {

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    if(title.equals(getString(R.string.about_us))){
                        mView!!.web_view.loadUrl("https://henna.devtechnosys.info/about-us")
                    }else if (title.equals(getString(R.string.privacy_and_policy))){
                        mView!!.web_view.loadUrl("https://henna.devtechnosys.info/privacy-policy")
                    }else if(title.equals(getString(R.string.terms_and_conditions))){
                        mView!!.web_view.loadUrl("https://henna.devtechnosys.info/terms-and-conditions")
                    }else if (title.equals(getString(R.string.frequently_asked_questions))){
                        mView!!.web_view.loadUrl("https://henna.devtechnosys.info/faq")
                    }else{
                        mView!!.web_view.loadUrl("https://henna.devtechnosys.info/about-us")
                    }
                }
            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "CMS Fragment")
        }
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        instance = SharedPreferenceUtility.getInstance()
        lang = instance!!.get(SharedPreferenceUtility.SelectedLang,"").toString()
        Utility.setLanguage(requireContext(),lang)

        mView!!.web_view.webViewClient = MyWebViewClient()
        mView!!.web_view.settings.javaScriptEnabled = true
        mView!!.web_view.settings.setSupportZoom(true)
        mView!!.web_view.getSettings().setBuiltInZoomControls(true)
        //Enable Multitouch if supported by ROM
        mView!!.web_view.getSettings().setUseWideViewPort(true)
        mView!!.web_view.getSettings().setLoadWithOverviewMode(false)
        mView!!.web_view.setBackgroundColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= 11) mView!!.web_view?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
//        mView!!.tv_title.text = title
        if(title.equals(getString(R.string.about_us))){
            mView!!.web_view.loadUrl("https://henna.devtechnosys.info/about-us")
        }else if (title.equals(getString(R.string.privacy_and_policy))){
            mView!!.web_view.loadUrl("https://henna.devtechnosys.info/privacy-policy")
        }else if(title.equals(getString(R.string.terms_and_conditions))){
            mView!!.web_view.loadUrl("https://henna.devtechnosys.info/terms-and-conditions")
        }else if (title.equals(getString(R.string.frequently_asked_questions))){
            mView!!.web_view.loadUrl("https://henna.devtechnosys.info/faq")
        }else{
            mView!!.web_view.loadUrl("https://henna.devtechnosys.info/about-us")
        }
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

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CMSFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
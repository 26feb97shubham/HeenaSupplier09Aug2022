package com.heena.supplier.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.webkit.WebView
import android.webkit.WebViewClient
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_terms_and_conditions.*

class TermsAndConditionsActivity : AppCompatActivity() {
    private var tnc_url = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        setUpViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpViews() {
        ic_arrow_back.setSafeOnClickListener {
            ic_arrow_back.startAnimation(AlphaAnimation(1f, 0.5f))
            onBackPressed()
        }

        tnc_url =
            if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] != "en") {
                "https://alniqasha.ae/terms-and-conditions/ar"
            } else {
                "https://alniqasha.ae/terms-and-conditions/en"
            }

        tnc_webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBarTnC.visibility = View.VISIBLE
                view?.setBackgroundColor(Color.TRANSPARENT)
                if (Build.VERSION.SDK_INT >= 11) view?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBarTnC.visibility = View.GONE
                view?.setBackgroundColor(Color.TRANSPARENT)
                if (Build.VERSION.SDK_INT >= 11) view?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (Build.VERSION.SDK_INT >= 11) view?.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
                view!!.loadUrl(url!!)
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        tnc_webview.settings.javaScriptEnabled = true
        tnc_webview.settings.setSupportZoom(true)
        tnc_webview.settings.builtInZoomControls = true
        tnc_webview.settings.useWideViewPort = true
        tnc_webview.settings.loadWithOverviewMode = false
        tnc_webview.setBackgroundColor(Color.TRANSPARENT)
        tnc_webview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)

        if (!Utility.hasConnection(this)){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    tnc_webview.loadUrl(tnc_url)
                }

            })
            noInternetDialog.show(supportFragmentManager, "Terms And Conditions Activity")
        }else{
            tnc_webview.loadUrl(tnc_url)
        }
    }
}
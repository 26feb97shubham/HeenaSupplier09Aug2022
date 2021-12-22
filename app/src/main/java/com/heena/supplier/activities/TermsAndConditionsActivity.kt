package com.heena.supplier.activities

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.webkit.WebView
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.extras.MyWebViewClient
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import kotlinx.android.synthetic.main.activity_terms_and_conditions.*

class TermsAndConditionsActivity : AppCompatActivity() {
    private var tnc_url = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)
        setUpViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpViews() {
        ic_arrow_back.setOnClickListener {
            ic_arrow_back.startAnimation(AlphaAnimation(1f, 0.5f))
            onBackPressed()
        }

        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "").equals("en")){
            tnc_url = "https://henna.devtechnosys.info/terms-and-conditions/en"
        }else{
            tnc_url = "https://henna.devtechnosys.info/terms-and-conditions/ar"
        }

        tnc_webview.webViewClient = MyWebViewClient()
        tnc_webview.settings.javaScriptEnabled = true
        tnc_webview.settings.setSupportZoom(true)
        tnc_webview.getSettings().setBuiltInZoomControls(true)
        tnc_webview.getSettings().setUseWideViewPort(true)
        tnc_webview.getSettings().setLoadWithOverviewMode(false)
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
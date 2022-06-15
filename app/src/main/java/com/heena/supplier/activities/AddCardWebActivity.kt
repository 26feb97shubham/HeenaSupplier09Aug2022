package com.heena.supplier.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.heena.supplier.R
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.addCardURL
import kotlinx.android.synthetic.main.activity_add_card_web.*
import kotlinx.android.synthetic.main.activity_tap_payment.*
import kotlinx.android.synthetic.main.activity_tap_payment.tapWebView

class AddCardWebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card_web)
        setUpView()
    }

    private fun setUpView() {
        addCardActivityWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                super.onProgressChanged(view, progress)
                addCardActivityProgressbar.visibility= View.VISIBLE
                if(progress>=90){
                    addCardActivityProgressbar.visibility= View.GONE
                }

            }
        }
        addCardActivityWebView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView, url: String?
            ): Boolean {
                addCardActivityProgressbar.visibility= View.VISIBLE
                return if(!url.equals("")){
                    if(url!!.contains("success")){
                        finish()
                    }
                    true
                }else{
                    true
                }
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                addCardActivityProgressbar.visibility= View.GONE
            }
        }
        addCardActivityWebView.settings.javaScriptEnabled=true
        addCardActivityWebView.settings.allowContentAccess=true
        addCardActivityWebView.settings.loadWithOverviewMode=true
        addCardActivityWebView.settings.useWideViewPort=true
        addCardActivityWebView.settings.loadsImagesAutomatically=true
        addCardActivityWebView.loadUrl(addCardURL+"${SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]}")
    }
}
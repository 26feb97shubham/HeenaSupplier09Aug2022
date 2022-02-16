package com.heena.supplier.application

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.heena.supplier.R
import com.heena.supplier.utils.LogUtils
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import okhttp3.OkHttpClient
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

class MyApp : Application() {
    companion object {
        var socket: Socket? = null
        var instance: MyApp? = null
        var SOCKET_URL: String = "https://henna.devtechnosys.info:17303"
    }
    public override fun attachBaseContext(base: Context) {
        instance = this

        MultiDex.install(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.attachBaseContext(base)

    }
    @Synchronized
    fun getInstance(): MyApp? {
        return instance
    }
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Places.initialize(this, getString(R.string.places_api_key))
         AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

         try {
             val mySSLContext = SSLContext.getInstance("TLS")
             val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                 override fun checkClientTrusted(
                     p0: Array<out java.security.cert.X509Certificate>?,
                     p1: String?
                 ) {
                     Log.e("message", p1.toString())
                 }

                 override fun checkServerTrusted(
                     p0: Array<out java.security.cert.X509Certificate>?,
                     p1: String?
                 ) {
                     Log.e("message", p1.toString())
                 }

                 override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                     return arrayOf()
                 }

             })
             mySSLContext.init(null, trustAllCerts, null)
             val myHostnameVerifier = HostnameVerifier { hostname, session -> true }
             val okHttpClient = OkHttpClient.Builder()
                 .hostnameVerifier(myHostnameVerifier)
                 .sslSocketFactory(mySSLContext.socketFactory, object : X509TrustManager {
                     override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                         return arrayOf()
                     }

                     @Throws(CertificateException::class)
                     override fun checkClientTrusted(
                         p0: Array<out java.security.cert.X509Certificate>?,
                         authType: String?
                     ) {
                         Log.e("message", authType.toString())
                     }

                     @Throws(CertificateException::class)
                     override fun checkServerTrusted(
                         p0: Array<out java.security.cert.X509Certificate>?,
                         authType: String?
                     ) {
                         Log.e("message", authType.toString())
                     }
                 })
                 .build()
             // default settings for all sockets
             IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
             IO.setDefaultOkHttpCallFactory(okHttpClient)
             // set as an option
             val opts = IO.Options()
             opts.callFactory = okHttpClient
             opts.webSocketFactory = okHttpClient
             opts.forceNew = true
             //  socket = IO.socket(ChatConstant.CHAT_SERVER_URL, opts);
             socket = IO.socket(SOCKET_URL, opts)
              socket!!.io().open(Manager.OpenCallback { e ->
                  if (e != null) {
                      LogUtils.e("call", "call: " + e.message)
                  }
              })
         } catch (e: Exception) {
             throw RuntimeException(e)
         }
    }
}
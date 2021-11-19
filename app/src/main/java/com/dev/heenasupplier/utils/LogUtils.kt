package com.dev.heenasupplier.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.viewbinding.BuildConfig


class LogUtils {
    companion object{
        fun d(key: String?, message: String?) {
            if (BuildConfig.DEBUG) {
                if (message != null) {
                    Log.d(key, message)
                }
            }
        }

        fun e(key: String?, message: String?) {
            if (BuildConfig.DEBUG) {
                if (message != null) {
                    Log.e(key, message)
                }
            }
        }

        fun shortToast(context: Context?, message: String?) {
            if (context != null) {
                val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                toast.show()
            }
        }

        fun longToast(context: Context?, message: String?) {
            if (context != null) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }

    }
}
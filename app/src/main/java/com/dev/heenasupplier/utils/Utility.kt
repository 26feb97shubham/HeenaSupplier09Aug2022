package com.dev.heenasupplier.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.dev.heenasupplier.application.MyApp
import com.dev.heenasupplier.models.CategoryItem
import com.dev.heenasupplier.models.CategoryListResponse
import com.dev.heenasupplier.models.Service
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Utility {
    companion object{
        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        var serviceslisting = ArrayList<Service>()
        val IMAGE_DIRECTORY_NAME = "Heena_Supplier"

        fun changeLanguage(context: Context, language:String){
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            context.resources
                .updateConfiguration(config, context.resources.displayMetrics)
        }

        fun setDefaultLanguage(context: Context, language: String){
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            context.resources
                    .updateConfiguration(config, context.resources.displayMetrics)
        }

        fun getLanguage() : String{
            return SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,"")
        }

        fun isNetworkAvailable(): Boolean {
            val connectivityManager = MyApp.instance!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        fun setLanguage(context: Context, language: String){
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            context.resources
                    .updateConfiguration(config, context.resources.displayMetrics)
        }

        fun returnCategory(selectedCategory: String, categoryList: ArrayList<CategoryItem>): Int? {
            for (category : CategoryItem in categoryList) {
                if (category.name.equals(selectedCategory)) {
                    return category.category_id
                }
            }
            return null
        }

        fun hasConnection(ct: Context): Boolean {
            val cm = (ct.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            @SuppressLint("MissingPermission") val wifiNetwork =
                    cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (wifiNetwork != null && wifiNetwork.isConnected) {
                return true
            }
            val mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (mobileNetwork != null && mobileNetwork.isConnected) {
                return true
            }
            val activeNetwork = cm.activeNetworkInfo
            return if (activeNetwork != null && activeNetwork.isConnected) {
                true
            } else false
        }

        fun isCharacterAllowed(validateString: String): Boolean {
            var containsInvalidChar = false
            for (i in 0 until validateString.length) {
                val type = Character.getType(validateString[i])
                containsInvalidChar = !(type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt())
            }
            return containsInvalidChar
        }
    }



}
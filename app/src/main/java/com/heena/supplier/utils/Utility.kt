package com.heena.supplier.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.provider.Settings
import android.util.Log
import com.heena.supplier.application.MyApp
import com.heena.supplier.broadcastreceiver.ConnectivityReceiver
import com.heena.supplier.models.CategoryItem
import com.heena.supplier.models.Service
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import kotlin.collections.ArrayList

class Utility {
    companion object{
        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        var serviceslisting = ArrayList<Service>()
        val IMAGE_DIRECTORY_NAME = "Heena_Supplier"
        var booking_item_type = 0
        var message_type = 0
        var networkChangeReceiver: ConnectivityReceiver? = null


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
            return try {
                val connectivityManager = MyApp.instance!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
            }catch (e: NullPointerException){
                showLog(e.localizedMessage)
                false
            }
        }

        private fun showLog(localizedMessage: String?) {
            Log.e("NetworkChangeReceiver", "" + localizedMessage)
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

        fun getFCMToken() {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("getInstanceId", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    val fcmToken = task.result
                    Log.e("getInstanceId", fcmToken)
                    SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.FCMTOKEN,fcmToken.toString())

                })

        }
        fun deviceId(context: Context?){
            val deviceId = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
            Log.e("deviceId", deviceId)
            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.DeviceId,deviceId.toString())
        }
    }



}
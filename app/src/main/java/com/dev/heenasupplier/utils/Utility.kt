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
        private var progressBar: ProgressBar? = null
        var categoryList = ArrayList<CategoryItem>()
        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        var categoryNames = ArrayList<String>()
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

        fun Context.showErrorToast(message: String?) {

            try {
                val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)

                // set message color
                val textView = toast.view!!.findViewById(android.R.id.message) as? TextView
                textView?.setTextColor(Color.WHITE)
                textView?.gravity = Gravity.CENTER

                // set background color
                toast.view!!.setBackgroundColor(Color.RED)

                toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)

                toast.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        // show progressbar
        fun Context.showProgressBar() {
            try {
                val layout = (this as? Activity)?.findViewById<View>(android.R.id.content)?.rootView as? ViewGroup

                progressBar = ProgressBar(this, null, android.R.style.Widget_DeviceDefault_ProgressBar_Small)
                progressBar?.let { it1 ->
                    it1.isIndeterminate = true

                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )

                    val rl = RelativeLayout(this)

                    rl.gravity = Gravity.CENTER
                    rl.addView(it1)

                    layout?.addView(rl, params)

                    it1.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // hide progressbar
        fun hideProgressBar() {
            try {
                progressBar?.let {
                    it.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //set flags for windows

        fun getCategories() : ArrayList<CategoryItem>{
            val call = apiInterface.categoryList()
            call!!.enqueue(object : Callback<CategoryListResponse?> {
                override fun onResponse(
                        call: Call<CategoryListResponse?>,
                        response: Response<CategoryListResponse?>
                ) {
                    try {
                        if (response.isSuccessful) {
                            if (response.body()!!.status == 1) {
                                categoryList = response.body()!!.category as ArrayList<CategoryItem>
                               /* for (i in 0 until categoryList.size) {
                                    categoryNames.add(categoryList.get(i).name.toString())
                                }*/
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<CategoryListResponse?>, t: Throwable) {
                }
            })
            return categoryList
        }

        fun isDateAfter(startDate : String, endDate : String) : Boolean{
            try {
                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                val endingDate = sdf.parse(endDate)
                val startingDate = sdf.parse(startDate)
                if (endingDate?.after(startingDate)!!){
                    return true
                }else{
                    return false
                }
            }catch (e : Exception){
                return false
            }
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
    }



}
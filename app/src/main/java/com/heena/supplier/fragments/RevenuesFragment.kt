package com.heena.supplier.fragments

import android.Manifest
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.RevenuesAdapter
import com.heena.supplier.adapters.ServiceListingAdapter
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_revenues.view.*
import kotlinx.android.synthetic.main.fragment_revenues.view.cards_service_categories_listing
import kotlinx.android.synthetic.main.fragment_revenues.view.rv_services_listing
import kotlinx.android.synthetic.main.row_chat.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RevenuesFragment : Fragment() {
    lateinit var revenuesAdapter: RevenuesAdapter
    private var mView : View?=null
    private var transactionsList = ArrayList<TransactionItem>()
    private var queryMap = HashMap<String, String>()
    private var isFilterClicked = false
    private var isServicesClicked = false

    lateinit var serviceListingAdapter: ServiceListingAdapter
    private var myStartDate : String = ""
    private var myEndDate : String = ""
    var selected_category : String = ""
    var service_id :String = ""
    var search_keyword :String = ""
    var open_calendar_status = false
    var serviceList = ArrayList<Service>()
    var serviceNames = ArrayList<String>()
    var offer_duration : String = ""
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    val myFormat = "yyyy-MM-dd"
    val sdf = SimpleDateFormat(myFormat, Locale.US)

    private val PERMISSIONS_1 =  arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val myCalendar: Calendar = Calendar.getInstance()

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            var allAreGranted = true
            for(b in it.values) {
                allAreGranted = allAreGranted && b
            }
            if(allAreGranted) {
                Log.e("Granted", "Permissions")
                queryMap.put("lang", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
                queryMap.put("service_id", "")
                queryMap.put("from", "")
                queryMap.put("to", "")
                queryMap.put("search", "")
                getTransactionURL(queryMap)

            }else{
                LogUtils.shortToast(requireContext(), getString(R.string.please_allow_permissions))
                Log.e("Denied", "Permissions")
            }
        }

    private fun getTransactionURL(queryMap: HashMap<String, String>) {
        mView!!.frag_revenues_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.getTransactionURL(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0), queryMap)
        call?.enqueue(object : Callback<TransactionURLResponse?>{
            override fun onResponse(call: Call<TransactionURLResponse?>, response: Response<TransactionURLResponse?>) {
                mView!!.frag_revenues_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        val url = response.body()!!.url
                        if (url != null) {
                            downloadRevenueExcel(url)
                        }
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    mView!!.rv_revenues_listing.visibility = View.GONE
                    mView!!.ll_no_transactions_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<TransactionURLResponse?>, t: Throwable) {
                mView!!.frag_revenues_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                mView!!.rv_revenues_listing.visibility = View.GONE
                mView!!.ll_no_transactions_found.visibility = View.VISIBLE
                LogUtils.shortToast(requireContext(), t.message)
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(
                R.layout.fragment_revenues, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    queryMap["lang"] = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
                    queryMap["service_id"] = ""
                    queryMap["from"] = ""
                    queryMap["to"] = ""
                    queryMap["search"] = ""
                    getTransactionsList(queryMap)
                    getServices()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Revenues Fragment")
        }else{
            queryMap["lang"] =
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
            queryMap["service_id"] = ""
            queryMap["from"] = ""
            queryMap["to"] = ""
            queryMap["search"] = ""
            getTransactionsList(queryMap)
            getServices()
        }

        mView!!.civ_excel_doc.setSafeOnClickListener {
            activityResultLauncher.launch(PERMISSIONS_1)
        }




        mView!!.et_search.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent : KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    search_keyword = mView!!.et_search.text.toString()
                    SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(),  mView!!.et_search)
                    if (TextUtils.isEmpty(search_keyword)){
                        LogUtils.shortToast(requireContext(), getString(R.string.please_enter_search_keyword_for_searching))
                    }else{
                        queryMap["lang"] = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
                        queryMap["service_id"] = ""
                        queryMap["from"] = ""
                        queryMap["to"] = ""
                        queryMap["search"] = search_keyword
                        getTransactionsList(queryMap)
                    }
                    return true
                }
                return false
            }
        })

        mView!!.civ_filter.setSafeOnClickListener {
            if (!isFilterClicked){
                isFilterClicked = true
                mView!!.card_filter.visibility = View.VISIBLE
            }else{
                isFilterClicked = false
                mView!!.card_filter.visibility = View.GONE
            }
        }

        mView!!.card_services.setSafeOnClickListener {
            if (!isServicesClicked){
                isServicesClicked = true
                mView!!.cards_service_categories_listing.visibility = View.VISIBLE
            }else{
                isServicesClicked = false
                mView!!.cards_service_categories_listing.visibility = View.GONE
            }
        }

        mView!!.card_duration.setSafeOnClickListener {
            if (!open_calendar_status){
                open_calendar_status = true
                mView!!.cv_Calendar_Revenue.visibility = View.VISIBLE
            }else{
                open_calendar_status = false
                mView!!.cv_Calendar_Revenue.visibility = View.GONE
            }
        }

        mView!!.cdrvCalendarRevenue.setCurrentMonth(myCalendar)

        mView!!.cdrvCalendarRevenue.setCalendarListener(object : CalendarListener {
            override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                myStartDate =  sdf.format(startDate.time)
                myEndDate =  sdf.format(endDate.time)
                offer_duration = sdf.format(startDate.time) + " - " + sdf.format(endDate.time)
                Log.e("offer_duration", offer_duration)
                mView!!.cv_Calendar_Revenue.visibility = View.GONE
                mView!!.ll_from_to_service_date.visibility = View.VISIBLE
                mView!!.tv_from.text = myStartDate
                mView!!.tv_to.text = myEndDate
                mView!!.tv_card_duration.text = offer_duration
            }

            override fun onFirstDateSelected(startDate: Calendar) {
            }

        })

        mView!!.tv_apply_filter.setSafeOnClickListener {
            validateAndProceed()
        }


    }

    private fun validateAndProceed() {
        queryMap.put("lang", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        queryMap.put("service_id", service_id.toString())
        queryMap.put("from", myStartDate)
        queryMap.put("to", myEndDate)
        queryMap.put("search", "")
        mView!!.card_filter.visibility = View.GONE
        getTransactionsList(queryMap)
    }

    private fun getServices() {
        val call = apiInterface.serviceslisting(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0),
        SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,""))
        call?.enqueue(object : Callback<ServiceListingResponse?>{
            override fun onResponse(call: Call<ServiceListingResponse?>, response: Response<ServiceListingResponse?>) {
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            serviceList.clear()
                            serviceNames.clear()
                            serviceList = response.body()!!.service as ArrayList<Service>
                            for (i in 0 until serviceList.size) {
                                serviceList.get(i).name?.let { serviceNames.add(it) }
                            }
                            mView!!.rv_services_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            serviceListingAdapter = ServiceListingAdapter(requireContext(), serviceNames, object : ClickInterface.OnServiceClick {
                                override fun OnAddService(position: Int, service : String) {
                                    isServicesClicked = false
                                    mView!!.tv_service.text = service
                                    selected_category = mView!!.tv_service.text.toString().trim()
                                    mView!!.cards_service_categories_listing.visibility = View.GONE
                                    service_id = returnServiceId(selected_category,serviceList).toString()
                                    Log.e("service_id", service_id.toString())
                                }
                            })
                            mView!!.rv_services_listing.adapter = serviceListingAdapter
                            serviceListingAdapter.notifyDataSetChanged()
                        } else {
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    } else {
                        LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ServiceListingResponse?>, t: Throwable) {
                LogUtils.shortToast(requireContext(), t.message)
            }

        })
    }

    fun returnServiceId(selectedCategory: String, serviceList: ArrayList<Service>): Int? {
        for (service : Service in serviceList) {
            if (service.name.equals(selectedCategory)) {
                return service.service_id
            }
        }
        return null
    }

    private fun getTransactionsList(queryMap: HashMap<String, String>) {
        mView!!.frag_revenues_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.getTransactionsList(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0), queryMap)
        call?.enqueue(object : Callback<TransactionsListingResponse?>{
            override fun onResponse(call: Call<TransactionsListingResponse?>, response: Response<TransactionsListingResponse?>) {
                mView!!.frag_revenues_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        transactionsList.clear()
                        transactionsList = response.body()!!.transaction as ArrayList<TransactionItem>
                        if (transactionsList.size==0){
                            mView!!.card_search_filter.visibility = View.GONE
                            mView!!.rv_revenues_listing.visibility = View.GONE
                            mView!!.ll_no_transactions_found.visibility = View.VISIBLE
                        }else{
                            mView!!.card_search_filter.visibility = View.VISIBLE
                            mView!!.rv_revenues_listing.visibility = View.VISIBLE
                            mView!!.ll_no_transactions_found.visibility = View.GONE
                        }
                        val balance = "AED " + Utility.convertDoubleValueWithCommaSeparator(response.body()!!.balance!!.toDouble())
                        mView!!.tv_revenues.text = balance
                        setTransactionsAdapter(transactionsList)
                    }else{
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                        mView!!.rv_revenues_listing.visibility = View.GONE
                        mView!!.ll_no_transactions_found.visibility = View.VISIBLE
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    mView!!.rv_revenues_listing.visibility = View.GONE
                    mView!!.ll_no_transactions_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<TransactionsListingResponse?>, t: Throwable) {
                mView!!.frag_revenues_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                mView!!.rv_revenues_listing.visibility = View.GONE
                mView!!.ll_no_transactions_found.visibility = View.VISIBLE
                LogUtils.shortToast(requireContext(), t.message)
            }

        })
    }

    private fun setTransactionsAdapter(transactionsList: ArrayList<TransactionItem>) {
        mView!!.rv_revenues_listing.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)
        revenuesAdapter = RevenuesAdapter(requireContext(), transactionsList)
        mView!!.rv_revenues_listing.adapter = revenuesAdapter
        revenuesAdapter.notifyDataSetChanged()
        mView!!.rv_revenues_listing.setHasFixedSize(true)
    }


    private fun downloadRevenueExcel(/*progressDialog: ProgressDialog, */excelDownloadUrl: String) {
        if (Utility.isNetworkAvailable()) {
            val dir: File =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "alniqasha")
            if (dir == null || !dir.exists())
                dir.mkdir()
            val excelFileName = dir.toString() + "/DOC_" + System.currentTimeMillis() + ".xlsx"
            val excelFile = File(excelFileName)
            if (!excelFile.exists()) {
                try {
                    excelFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                    return
                }
            }
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                try {
                    val url = URL(excelDownloadUrl)
                    val c = url.openConnection() as HttpURLConnection
                    c.requestMethod = "GET"
                    c.connect()
                    //If Connection response is not OK then show Logs
                    if (c.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.e(
                            "Server returned HTTP ",
                            c.responseCode.toString() + " " + c.responseMessage
                        )
                        //progressDialog.dismiss()
                    }
                    val fos: FileOutputStream =
                        FileOutputStream(excelFile) //Get OutputStream for NewFile Location
                    val `is` = c.inputStream //Get InputStream for connection
                    val buffer = ByteArray(1024) //Set buffer type
                    var len1 = 0 //init length
                    while (`is`.read(buffer).also { len1 = it } > 0) {
                        fos.write(buffer, 0, len1) //Write new file
                    }
                    fos.close()
                    `is`.close()
                    requireActivity().runOnUiThread {
                        if (excelFile != null) {
                            //progressDialog.dismiss()
                            LogUtils.shortToast(requireContext(), excelFile.absolutePath)
                            Log.e("er", excelFile.absolutePath)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        } else {
            LogUtils.shortToast(context, getString(R.string.check_internet))
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
    }
}
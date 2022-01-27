package com.heena.supplier.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.bumptech.glide.Glide
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.ServiceListingAdapter
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_add_offers.*
import kotlinx.android.synthetic.main.fragment_add_offers.view.*
import kotlinx.android.synthetic.main.fragment_add_offers.view.tv_save_service
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.heena.supplier.extras.InputFilterMinMax

import android.text.InputFilter
import com.heena.supplier.extras.DecimalDigitsInputFilter
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener


class AddOffers : Fragment() {
    private var mView : View ?= null
    lateinit var serviceListingAdapter: ServiceListingAdapter
    private var myStartDate : String = ""
    private var myEndDate : String = ""
    private var selectedService =""
    private var offerPrice = ""
    private var offerChildPrice = ""
    var selected_category : String = ""
    var offer_discount : String = ""
    var service_id : Int?=null
    var open_calendar_status = false
    var serviceList = ArrayList<Service>()
    var serviceNames = ArrayList<String>()
    var offer_duration : String = ""
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    val myFormat = "yyyy-MM-dd"
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    var service_price = ""

    val myCalendar: Calendar = Calendar.getInstance()

    var offer_id = 0
    var offer_status = ""

    private var services_clicked = false
    var pos : Int?=null

    var offer_Price = 0.0
    var service_Price = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offer_status = it.getString("status").toString()
            offer_id = it.getInt("offer_id")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_add_offers, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        getServices()
        setUpViews()
        return mView
    }

    private fun setUpViews() {
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

        if (offer_status.equals("edit")){
            mView!!.tv_add_new_offers_txt.text = getString(R.string.update_offers)
            mView!!.tv_save_service.text = getString(R.string.update)
            showOffer(offer_id)
        }else{
            mView!!.tv_add_new_offers_txt.text = getString(R.string.add_new_offers)
            mView!!.tv_save_service.text = getString(R.string.save)
        }

        mView!!.card_choose_services.setSafeOnClickListener {
            if (!services_clicked){
                services_clicked = true
                mView!!.cards_service_categories_listing.visibility = View.VISIBLE
            }else{
                services_clicked = false
                mView!!.cards_service_categories_listing.visibility = View.GONE
            }
        }

        mView!!.ll_card_duration.setSafeOnClickListener {
            if (!open_calendar_status){
                open_calendar_status = true
                mView!!.cv_Calendar.visibility = View.VISIBLE
            }else{
                open_calendar_status = false
                mView!!.cv_Calendar.visibility = View.GONE
            }
        }

        val startMonth = myCalendar
        val endMonth = startMonth.clone() as Calendar
        endMonth.add(Calendar.MONTH, 12)
        Log.d(
            "TAG",
            "Start month: " + startMonth.time.toString() + " :: End month: " + endMonth.time.toString()
        )
        mView!!.cdrvCalendar.setVisibleMonthRange(startMonth, endMonth)

        val startDateSelectable = startMonth.clone() as Calendar
        startDateSelectable.add(Calendar.DATE, 0)
        val endDateSelectable = endMonth.clone() as Calendar
        endDateSelectable.add(Calendar.DATE, -20)
        Log.d(
            "TAG",
            "startDateSelectable: " + startDateSelectable.time.toString() + " :: endDateSelectable: " + endDateSelectable.time.toString()
        )
        mView!!.cdrvCalendar.setSelectableDateRange(startDateSelectable, endDateSelectable)


        mView!!.cdrvCalendar.setCalendarListener(object : CalendarListener {
            override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                myStartDate = sdf.format(startDate.time)
                myEndDate = sdf.format(endDate.time)
                offer_duration = sdf.format(startDate.time) + " - " + sdf.format(endDate.time)
                Log.e("offer_duration", offer_duration)
                mView!!.et_card_duration.setText(offer_duration)
            }

            override fun onFirstDateSelected(startDate: Calendar) {
                val date = sdf.format(System.currentTimeMillis())
                if (startDate.before(date)) {
                    LogUtils.shortToast(requireContext(), "Date cannot be selected")
                }
            }

        })

        mView!!.tv_save_service.setSafeOnClickListener {
            validateAndSave()
        }

        mView!!.img.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putStringArrayList("gallery",
                    serviceList[pos!!].gallery as ArrayList<String>?
            )
            findNavController().navigate(R.id.viewImageFragment, bundle)
        }


        mView!!.et_offers_price.doOnTextChanged { text, start, before, count ->
            if(mView!!.et_discount.text!!.toString().trim().equals("")){
                LogUtils.shortToast(requireContext(), requireContext().getString(R.string.please_enter_valid_offer_total_price))
            }else{
                if(count==0){
                    mView!!.et_offers_price.requestFocus()
                    mView!!.et_offers_price.error = requireContext().getString(R.string.please_enter_valid_offer_total_price)
                }else{
                    if(text.toString().length==1 && text.toString().contains("0")) {
                        val my_price = text.toString().substring(0, text.toString().length -1)
                        val myprice = my_price.toDouble()
                        if (service_price.equals("")) {
                            mView!!.tv_choose_service.requestFocus()
                            mView!!.tv_choose_service.error =
                                getString(R.string.no_service_selected)
                        } else {
                            if (service_price.toDouble() <= myprice) {
                                mView!!.et_offers_price.requestFocus()
                                mView!!.et_offers_price.error =
                                    requireContext().getString(R.string.please_enter_valid_offer_total_price)
                            } else {
                                val my_disc = (mView!!.et_discount.text.toString()).toDouble()
                                val my_offer_price = (myprice * my_disc) / 100
                                mView!!.et_offers_child_price.setText(my_offer_price.toString())
                            }
                        }
                    }else{
                        val my_price = text.toString().toDouble()
                        if (service_price.equals("")) {
                            mView!!.tv_choose_service.requestFocus()
                            mView!!.tv_choose_service.error =
                                getString(R.string.no_service_selected)
                        } else {
                            if (service_price.toDouble() <= my_price) {
                                mView!!.et_offers_price.requestFocus()
                                mView!!.et_offers_price.error =
                                    requireContext().getString(R.string.please_enter_valid_offer_total_price)
                            } else {
                                val my_disc = (mView!!.et_discount.text.toString()).toDouble()
                                val my_offer_price = (my_price * my_disc) / 100
                                mView!!.et_offers_child_price.setText(my_offer_price.toString())
                            }
                        }
                    }
                }
            }
        }


        mView!!.et_discount.setFilters(arrayOf<InputFilter>(InputFilterMinMax(0, 99)))
        mView!!.et_offers_price.setFilters(arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2)))
    }

    private fun showOffer(offerId: Int) {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mView!!.frag_add_offer_progressBar.visibility= View.VISIBLE
        val call = apiInterface.showOffer(offerId)
        call?.enqueue(object : Callback<ShowOfferResponse?> {
            override fun onResponse(
                call: Call<ShowOfferResponse?>,
                response: Response<ShowOfferResponse?>
            ) {
                mView!!.frag_add_offer_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if (response.body()!!.status == 1) {
                                LogUtils.shortToast(requireContext(), response.body()!!.message)
                                val offerItem = response.body()!!.offer
                                mView!!.tv_choose_service.text = offerItem!!.service!!.name
                                mView!!.et_discount.setText(offerItem.percentage)
                                mView!!.et_offers_price.setText(offerItem.price)
                                mView!!.et_offers_child_price.setText(offerItem.offer_price)
                                val offer_dur = offerItem.started_at + " - " + offerItem.ended_at
                                mView!!.et_card_duration.text = offer_dur
                            } else {
                                LogUtils.longToast(requireContext(), response.body()!!.message)
                            }
                        }
                    } else {
                        LogUtils.shortToast(
                            requireContext(),
                            getString(R.string.response_isnt_successful)
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ShowOfferResponse?>, t: Throwable) {
                mView!!.frag_add_offer_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun getServices() {
        val call = apiInterface.serviceslisting(
            SharedPreferenceUtility.getInstance().get(
                SharedPreferenceUtility.UserId,
                0
            ),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        call?.enqueue(object : Callback<ServiceListingResponse?> {
            override fun onResponse(
                call: Call<ServiceListingResponse?>,
                response: Response<ServiceListingResponse?>
            ) {
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            mView!!.card_choose_services.isClickable = true
                            serviceList.clear()
                            serviceList = response.body()!!.service as ArrayList<Service>
                            serviceNames.clear()
                            for (i in 0 until serviceList.size) {
                                serviceList.get(i).name?.let { serviceNames.add(it) }
                            }
                            mView!!.rv_services_listing.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                            serviceListingAdapter = ServiceListingAdapter(
                                requireContext(),
                                serviceNames,
                                object : ClickInterface.OnServiceClick {
                                    override fun OnAddService(position: Int, service: String) {
                                        mView!!.tv_choose_service.text = service
                                        selected_category = tv_choose_service.text.toString().trim()
                                        mView!!.cards_service_categories_listing.visibility =
                                            View.GONE
                                        services_clicked = false
                                        service_id = returnServiceId(selected_category, serviceList)
                                        Log.e("service_id", service_id.toString())
                                        service_price = serviceList[position].price!!.total.toString()
                                        mView!!.serviceCard.visibility = View.VISIBLE
                                        pos = position
                                        if(serviceList[position].gallery!!.size==0){
                                            mView!!.img.setImageResource(R.drawable.user)
                                        }else{
                                            Glide.with(requireContext())
                                                    .load(serviceList[position].gallery!![0]).into(
                                                            mView!!.img
                                                    )
                                        }

                                        mView!!.tv_services.text = serviceList[position].name
                                        mView!!.tv_price.text = "AED "+serviceList[position].price!!.total
                                        mView!!.tv_category_name.text =
                                            serviceList[position].category!!.name
                                    }
                                })
                            mView!!.rv_services_listing.adapter = serviceListingAdapter
                            serviceListingAdapter.notifyDataSetChanged()
                        } else {
                            mView!!.cards_service_categories_listing.visibility = View.GONE
                            mView!!.card_choose_services.isClickable = false
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    } else {
                        mView!!.cards_service_categories_listing.visibility = View.GONE
                        LogUtils.shortToast(
                            requireContext(),
                            getString(R.string.response_isnt_successful)
                        )
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
                mView!!.cards_service_categories_listing.visibility = View.GONE
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


    private fun validateAndSave() {
        selectedService = mView!!.tv_choose_service.text.toString().trim()
        offerPrice = mView!!.et_offers_price.text.toString().trim()
        offerChildPrice = mView!!.et_offers_child_price.text.toString().trim()
        offer_discount = mView!!.et_discount.text.toString().trim()

        if (TextUtils.isEmpty(selectedService)){
            mView!!.tv_choose_service.requestFocus()
            mView!!.tv_choose_service.error = getString(R.string.no_service_selected)
        }else if(TextUtils.isEmpty(offer_discount)){
            mView!!.et_discount.requestFocus()
            mView!!.et_discount.error = getString(R.string.please_enter_valid_offer_discount)
        } else if(TextUtils.isEmpty(offerPrice)){
            mView!!.et_offers_price.requestFocus()
            mView!!.et_offers_price.error = getString(R.string.please_enter_valid_offer_price)
        } else if(TextUtils.isEmpty(offerChildPrice)){
            mView!!.et_offers_child_price.requestFocus()
            mView!!.et_offers_child_price.error = getString(R.string.please_enter_valid_offer_price)
        }else if(TextUtils.isEmpty(offer_duration)){
            mView!!.et_card_duration.requestFocus()
            mView!!.et_card_duration.error = getString(R.string.please_select_a_valid_duration)
        } else{
            if(offer_status.equals("edit")){
                offer_Price = offerPrice.toDouble()
                service_Price = service_price.toDouble()
                update()
            }else{
                offer_Price = offerPrice.toDouble()
                service_Price = service_price.toDouble()
                save()
            }
        }
    }

    private fun update(){
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mView!!.frag_add_offer_progressBar.visibility= View.VISIBLE

        val builder = APIClient.createBuilder(
            arrayOf(
                "user_id",
                "service_id",
                "started_at",
                "ended_at",
                "price",
                "offer_price",
                "percentage",
                "offer_id"
            ),
            arrayOf(
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0)
                    .toString(),
                service_id.toString(),
                myStartDate,
                myEndDate,
                offerPrice,
                offerChildPrice,
                offer_discount,
                offer_id.toString()
            )
        )
        val call = apiInterface.updateOffers(builder.build())
        call!!.enqueue(object : Callback<OfferUpdateResponse?> {
            override fun onResponse(
                call: Call<OfferUpdateResponse?>,
                response: Response<OfferUpdateResponse?>
            ) {
                frag_add_offer_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if (response.body()!!.status == 1) {
                                LogUtils.shortToast(requireContext(), response.body()!!.message)
                                findNavController().popBackStack()
                            } else {
                                LogUtils.longToast(requireContext(), response.body()!!.message)
                            }
                        }
                    } else {
                        LogUtils.shortToast(
                            requireContext(),
                            getString(R.string.response_isnt_successful)
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<OfferUpdateResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.localizedMessage)
                mView!!.frag_add_offer_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun save() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mView!!.frag_add_offer_progressBar.visibility= View.VISIBLE
        val builder = APIClient.createBuilder(
            arrayOf(
                "user_id",
                "service_id",
                "started_at",
                "ended_at",
                "price",
                "offer_price",
                "percentage"
            ),
            arrayOf(
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0)
                    .toString(),
                service_id.toString(),
                myStartDate,
                myEndDate,
                offerPrice,
                offerChildPrice,
                offer_discount
            )
        )
        val call = apiInterface.addOffers(builder.build())
        call!!.enqueue(object : Callback<AddOfferResponse?> {
            override fun onResponse(
                call: Call<AddOfferResponse?>,
                response: Response<AddOfferResponse?>
            ) {
                frag_add_offer_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if (response.body()!!.status == 1) {
                                LogUtils.shortToast(requireContext(), response.body()!!.message)
                                findNavController().popBackStack()
                            } else {
                                LogUtils.longToast(requireContext(), response.body()!!.message)
                            }
                        }
                    } else {
                        LogUtils.shortToast(
                            requireContext(),
                            getString(R.string.response_isnt_successful)
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<AddOfferResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.localizedMessage)
                mView!!.frag_add_offer_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
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

    override fun onResume() {
        super.onResume()
        mView!!.cards_service_categories_listing.visibility = View.GONE
    }

}
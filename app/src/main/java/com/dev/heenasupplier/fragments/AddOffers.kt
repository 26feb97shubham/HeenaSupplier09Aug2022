package com.dev.heenasupplier.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.bumptech.glide.Glide
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.ServiceListingAdapter
import com.dev.heenasupplier.models.*
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.fragment_add_new_service.*
import kotlinx.android.synthetic.main.fragment_add_new_service.view.*
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

class AddOffers : Fragment() {
    private var mView : View ?= null
    lateinit var serviceListingAdapter: ServiceListingAdapter
    private var myStartDate : String = ""
    private var myEndDate : String = ""
    private var selectedService =""
    private var offerPrice = ""
    private var offerChildPrice = ""
    var selected_category : String = ""
    var service_id : Int?=null
    var open_calendar_status = false
    var serviceList = ArrayList<Service>()
    var serviceNames = ArrayList<String>()
    var offer_duration : String = ""
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    val myFormat = "yyyy-MM-dd"
    val sdf = SimpleDateFormat(myFormat, Locale.US)

    val myCalendar: Calendar = Calendar.getInstance()

    var offer_id = 0
    var offer_status = ""

    private var services_clicked = false
    var pos : Int?=null

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
        getServices()
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(
                requireContext(),
                requireActivity().iv_back
            )
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setOnClickListener {
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

        mView!!.card_choose_services.setOnClickListener {
            if (!services_clicked){
                services_clicked = true
                mView!!.cards_service_categories_listing.visibility = View.VISIBLE
            }else{
                services_clicked = false
                mView!!.cards_service_categories_listing.visibility = View.GONE
            }
        }

        mView!!.ll_card_duration.setOnClickListener {
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

        mView!!.tv_save_service.setOnClickListener {
            validateAndSave()
        }

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1f, 0.5f))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(
                requireContext(),
                requireActivity().iv_back
            )
            findNavController().popBackStack()
        }

        mView!!.img.setOnClickListener {
            val bundle = Bundle()
            bundle.putStringArrayList("gallery",
                    serviceList[pos!!].gallery as ArrayList<String>?
            )
            findNavController().navigate(R.id.viewImageFragment, bundle)
        }
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
                                mView!!.et_offers_price.setText(offerItem.price)
                                mView!!.et_offers_child_price.setText(offerItem.child_price)
                                val offer_dur = offerItem.started_at + " - " + offerItem.ended_at
                                mView!!.et_card_duration.text = offer_dur
//                                findNavController().navigate(R.id.action_addNewOffersFragment_to_myProfileFragment)
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
                                        mView!!.tv_price.text = serviceList[position].price!!.total
                                        mView!!.tv_category_name.text =
                                            serviceList[position].category!!.name
                                    }
                                })
                            mView!!.rv_services_listing.adapter = serviceListingAdapter
                            serviceListingAdapter.notifyDataSetChanged()
                        } else {
                            mView!!.cards_service_categories_listing.visibility = View.GONE
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
       /* myStartDate = mView!!.et_card_duration_from.text.toString().trim()
        myEndDate = mView!!.et_card_duration_to.text.toString().trim()*/
        selectedService = mView!!.tv_choose_service.text.toString().trim()
        offerPrice = mView!!.et_offers_price.text.toString().trim()
        offerChildPrice = mView!!.et_offers_child_price.text.toString().trim()

        if (TextUtils.isEmpty(selectedService)){
            mView!!.tv_choose_service.requestFocus()
            mView!!.tv_choose_service.error = getString(R.string.no_service_selected)
        }else if(TextUtils.isEmpty(offerPrice)){
            mView!!.et_offers_price.requestFocus()
            mView!!.et_offers_price.error = getString(R.string.please_enter_valid_offer_price)
        }else if(TextUtils.isEmpty(offerChildPrice)){
            mView!!.et_offers_child_price.requestFocus()
            mView!!.et_offers_child_price.error = getString(R.string.please_enter_valid_offer_price)
        }else if(TextUtils.isEmpty(offer_duration)){
            mView!!.et_card_duration.requestFocus()
            mView!!.et_card_duration.error = getString(R.string.please_select_a_valid_duration)
        } else{
            if(offer_status.equals("edit")){
                update()
            }else{
                save()
            }
        }
    }

    private fun update(){

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
                "child_price"
            ),
            arrayOf(
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0)
                    .toString(),
                service_id.toString(),
                myStartDate,
                myEndDate,
                offerPrice,
                offerChildPrice
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
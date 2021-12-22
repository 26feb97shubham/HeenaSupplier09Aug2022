package com.heena.supplier.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.bottomsheets.CancelServiceBottomSheetFragment
import com.heena.supplier.models.AcceptRejectBookingResponse
import com.heena.supplier.models.BookingDetailsResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingdetailsFragment : Fragment() {
    var mView : View?= null
    private var booking_id : Int?=null
    private var booking_status : Int ?= null
   // private var booking : BookingItem?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            booking_id = it.getInt("booking_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView= inflater.inflate(R.layout.fragment_bookingdetails, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        showBookingDetails()

        tv_accept_booking.setOnClickListener {
            acceptBooking()
        }

        tv_reject_booking.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookingId", booking_id!!)

            val cancelServiceBottomSheetFragment = CancelServiceBottomSheetFragment.newInstance(requireContext(), bundle)
            cancelServiceBottomSheetFragment.show(requireActivity().supportFragmentManager,CancelServiceBottomSheetFragment.TAG)
            cancelServiceBottomSheetFragment.setCancelServiceClickListenerCallback(object : ClickInterface.OnCancelServiceClick{
                override fun OnCancelService(rsn_for_cancellation: String?) {
                    val builder = APIClient.createBuilder(arrayOf("booking_id", "message"), arrayOf(booking_id.toString(), rsn_for_cancellation.toString()))
                    val call = apiInterface.rejectBooking(builder.build())
                    call?.enqueue(object : Callback<AcceptRejectBookingResponse?> {
                        override fun onResponse(call: Call<AcceptRejectBookingResponse?>, response: Response<AcceptRejectBookingResponse?>) {
                            if (response.isSuccessful){
                                if (response.body()!!.status==1){
                                    LogUtils.shortToast(requireContext(), response.body()!!.message)
                                    findNavController().popBackStack()
                                    LogUtils.shortToast(requireContext(), response.body()!!.message)
                                }else{
                                    LogUtils.shortToast(requireContext(), response.body()!!.message)
                                }
                            }else{
                                LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                            }
                        }

                        override fun onFailure(call: Call<AcceptRejectBookingResponse?>, throwable: Throwable) {
                            LogUtils.shortToast(requireContext(), throwable.message)
                        }

                    })
                }

            })
        }
    }

    private fun showBookingDetails() {
        mView!!.frag_booking_details_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showBooking(booking_id.toString())
        call?.enqueue(object : Callback<BookingDetailsResponse?>{
            override fun onResponse(call: Call<BookingDetailsResponse?>, response: Response<BookingDetailsResponse?>) {
                mView!!.frag_booking_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if(response.isSuccessful){
                    if (response.body()!!.status==1){
                        val booking = response.body()!!.booking
                        Log.e("booking", ""+booking)
                        booking_status = booking!!.status
                        when(booking_status){
                            1 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.approved)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#37CC37"))
                                    mView!!.ll_accept_reject.visibility = View.GONE
                                    mView!!.tv_completed_booking.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.text = getString(R.string.approved)
                                    mView!!.tv_completed_booking.setBackgroundResource(R.drawable.curved_green_filled_rect_box)
                                }
                            }
                            2 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.cancelled)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#FF0909"))
                                    mView!!.ll_accept_reject.visibility = View.GONE
                                    mView!!.tv_completed_booking.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.text = getString(R.string.cancelled)
                                    mView!!.tv_completed_booking.setBackgroundResource(R.drawable.curved_red_filled_rect_box)
                                }
                            }
                            3 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.completed)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#37CC37"))
                                    mView!!.ll_accept_reject.visibility = View.GONE
                                    mView!!.tv_completed_booking.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.text = getString(R.string.completed)
                                    mView!!.tv_completed_booking.setBackgroundResource(R.drawable.curved_green_filled_rect_box)
                                }
                            }
                            4 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.pending)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#FF9F54"))
                                    mView!!.ll_accept_reject.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.visibility = View.GONE
                                }
                            }
                        }

                        mView!!.tv_service.text = booking.service!!.name
                        mView!!.tv_service_desc.text = booking.service.description
                        val street = booking.address!!.street
                        val country = booking.address.country
                        mView!!.tv_address.text = booking.location?.name
                        mView!!.tv_ladies_count.text = booking.c_ladies.toString()
                        mView!!.tv_childrens_count.text = booking.c_children.toString()
                        val booking_date_time = booking.booking_date + " - " + booking.booking_from
                        mView!!.tv_booking_date_time.text = booking_date_time
                        mView!!.tv_special_request_desc.text = booking.message
                        mView!!.tv_service_charge.text = "AED " + booking.service.price
                        mView!!.tv_sub_total.text = "AED " + booking.price!!.total
                        val total = booking.service.price!!.toDouble() + booking.price.total!!.toDouble()
                        mView!!.tv_total.text = "AED " + total
                        Glide.with(requireContext()).load(booking.user!!.image).into(mView!!.supplierImg)
                        if (booking.gallery?.size==0){
                            mView!!.iv_heena_design.setImageResource(R.drawable.hennatattoos)
                        }else{
                            Glide.with(requireContext()).load((booking.gallery?.get(0))).into(mView!!.iv_heena_design)
                        }
                    }else{
                        LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }

            }

            override fun onFailure(call: Call<BookingDetailsResponse?>, throwable: Throwable) {
                mView!!.frag_booking_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.shortToast(requireContext(), throwable.message)
            }
        })
    }

    private fun acceptBooking() {
        mView!!.frag_booking_details_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(arrayOf("booking_id"), arrayOf(booking_id.toString()))
        val call = apiInterface.acceptBooking(builder.build())
        call?.enqueue(object : Callback<AcceptRejectBookingResponse?>{
            override fun onResponse(call: Call<AcceptRejectBookingResponse?>, response: Response<AcceptRejectBookingResponse?>) {
                mView!!.frag_booking_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                        findNavController().popBackStack()
                    }else{
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<AcceptRejectBookingResponse?>, throwable: Throwable) {
                LogUtils.shortToast(requireContext(), throwable.message)
                mView!!.frag_booking_details_progressBar.visibility= View.GONE
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
}
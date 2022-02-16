package com.heena.supplier.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.*
import kotlinx.android.synthetic.main.fragment_bookingdetails.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class BookingdetailsFragment : Fragment() {
    var mView : View?= null
    private var booking_id : Int?=null
    private var booking_status : Int ?= null
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
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
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

        showBookingDetails()

        tv_accept_booking.setSafeOnClickListener {
            acceptBooking()
        }

        tv_reject_booking.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookingId", booking_id!!)

            val cancelServiceBottomSheetFragment = CancelServiceBottomSheetFragment.newInstance(requireContext(), bundle)
            cancelServiceBottomSheetFragment.show(requireActivity().supportFragmentManager,CancelServiceBottomSheetFragment.TAG)
            cancelServiceBottomSheetFragment.isCancelable = false
            cancelServiceBottomSheetFragment.setCancelServiceClickListenerCallback(object : ClickInterface.OnCancelServiceClick{
                override fun OnCancelService(rsn_for_cancellation: String?) {
                    val builder = APIClient.createBuilder(arrayOf("booking_id", "message", "lang"), arrayOf(booking_id.toString(), rsn_for_cancellation.toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
                    val call = apiInterface.rejectBooking(builder.build())
                    call?.enqueue(object : Callback<AcceptRejectBookingResponse?> {
                        override fun onResponse(call: Call<AcceptRejectBookingResponse?>, response: Response<AcceptRejectBookingResponse?>) {
                            if (response.isSuccessful){
                                if (response.body()!!.status==1){
                                    LogUtils.shortToast(requireContext(), response.body()!!.message)
                                    showBookingDetails()
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

        tv_complete_booking.setSafeOnClickListener {
            val completeBookingDialog = AlertDialog.Builder(requireContext())
            completeBookingDialog.setCancelable(false)
            completeBookingDialog.setTitle(requireContext().getString(R.string.complete_booking))
            completeBookingDialog.setMessage(requireContext().getString(R.string.would_you_like_to_complete_this_booking))
            completeBookingDialog.setPositiveButton(requireContext().getString(R.string.yes)
            ) { dialog, _ ->
                acceptBooking()
                dialog!!.dismiss()
            }
            completeBookingDialog.setNegativeButton(requireContext().getString(R.string.no)
            ) { dialog, _ -> dialog!!.cancel() }
            completeBookingDialog.show()

        }
    }

    private fun showBookingDetails() {
        mView!!.frag_booking_details_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showBooking(booking_id.toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<BookingDetailsResponse?>{
            @SuppressLint("SetTextI18n")
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
                                    mView!!.ll_complete_completed.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.text = getString(R.string.approved)
//                                    mView!!.tv_completed_booking.setBackgroundResource(R.drawable.curved_green_filled_rect_box)
                                }
                            }
                            2 -> {
                                mView!!.tv_status.apply {
                                    this.text = getString(R.string.cancelled)
                                    this.isAllCaps = true
                                    this.setTextColor(Color.parseColor("#FF0909"))
                                    mView!!.ll_accept_reject.visibility = View.GONE
                                    mView!!.ll_complete_completed.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.visibility = View.VISIBLE
                                    mView!!.tv_complete_booking.visibility = View.GONE
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
                                    mView!!.ll_complete_completed.visibility = View.VISIBLE
                                    mView!!.tv_completed_booking.visibility = View.VISIBLE
                                    mView!!.tv_complete_booking.visibility = View.GONE
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
                                    mView!!.ll_complete_completed.visibility = View.GONE
                                    mView!!.tv_completed_booking.visibility = View.VISIBLE
                                    mView!!.tv_complete_booking.visibility = View.VISIBLE
                                }
                            }
                        }

                        mView!!.tv_service.text = booking.service!!.name
                        mView!!.tv_service_desc.text = booking.service.description
                        if(booking.address==null){
                            mView!!.tv_address.text = booking.location!!.name
                        }else{
                            val street = booking.address.street
                            val country = booking.address.country
                            mView!!.tv_address.text = street+ " ," + country
                        }
                        mView!!.tv_ladies_count.text = booking.c_ladies.toString()
                        mView!!.tv_childrens_count.text = booking.c_children.toString()
                        val booking_date_time = booking.booking_date + " - " + booking.booking_from
                        mView!!.tv_booking_date_time.text = booking_date_time
                        mView!!.tv_special_request_desc.text = booking.message
                        mView!!.tv_service_charge.text = "AED ${
                            Utility.convertDoubleValueWithCommaSeparator(
                                booking.commission!!.toDouble()
                            )
                        }"
                        mView!!.tv_sub_total.text = "AED ${
                            Utility.convertDoubleValueWithCommaSeparator(
                                booking.price!!.total!!.toDouble()
                            )
                        }"
                        val total = booking.commission.toDouble() + booking.price.total!!.toDouble()
                        mView!!.tv_total.text = "AED ${
                            Utility.convertDoubleValueWithCommaSeparator(
                                total
                            )
                        }"
                        Glide.with(requireContext()).load(booking.user!!.image).into(mView!!.supplierImg)
                        if (booking.gallery?.size==0){
                            mView!!.iv_heena_design.setImageResource(R.drawable.hennatattoos)
                        }else{
                            Glide.with(requireContext()).load((booking.gallery?.get(0))).into(mView!!.iv_heena_design)
                        }

                        if(booking.card!=null){
                            mView!!.tv_payment_name.text = booking.card.CardY!!.name
                            mView!!.tv_payment_desc.text = requireContext().getString(R.string.ending_in)+ " "+booking.card.CardY!!.lastFour
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
        val builder = APIClient.createBuilder(arrayOf("booking_id", "lang"), arrayOf(booking_id.toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.acceptBooking(builder.build())
        call?.enqueue(object : Callback<AcceptRejectBookingResponse?>{
            override fun onResponse(call: Call<AcceptRejectBookingResponse?>, response: Response<AcceptRejectBookingResponse?>) {
                mView!!.frag_booking_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                        showBookingDetails()
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
package com.dev.heenasupplier.fragments

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.BookingHistoryAdapter
import com.dev.heenasupplier.adapters.BookingsAdapter
import com.dev.heenasupplier.adapters.CurrentBookingsAdapter
import com.dev.heenasupplier.models.BookingItem
import com.dev.heenasupplier.models.BookingsListingResponse
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import com.dev.heenasupplier.utils.Utility.Companion.booking_item_type
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_bookings.*
import kotlinx.android.synthetic.main.fragment_my_bookings.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBookingsFragment : Fragment() {
    lateinit var bookingsAdapter : BookingsAdapter
    var drawable1 : Drawable?= null
    var drawable2 : Drawable?= null
    val arabic_animId = R.anim.layout_animation_right_to_left
    val english_animId = R.anim.layout_animation_left_to_right
    lateinit var layoutAnimationController: LayoutAnimationController
    private var mView : View?=null
    private var bookingList = ArrayList<BookingItem>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(
                R.layout.fragment_my_bookings, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    booking_item_type = 1
                    getCurrentBookings()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "My Bookings Fragment")
        }else{
            booking_item_type = 1
            getCurrentBookings()
        }

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }


        mView!!.rv_tabs_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        tv_current_bookings.setOnClickListener {
            drawable1 = resources.getDrawable(R.drawable.little_gold_curved)
            drawable2 = resources.getDrawable(R.drawable.curved_white_filled_rect_box)
            tv_current_bookings.background = drawable1
            tv_bookings_history.background = drawable2
            tv_current_bookings.setTextColor(Color.parseColor("#FFFFFFFF"))
            tv_bookings_history.setTextColor(Color.parseColor("#D0B67A"))
            booking_item_type = 1
            getCurrentBookings()
        }

       tv_bookings_history.setOnClickListener {
            drawable1 = resources.getDrawable(R.drawable.curved_white_filled_rect_box)
            drawable2 = resources.getDrawable(R.drawable.little_gold_curved)
            tv_current_bookings.background = drawable1
            tv_bookings_history.background = drawable2
            tv_current_bookings.setTextColor(Color.parseColor("#D0B67A"))
            tv_bookings_history.setTextColor(Color.parseColor("#FFFFFFFF"))
           booking_item_type = 2
           getBookingsHistory()
        }

    }

    private fun getCurrentBookings() {
        mView!!.fragment_bookings_progressBar.visibility = View.VISIBLE
        mView!!.tv_no_bookings_found.visibility = View.GONE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val hashMap = HashMap<String, String>()
//        hashMap.put("lang", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        hashMap.put("type", "1")
        hashMap.put("user_id", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString())
        val call = apiInterface.getBookingsList(hashMap)
        call?.enqueue(object : Callback<BookingsListingResponse?> {
            override fun onResponse(
                    call: Call<BookingsListingResponse?>,
                    response: Response<BookingsListingResponse?>
            ) {
                mView!!.fragment_bookings_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status == 1){
                        mView!!.tv_no_bookings_found.visibility = View.GONE
                        mView!!.nsv_bookings.visibility = View.VISIBLE
                        bookingList = response.body()!!.booking as ArrayList<BookingItem>
                        setBookingsAdapter()
                    }else{
                        mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                        mView!!.nsv_bookings.visibility = View.GONE
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                    mView!!.nsv_bookings.visibility = View.GONE
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<BookingsListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.fragment_bookings_progressBar.visibility= View.GONE
                mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                mView!!.nsv_bookings.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun getBookingsHistory() {
        mView!!.fragment_bookings_progressBar.visibility = View.VISIBLE
        mView!!.tv_no_bookings_found.visibility = View.GONE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val hashMap = HashMap<String, String>()
        hashMap.put("lang", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        hashMap.put("type", "2")
        hashMap.put("user_id", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString())
        val call = apiInterface.getBookingsList(hashMap)
        call?.enqueue(object : Callback<BookingsListingResponse?>{
            override fun onResponse(
                    call: Call<BookingsListingResponse?>,
                    response: Response<BookingsListingResponse?>
            ) {
                mView!!.fragment_bookings_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status == 1){
                        mView!!.tv_no_bookings_found.visibility = View.GONE
                        mView!!.nsv_bookings.visibility = View.VISIBLE
                        bookingList = response.body()!!.booking as ArrayList<BookingItem>
                        setBookingsHistoryAdapter()
                    }else{
                        mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                        mView!!.nsv_bookings.visibility = View.GONE
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                    mView!!.nsv_bookings.visibility = View.GONE
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<BookingsListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.fragment_bookings_progressBar.visibility= View.GONE
                mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                mView!!.nsv_bookings.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun setBookingsAdapter() {
        bookingsAdapter = BookingsAdapter(requireContext(), bookingList, object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
                val bundle = Bundle()
                bundle.putInt("booking_id", bookingList[position].booking_id!!)
                findNavController().navigate(R.id.action_appointmentFragment_to_bookingDetailsFragment, bundle)
            }

        })

        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,"")=="ar"){
            layoutAnimationController = AnimationUtils.loadLayoutAnimation(requireContext(), arabic_animId)
        }else{
            layoutAnimationController = AnimationUtils.loadLayoutAnimation(requireContext(), english_animId)
        }
        rv_tabs_listing.layoutAnimation = layoutAnimationController
        rv_tabs_listing.adapter = bookingsAdapter
        bookingsAdapter.notifyDataSetChanged()
        rv_tabs_listing.scheduleLayoutAnimation()
    }

    private fun setBookingsHistoryAdapter() {
        bookingsAdapter = BookingsAdapter(requireContext(), bookingList, object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
                val bundle = Bundle()
                bundle.putInt("booking_id", bookingList[position].booking_id!!)
                findNavController().navigate(R.id.action_appointmentFragment_to_bookingDetailsFragment, bundle)
            }

        })

        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,"")=="ar"){
            layoutAnimationController = AnimationUtils.loadLayoutAnimation(requireContext(), arabic_animId)
        }else{
            layoutAnimationController = AnimationUtils.loadLayoutAnimation(requireContext(), english_animId)
        }
        mView!!. rv_tabs_listing.layoutAnimation = layoutAnimationController
        mView!!.rv_tabs_listing.adapter = bookingsAdapter
        bookingsAdapter.notifyDataSetChanged()
        mView!!.rv_tabs_listing.scheduleLayoutAnimation()
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
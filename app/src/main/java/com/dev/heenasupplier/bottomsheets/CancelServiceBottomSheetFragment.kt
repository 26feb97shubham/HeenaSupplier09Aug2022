package com.dev.heenasupplier.bottomsheets

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.models.AcceptRejectBookingResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_cancel_service_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_cancel_service_bottom_sheet.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CancelServiceBottomSheetFragment : BottomSheetDialogFragment() {
    private var mView : View?=null
    private var rsn_for_cancellation : String ?= null
    private var onServiceCancelClick : ClickInterface.OnCancelServiceClick?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView  = inflater.inflate(
                R.layout.fragment_cancel_service_bottom_sheet, container, false)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tv_proceed!!.setOnClickListener { // dismiss dialog
            /*findNavController().navigate(R.id.action_filterbottomsheetdialogfragment_to_filteredproductsfragment)*/

            rsn_for_cancellation = mView!!.et_rsn_for_cancellation.text.toString().trim()
            if (TextUtils.isEmpty(rsn_for_cancellation)){
                mView!!.et_rsn_for_cancellation.requestFocus()
                mView!!.et_rsn_for_cancellation.error = getString(R.string.please_enter_valid_message)
            }else{
                onServiceCancelClick!!.OnCancelService(rsn_for_cancellation)
                dismiss()
            }
        }
    }

    fun setCancelServiceClickListenerCallback(onServiceCancelClick : ClickInterface.OnCancelServiceClick){
        this.onServiceCancelClick = onServiceCancelClick
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }


    companion object {
        const val TAG = "CancelServiceBottomSheetFragment"
        private var bookingId : Int?= null
        private var instance: SharedPreferenceUtility? = null
        fun newInstance(context: Context?, bundle: Bundle): CancelServiceBottomSheetFragment {
            //this.context = context;
            if (bundle!=null){
                bookingId = bundle.getInt("bookingId")
            }
            return CancelServiceBottomSheetFragment()
        }

        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}
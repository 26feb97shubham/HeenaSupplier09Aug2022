package com.heena.supplier.bottomsheets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_book_service_bottom_sheet.*

class BookServiceBottomSheetFragment : BottomSheetDialogFragment() {
    private var OnBookServiceCallback : ClickInterface.OnBookServiceClick?= null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view  = inflater.inflate(
            R.layout.fragment_book_service_bottom_sheet, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tv_book_service!!.setOnClickListener { // dismiss dialog
            /*findNavController().navigate(R.id.action_filterbottomsheetdialogfragment_to_filteredproductsfragment)*/
            OnBookServiceCallback!!.OnBookService()
            dismiss()
        }
    }

    fun setSubscribeClickListenerCallback(OnBookServiceCallback: ClickInterface.OnBookServiceClick){
        this.OnBookServiceCallback = OnBookServiceCallback
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        const val TAG = "BookServiceBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null
        fun newInstance(context: Context?): BookServiceBottomSheetFragment {
            //this.context = context;
            return BookServiceBottomSheetFragment()
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
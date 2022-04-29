package com.heena.supplier.Dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.heena.supplier.R
import com.heena.supplier.application.MyApp
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.fragment_nointernet_dialog.view.*

class NoInternetDialog : DialogFragment() {
    private var retryCallback : RetryInterface?=null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nointernet_dialog, container, false)
        Utility.changeLanguage(
            requireContext(),
            MyApp.sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        setUpViews(view)
        return view
    }

    private fun setUpViews(view: View) {
        view.retry.setSafeOnClickListener {
            if (Utility.hasConnection(requireContext())){
                retryCallback!!.retry()
            }else{
                LogUtils.shortToast(requireContext(), "Still No Internet")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun setRetryCallback(retryCallback: RetryInterface){
        this.retryCallback = retryCallback
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

    interface RetryInterface{
        fun retry()
    }
}
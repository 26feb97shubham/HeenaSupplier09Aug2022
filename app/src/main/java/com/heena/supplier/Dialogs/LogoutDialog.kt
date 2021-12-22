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
import com.heena.supplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.fragment_logout_dialog.view.*

class LogoutDialog : DialogFragment() {
    private var completionCallback: LogoutInterface? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_logout_dialog, container, false)
        setUpViews(view)
        return view
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun setDataCompletionCallback(completionCallback: LogoutInterface?) {
        this.completionCallback = completionCallback
    }



    private fun setUpViews(view: View) {

        view.cancel.setOnClickListener {
            dismiss()
        }

        view.ok.setOnClickListener {
            completionCallback?.complete()
            dismiss()
        }
    }

    interface LogoutInterface {
        fun complete()
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
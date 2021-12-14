package com.dev.heenasupplier.Dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.fragment_expired_membership_dialog.view.*
import kotlinx.android.synthetic.main.fragment_nointernet_dialog.view.*
import kotlinx.android.synthetic.main.fragment_nointernet_dialog.view.retry

class ExpiredMembershipDialogFragment : DialogFragment() {
    private var subscribeMembershipCallback : SubscribeMembershipInterface?=null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expired_membership_dialog, container, false)
        setUpViews(view)
        return view
    }
    private fun setUpViews(view: View) {
        view.subscribe_membership.setOnClickListener {
            subscribeMembershipCallback!!.subscribe_membership()
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

    fun subscribeCallback(subscribeMembershipCallback : SubscribeMembershipInterface){
        this.subscribeMembershipCallback = subscribeMembershipCallback
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

    interface SubscribeMembershipInterface{
        fun subscribe_membership()
    }
}
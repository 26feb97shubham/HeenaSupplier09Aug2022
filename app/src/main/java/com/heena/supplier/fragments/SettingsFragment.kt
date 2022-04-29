package com.heena.supplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    private var mView : View?=null
    var profile_picture:String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_settings, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.txtChangePass.setOnClickListener {
            mView!!.txtChangePass.startAnimation(AlphaAnimation(1f, 0.5f))
            val args= Bundle()
            args.putString("profile_picture", profile_picture)
            findNavController().navigate(R.id.action_SettingsFragment_to_changePasswordFragment, args)
        }

        mView!!.btneditprofile.setOnClickListener {
            mView!!.btneditprofile.startAnimation(AlphaAnimation(1f, .5f))
            findNavController().navigate(R.id.action_SettingsFragment_to_editProfileFragment)
        }
    }
}
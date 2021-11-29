package com.dev.heenasupplier.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_locations.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyLocationsFragment : Fragment() {
    private val appPerms = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if(allAreGranted) {
                Log.e("Granted", "Permissions")
                val bundle = Bundle()
                bundle.putString("direction", "Navigation")
//                findNavController().navigate(R.id.action_mylocationsFragment_to_addressBottomSheetFragment, bundle)
                true
            }else{
                Log.e("Denied", "Permissions")
            }
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.homeFragment)
        }

        requireActivity().iv_notification.setOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }


        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    getAddressList()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "MyLocations Fragment")
        }

        getAddressList()

        tv_add_new_address.setOnClickListener {
            activityResultLauncher.launch(appPerms)
        }
    }

    private fun getAddressList() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_locations, container, false)
    }

    override fun onResume() {
        super.onResume()
        getAddressList()
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
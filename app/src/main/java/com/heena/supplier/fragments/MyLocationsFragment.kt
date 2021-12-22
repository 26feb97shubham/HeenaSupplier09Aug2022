package com.heena.supplier.fragments

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
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.AddressListAdapter
import com.heena.supplier.bottomsheets.AddAddressBottomSheetFragment
import com.heena.supplier.models.AddEditDeleteAddressResponse
import com.heena.supplier.models.AddressItem
import com.heena.supplier.models.AddressListingResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_locations.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyLocationsFragment : Fragment() {
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    private var addressesList = ArrayList<AddressItem>()
    private var addressListAdapter: AddressListAdapter?=null
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
                findNavController().navigate(R.id.action_mylocationsFragment_to_addressSheetFragment, bundle)
                true
            }else{
                Log.e("Denied", "Permissions")
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_locations, container, false)
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
        val call = apiInterface.getAddressList(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        call?.enqueue(object : Callback<AddressListingResponse?> {
            override fun onResponse(
                call: Call<AddressListingResponse?>,
                response: Response<AddressListingResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == 0) {
                        noLocationView.visibility = View.VISIBLE
                        cl_locations.visibility = View.GONE
                    } else {
                        noLocationView.visibility = View.GONE
                        cl_locations.visibility = View.VISIBLE
                        addressesList = response.body()!!.address as ArrayList<AddressItem>
                        rv_address_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        addressListAdapter = AddressListAdapter(requireContext(), addressesList, object : ClickInterface.OnAddressItemClick {
                            override fun editAdddress(position: Int) {
                                val bundle = Bundle()
                                bundle.putString("status", "edit")
                                bundle.putInt("addressId", addressesList[position].address_id!!.toInt())
                                val addressBottomSheetFragment = AddAddressBottomSheetFragment.newInstance(requireContext(), bundle)

                                addressBottomSheetFragment.show(requireActivity().supportFragmentManager, AddressSheetFragment.TAG)
                            }

                            override fun deleteAddress(position: Int) {
                                val call = apiInterface.deleteAddress(response.body()!!.address?.get(position)!!.address_id!!.toInt(),
                                    SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
                                call?.enqueue(object : Callback<AddEditDeleteAddressResponse?> {
                                    override fun onResponse(
                                        call: Call<AddEditDeleteAddressResponse?>,
                                        response: Response<AddEditDeleteAddressResponse?>
                                    ) {
                                        if (response.isSuccessful) {
                                            if (response.body()!!.status == 1) {
                                                addressesList.removeAt(position)
                                                if (rv_address_list.adapter != null) {
                                                    rv_address_list.adapter!!.notifyDataSetChanged()
                                                    if (addressesList.size == 0) {
                                                        noLocationView.visibility = View.GONE
                                                        cl_locations.visibility = View.VISIBLE
                                                    }
                                                    LogUtils.longToast(requireContext(), response.body()!!.message)
                                                } else {
                                                    LogUtils.longToast(requireContext(), response.body()!!.message)
                                                }
                                                LogUtils.longToast(requireContext(), response.body()!!.message)
                                            } else {
                                                LogUtils.longToast(requireContext(), response.body()!!.message)
                                            }
                                        } else {
                                            LogUtils.longToast(requireContext(), requireContext().getString(R.string.response_isnt_successful))
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<AddEditDeleteAddressResponse?>,
                                        throwable: Throwable
                                    ) {
                                        LogUtils.e("msg", throwable.message)
                                        LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                                    }

                                })
                            }

                        })

                        rv_address_list.adapter = addressListAdapter
                        addressListAdapter!!.notifyDataSetChanged()
                    }
                } else {
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    noLocationView.visibility = View.GONE
                    cl_locations.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<AddressListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                noLocationView.visibility = View.GONE
                cl_locations.visibility = View.VISIBLE
            }

        })
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
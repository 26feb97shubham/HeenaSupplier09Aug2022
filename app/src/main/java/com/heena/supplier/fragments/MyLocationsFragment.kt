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
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.AddressListAdapter
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.bottomsheets.AddAddressBottomSheetFragment
import com.heena.supplier.models.AddEditDeleteAddressResponse
import com.heena.supplier.models.AddressItem
import com.heena.supplier.models.AddressListingResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_locations.*
import kotlinx.android.synthetic.main.fragment_my_locations.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyLocationsFragment : Fragment() {
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    private var addressesList = ArrayList<AddressItem>()
    private var addressListAdapter: AddressListAdapter?=null
    private var mView : View?=null
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
        mView = inflater.inflate(R.layout.fragment_my_locations, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().navigate(R.id.homeFragment)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
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
        }else{
            getAddressList()
        }

        mView!!.tv_add_new_address.setSafeOnClickListener {
            activityResultLauncher.launch(appPerms)
        }
    }

    private fun getAddressList() {
        val call = apiInterface.getAddressList(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
        call?.enqueue(object : Callback<AddressListingResponse?> {
            override fun onResponse(
                call: Call<AddressListingResponse?>,
                response: Response<AddressListingResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!==null){
                        if (response.body()!!.status == 0) {
                            noLocationView.visibility = View.VISIBLE
                            cl_locations.visibility = View.GONE
                            Utility.showSnackBarOnResponseError(mView!!.myLocationsFragmentRelativeLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        } else {
                            noLocationView.visibility = View.GONE
                            cl_locations.visibility = View.VISIBLE
                            addressesList.clear()
                            addressesList = response.body()!!.address as ArrayList<AddressItem>
                            rv_address_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            addressListAdapter = AddressListAdapter(requireContext(), addressesList, object : ClickInterface.OnAddressItemClick {
                                override fun editAdddress(position: Int) {
                                    val updateAddressDialog = AlertDialog.Builder(requireContext())
                                    updateAddressDialog.setCancelable(false)
                                    updateAddressDialog.setTitle(requireContext().getString(R.string.update_address))
                                    updateAddressDialog.setMessage(requireContext().getString(R.string.would_you_like_to_update_your_address))
                                    updateAddressDialog.setPositiveButton(requireContext().getString(R.string.yes))
                                    { dialog, _ ->
                                        updateAddressDialog.show()
                                        val bundle = Bundle()
                                        bundle.putString("status", "edit")
                                        bundle.putInt("addressId", addressesList[position].address_id!!.toInt())
                                        val addressBottomSheetFragment = AddAddressBottomSheetFragment.newInstance(requireContext(), bundle)
                                        addressBottomSheetFragment.show(requireActivity().supportFragmentManager, AddressSheetFragment.TAG)
                                    }
                                    updateAddressDialog.setNegativeButton(requireContext().getString(R.string.no))
                                    { dialog, _ -> dialog!!.cancel() }
                                    updateAddressDialog.show()
                                }

                                override fun deleteAddress(position: Int) {
                                    val address_id = addressesList.get(position).address_id!!.toInt()
                                    val deleteServiceDialog = AlertDialog.Builder(requireContext())
                                    deleteServiceDialog.setCancelable(false)
                                    deleteServiceDialog.setTitle(requireContext().getString(R.string.delete_address))
                                    deleteServiceDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_address))
                                    deleteServiceDialog.setPositiveButton(requireContext().getString(R.string.delete)
                                    ) { dialog, _ ->
                                        delete_Address(address_id, position)
                                        dialog!!.dismiss()
                                    }
                                    deleteServiceDialog.setNegativeButton(requireContext().getString(R.string.cancel)
                                    ) { dialog, _ -> dialog!!.cancel() }
                                    deleteServiceDialog.show()
                                }

                            })
                            rv_address_list.adapter = addressListAdapter
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.myLocationsFragmentRelativeLayout,
                            response.message(),
                            requireContext())
                    }
                } else {
                    Utility.showSnackBarOnResponseError(mView!!.myLocationsFragmentRelativeLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                    noLocationView.visibility = View.GONE
                    cl_locations.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<AddressListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.myLocationsFragmentRelativeLayout,
                    throwable.message.toString(),
                    requireContext())
                noLocationView.visibility = View.GONE
                cl_locations.visibility = View.VISIBLE
            }
        })
    }

    fun delete_Address(address_id: Int, position: Int) {
        val call = apiInterface.deleteAddress(address_id,
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
        call?.enqueue(object : Callback<AddEditDeleteAddressResponse?> {
            override fun onResponse(
                call: Call<AddEditDeleteAddressResponse?>,
                response: Response<AddEditDeleteAddressResponse?>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!=null){
                        if (response.body()!!.status == 1) {
                            addressesList.removeAt(position)
                            if (rv_address_list.adapter != null) {
                                rv_address_list.adapter!!.notifyDataSetChanged()
                                if (addressesList.size == 0) {
                                    noLocationView.visibility = View.VISIBLE
                                    cl_locations.visibility = View.GONE
                                }else{
                                    noLocationView.visibility = View.GONE
                                    cl_locations.visibility = View.VISIBLE
                                }
                            } else {
                                LogUtils.longToast(requireContext(), response.body()!!.message)
                            }
                            Utility.showSnackBarOnResponseSuccess(mView!!.myLocationsFragmentRelativeLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        } else {
                            Utility.showSnackBarOnResponseError(mView!!.myLocationsFragmentRelativeLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.myLocationsFragmentRelativeLayout,
                            response.message(),
                            requireContext())
                    }
                } else {
                    Utility.showSnackBarOnResponseError(mView!!.myLocationsFragmentRelativeLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(
                call: Call<AddEditDeleteAddressResponse?>,
                throwable: Throwable
            ) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseSuccess(mView!!.myLocationsFragmentRelativeLayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getAddressList()
    }
}
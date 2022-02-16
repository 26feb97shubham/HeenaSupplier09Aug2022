package com.heena.supplier.bottomsheets

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.CountryListingAdapter
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.activity_sign_up2.cards_countries_listing
import kotlinx.android.synthetic.main.activity_sign_up2.rv_countries_listing
import kotlinx.android.synthetic.main.activity_sign_up2.tv_emirate
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class AddAddressBottomSheetFragment : BottomSheetDialogFragment(){
    private var mView: View?=null
    private var flat_villa: String ?=null
    private var building_name : String?=null
    private var title : String?=null
    private var street_area : String?=null
    private var is_default : Int?=null
    private var countryList = ArrayList<CountryItem>()
    lateinit var countryListingAdapter: CountryListingAdapter
    private var selectedCountry : String?=null
    private var countryId : Int?=null
    private var is_set_default = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_add_address_bottom_sheet, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        if (status.equals("edit")){
            mView!!.tv_save.text = getString(R.string.update)
            showAddress()
        }else{
            mView!!.tv_save.text = getString(R.string.save)
            mView!!.et_flat_villa.setText("")
            mView!!.et_building_name.setText("")
            mView!!.et_title.setText("")
            mView!!.et_street_area.setText("")
            mView!!.tv_emirate.setText("")
        }

        mView!!.tv_save.setOnClickListener {
            validate(countryId)
        }

        mView!!.tv_emirate.setOnClickListener {
            getCountires()
        }

        mView!!.iv_toggle_off.setOnClickListener {
            is_set_default = true
            is_default = 0
            mView!!.iv_toggle_off.visibility = View.GONE
            mView!!.iv_toggle_on.visibility = View.VISIBLE
        }

        mView!!.iv_toggle_on.setOnClickListener {
            is_set_default = false
            is_default = 1
            mView!!.iv_toggle_off.visibility = View.VISIBLE
            mView!!.iv_toggle_on.visibility = View.GONE
        }
    }


    private fun showAddress() {
        val call = apiInterface.showAddress(addressId, SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        call?.enqueue(object : Callback<ShowAddressResponse?> {
            override fun onResponse(call: Call<ShowAddressResponse?>, response: Response<ShowAddressResponse?>) {
                try {
                    if (response.body() != null) {
                        if (response.body()!!.status==1){
                            val addressItem = response.body()!!.address
                            mView!!.et_flat_villa.setText(addressItem!!.flat)
                            mView!!.et_building_name.setText(addressItem.building_name)
                            mView!!.et_title.setText(addressItem.title)
                            mView!!.et_street_area.setText(addressItem.street)
                            mView!!.tv_emirate.text = addressItem.country!!.name
                            if (addressItem.is_default==1){
                                mView!!.iv_toggle_on.visibility = View.VISIBLE
                                mView!!.iv_toggle_off.visibility = View.VISIBLE
                            }
                        }else{
                            mView!!.cards_countries_listing.visibility = View.GONE
                            LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                        }
                    }else {
                        mView!!.cards_countries_listing.visibility = View.GONE
                        LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ShowAddressResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(),throwable.message)
            }

        })
    }

    private fun getCountires() {
        if (isNetworkAvailable()){
            val call = apiInterface.getCountries(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
            call!!.enqueue(object : Callback<CountryResponse?> {
                override fun onResponse(call: Call<CountryResponse?>, response: Response<CountryResponse?>) {
                    try {
                        if (response.body() != null) {
                            if (response.body()!!.status==1){
                                cards_countries_listing.visibility = View.VISIBLE
                                rv_countries_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                                countryList = response.body()!!.country as ArrayList<CountryItem>
                                countryListingAdapter = CountryListingAdapter(requireContext(), countryList, object :  ClickInterface.OnRecyclerItemClick{
                                    override fun OnClickAction(position: Int) {
                                        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,"").equals("ar")){
                                            tv_emirate.text = countryList[position].name_ar
                                        }else
                                        {
                                            tv_emirate.text = countryList[position].name
                                        }
                                        selectedCountry = tv_emirate.text.toString().trim()
                                        cards_countries_listing.visibility = View.GONE
                                        countryId = countryList[position].country_id
                                        Log.e("service_id", countryId.toString())
                                    }
                                })
                                rv_countries_listing.adapter = countryListingAdapter
                                countryListingAdapter.notifyDataSetChanged()
                            }else{
                                cards_countries_listing.visibility = View.GONE
                                LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                            }
                        }else {
                            cards_countries_listing.visibility = View.GONE
                            LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<CountryResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    LogUtils.shortToast(requireContext(),throwable.message)
                }
            })
        }else{
            LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
        }
    }

    private fun validate(countryId: Int?) {
        flat_villa = mView!!.et_flat_villa.text.toString().trim()
        building_name = mView!!.et_building_name.text.toString().trim()
        title = mView!!.et_title.text.toString().trim()
        street_area = mView!!.et_street_area.text.toString().trim()

        if (TextUtils.isEmpty(flat_villa)){
            mView!!.et_flat_villa.requestFocus()
            mView!!.et_flat_villa.error = getString(R.string.please_enter_valid_flat_villa_name)
        }else if(TextUtils.isEmpty(building_name)){
            mView!!.et_building_name.requestFocus()
            mView!!.et_building_name.error = getString(R.string.please_enter_valid_building_name)
        }else if(TextUtils.isEmpty(title)){
            mView!!.et_title.requestFocus()
            mView!!.et_title.error = getString(R.string.please_enter_valid_title)
        }else if (TextUtils.isEmpty(street_area)){
            mView!!.et_street_area.requestFocus()
            mView!!.et_street_area.error = getString(R.string.please_enter_valid_street_area)
        }else{
            if (status.equals("edit")){
                editAddress(addressId.toString())
            }else{
                saveAddress(countryId)
            }
        }
    }

    private fun editAddress(address_id: String) {
        val edit_address_builder = APIClient.createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "address_id", "lang"),
            arrayOf(flat_villa.toString(),
                title.toString(),
                street_area.toString(),
                is_default.toString(),
                countryId.toString(),
                building_name.toString(),
                address_id.toString(),
            SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.editAddress(edit_address_builder.build())
        call!!.enqueue(object : Callback<AddEditDeleteAddressResponse?> {
            override fun onResponse(call: Call<AddEditDeleteAddressResponse?>, response: Response<AddEditDeleteAddressResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            dismiss()
                        }
                    }else{
                        LogUtils.shortToast(requireContext(), response.message())
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<AddEditDeleteAddressResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.localizedMessage)
            }

        })
    }

    private fun saveAddress(country_Id: Int?) {
        val add_address_builder = APIClient.createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "user_id", "lang"),
            arrayOf(flat_villa.toString(),
                title.toString(),
                street_area.toString(),
                is_default.toString(),
                country_Id.toString(),
                building_name.toString(),
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString(),
            SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.addAddress(add_address_builder.build())
        call!!.enqueue(object : Callback<AddEditDeleteAddressResponse?> {
            override fun onResponse(call: Call<AddEditDeleteAddressResponse?>, response: Response<AddEditDeleteAddressResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            dismiss()
                        }
                    }else{
                        LogUtils.shortToast(requireContext(), response.message())
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<AddEditDeleteAddressResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.localizedMessage)
            }

        })
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddAddressBottomSheetFragment.
         */
        // TODO: Rename and change types and number of parameters

        const val TAG = "AddAddressBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null
        private var status = ""
        private var addressId = 0
        fun newInstance(context: Context?, bundle: Bundle?): AddAddressBottomSheetFragment {
            if (bundle!=null){
                status = bundle.getString("status").toString()
                addressId = bundle.getInt("addressId")
            }else{
                Log.e("bundle is ", "null")
            }
            return AddAddressBottomSheetFragment()
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
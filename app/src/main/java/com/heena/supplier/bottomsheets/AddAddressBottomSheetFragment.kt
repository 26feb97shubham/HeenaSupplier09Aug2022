package com.heena.supplier.bottomsheets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.CountryListingAdapter
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.Utility
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
    private var is_default = 0
    lateinit var countryListingAdapter: CountryListingAdapter
    private var selectedCountry : String?=null
    private var is_set_default = false
    private var selectedEmirates : String?=null
    private var emiratesList = ArrayList<Emirates>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_add_address_bottom_sheet, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
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
            validate(229)
        }

        mView!!.tv_emirate.setOnClickListener {
            getEmirates()
        }

        mView!!.iv_toggle_off.setOnClickListener {
            is_set_default = true
            is_default = 1
            mView!!.iv_toggle_off.visibility = View.GONE
            mView!!.iv_toggle_on.visibility = View.VISIBLE
        }

        mView!!.iv_toggle_on.setOnClickListener {
            is_set_default = false
            is_default = 0
            mView!!.iv_toggle_off.visibility = View.VISIBLE
            mView!!.iv_toggle_on.visibility = View.GONE
        }
    }


    private fun showAddress() {
        val call = apiInterface.showAddress(addressId, sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
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
                            if (addressItem.is_default==0){
                                mView!!.iv_toggle_off.visibility = View.VISIBLE
                                mView!!.iv_toggle_on.visibility = View.GONE
                            }else{
                                mView!!.iv_toggle_off.visibility = View.GONE
                                mView!!.iv_toggle_on.visibility = View.VISIBLE
                            }
                            is_default = addressItem.is_default!!
                        }else{
                            mView!!.cards_country_listing.visibility = View.GONE
                            mView!!.cards_emirates_listing.visibility = View.GONE
                            Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else {
                        mView!!.cards_country_listing.visibility = View.GONE
                        mView!!.cards_emirates_listing.visibility = View.GONE
                        Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                            response.message(),
                            requireContext())
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
                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    private fun getEmirates() {
        if (isNetworkAvailable()){
            val call = apiInterface.getEmirates(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
            call!!.enqueue(object : Callback<EmiratesListResponse?> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<EmiratesListResponse?>, response: Response<EmiratesListResponse?>) {
                    try {
                        if (response.body() != null) {
                            if (response.body()!!.status==1){
                                mView!!.cards_emirates_listing.visibility = View.VISIBLE
                                mView!!.rv_emirates_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                                emiratesList = response.body()!!.emirates as ArrayList<Emirates>
                                countryListingAdapter = CountryListingAdapter(requireContext(),null, emiratesList, object :  ClickInterface.OnRecyclerItemClick{
                                    override fun OnClickAction(position: Int) {
                                        if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "").equals("ar")){
                                            mView!!.tv_emirate.text = emiratesList[position].nameAr
                                        }else{
                                            mView!!.tv_emirate.text = emiratesList[position].name
                                        }
                                        selectedEmirates = mView!!.tv_emirate.text.toString().trim()
                                        mView!!.cards_emirates_listing.visibility = View.GONE
                                    }
                                })
                                mView!!.rv_emirates_listing.adapter = countryListingAdapter
                                countryListingAdapter.notifyDataSetChanged()
                            }else{
                                mView!!.cards_emirates_listing.visibility = View.GONE
                                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                                    response.body()!!.message.toString(),
                                    requireContext())
                            }
                        }else {
                            mView!!.cards_emirates_listing.visibility = View.GONE
                            Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                                response.message(),
                                requireContext())
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<EmiratesListResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                        throwable.message.toString(),
                        requireContext())
                }
            })
        }else{
            Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                requireContext().getString(R.string.check_internet),
                requireContext())
        }

    }

    private fun validate(countryId: Int?) {
        flat_villa = mView!!.et_flat_villa.text.toString().trim()
        building_name = mView!!.et_building_name.text.toString().trim()
        title = mView!!.et_title.text.toString().trim()
        street_area = mView!!.et_street_area.text.toString().trim()

        when {
            TextUtils.isEmpty(flat_villa) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    requireContext().getString(R.string.please_enter_valid_flat_villa_name),
                    requireContext())
            }
            TextUtils.isEmpty(building_name) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    requireContext().getString(R.string.please_enter_valid_building_name),
                    requireContext())
            }
            TextUtils.isEmpty(title) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    requireContext().getString(R.string.please_enter_valid_title),
                    requireContext())
            }
            TextUtils.isEmpty(street_area) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    requireContext().getString(R.string.please_enter_valid_street_area),
                    requireContext())
            }
            TextUtils.isEmpty(selectedEmirates) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    requireContext().getString(R.string.please_enter_valid_emirate),
                    requireContext())
            }
           /* TextUtils.isEmpty(selectedCountry) -> {
                Utility.showSnackBarValidationError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    requireContext().getString(R.string.please_enter_valid_country),
                    requireContext())
            }*/
            else -> {
                if (status.equals("edit")){
                    if (countryId != null) {
                        editAddress(addressId.toString(), countryId)
                    }
                }else{
                    saveAddress(countryId)
                }
            }
        }
    }

    private fun editAddress(address_id: String, countryId: Int) {
        val edit_address_builder = APIClient.createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "address_id", "lang"),
            arrayOf(flat_villa.toString(),
                title.toString(),
                street_area.toString(),
                is_default.toString(),
                countryId.toString(),
                building_name.toString(),
                address_id.toString(),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.editAddress(edit_address_builder.build())
        call!!.enqueue(object : Callback<AddEditDeleteAddressResponse?> {
            override fun onResponse(call: Call<AddEditDeleteAddressResponse?>, response: Response<AddEditDeleteAddressResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            dismiss()
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                            response.message(),
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<AddEditDeleteAddressResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    throwable.message.toString(),
                    requireContext())
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
                sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0).toString(),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.addAddress(add_address_builder.build())
        call!!.enqueue(object : Callback<AddEditDeleteAddressResponse?> {
            override fun onResponse(call: Call<AddEditDeleteAddressResponse?>, response: Response<AddEditDeleteAddressResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            dismiss()
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                            response.message(),
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<AddEditDeleteAddressResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addAddressBottomSheetdialogCoordinatorlayout,
                    throwable.message.toString(),
                    requireContext())
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
package com.heena.supplier.bottomsheets

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.CountryListingAdapter
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.custom.FetchPath
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.activity_sign_up2.cards_countries_listing
import kotlinx.android.synthetic.main.activity_sign_up2.rv_countries_listing
import kotlinx.android.synthetic.main.activity_sign_up2.tv_emirate
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.et_title
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.iv_toggle_off
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.iv_toggle_on
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.set_as_default_layout
import kotlinx.android.synthetic.main.fragment_add_address_bottom_sheet.view.title_layout
import kotlinx.android.synthetic.main.fragment_add_new_service.view.*
import kotlinx.android.synthetic.main.fragment_add_new_service.view.tv_location
import kotlinx.android.synthetic.main.fragment_address_sheet.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AddAddressBottomSheetFragment : BottomSheetDialogFragment(){
    private var mView: View?=null
    private var flat_villa: String ?=null
    private var building_name : String?=null
    private var title : String?=null
    private var street_area : String?=null
    private var stateName : String?=null
    private var is_default = 0
    lateinit var countryListingAdapter: CountryListingAdapter
    private var selectedCountry : String?=null
    private var is_set_default = false
    var myStatus = 0
    private var selectedEmirates : String?=null
    var AUTOCOMPLETE_REQUEST_CODE: Int = 500
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    private var emiratesId : Int?=null
    var countryName: String = ""
    private var current_latitude = 0.0
    private var current_longitude = 0.0
    private var strAddress = ""
    private var strCity = ""
    private var emiratesList = ArrayList<Emirates>()
    private val PERMISSIONS_2 = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    var my_click = ""
    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }
            if(allAreGranted) {
                Log.e("Granted", "Permissions")
                val fields: MutableList<Place.Field> = java.util.ArrayList()
                fields.add(Place.Field.NAME)
                fields.add(Place.Field.ID)
                fields.add(Place.Field.LAT_LNG)
                fields.add(Place.Field.ADDRESS)
                fields.add(Place.Field.ADDRESS_COMPONENTS)
                myStatus = AUTOCOMPLETE_REQUEST_CODE
                // Start the autocomplete intent.
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(
                    requireContext()
                )
                resultLauncher.launch(intent)
            }else{
                Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.please_allow_permissions),
                    requireContext())
                Log.e("Denied", "Permissions")
            }
        }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
         if (myStatus.equals(AUTOCOMPLETE_REQUEST_CODE)){
            if (it.resultCode== Activity.RESULT_OK){
                val place = Autocomplete.getPlaceFromIntent(it.data!!)
                val latlng = place.latLng
                mLatitude = latlng!!.latitude
                mLongitude = latlng.longitude
                val addressComponents: MutableList<AddressComponent> = place.addressComponents!!.asList()
                var ard = 0
                var add = ""
                for (addressComponent in addressComponents) {
                    Log.e("addressss--", addressComponent.name)
                    countryName = addressComponent.name
                    if (ard == 0) {
                        add = addressComponent.name
                    }

                    var flag = false
                    val types: MutableList<String> = addressComponent.types
                    for (type in types) {
                        if (type.equals(
                                "locality",
                                true
                            ) || type.equals("administrative_area_level_2") || type.equals(
                                "administrative_area_level_1",
                                true
                            )
                        ) {
                            flag = true
                        }
                    }
                    if (flag) {
                        val center = LatLng(mLatitude, mLongitude)
                        val locality = getLocality(center)
                        val countryName = getCountry(center)

                        var address = ""

                        if (!add.isEmpty()) {
                            address = add
                        }
                        if (!locality.isEmpty() && !address.isEmpty() && !address.equals(locality)) {
                            address = address + ", " + locality
                        }
                        if (!addressComponent.name.isEmpty() && !address.isEmpty() && !address.equals(addressComponent.name)) {
                            address = address + ", " + addressComponent.name
                        }
                        if (!countryName.isEmpty() && !address.isEmpty()) {
                            address = address + ", " + countryName
                        }
                        mView!!.et_search_address.setText(address)
                        break
                    }
                    ard++
                }
            }
        }
    }

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

        mView!!.et_search_address.setOnClickListener {
            mView!!.et_search_address.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "location"
            activityResultLauncher.launch(PERMISSIONS_2)
        }
        checkSearchAddressField()
    }

    private fun checkSearchAddressField() {
        var myLocation = mView!!.et_search_address.text.toString().trim()
        if (myLocation.isEmpty()){
            Utility.showSnackBarValidationError(addAddressBottomSheetdialogCoordinatorlayout,requireContext().getString(R.string.please_select_the_address_first),requireContext());
            mView!!.flat_villa_layout.isEnabled = false
            mView!!.building_name_layout.isEnabled = false
            mView!!.title_layout.isEnabled = false
            mView!!.street_area_layout.isEnabled = false
            mView!!.emirate_layout.isEnabled = false
            mView!!.set_as_default_layout.isEnabled = false
        }else{
            mView!!.flat_villa_layout.isEnabled = true
            mView!!.building_name_layout.isEnabled = true
            mView!!.title_layout.isEnabled = true
            mView!!.street_area_layout.isEnabled = true
            mView!!.emirate_layout.isEnabled = true
            mView!!.set_as_default_layout.isEnabled = true

            setAddress(LatLng(mLatitude, mLongitude))

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
                            mLatitude = addressItem.lat!!
                            mLongitude = addressItem.langt!!
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
        val edit_address_builder = APIClient.createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "address_id", "lang", "lat", "langt"),
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


    private fun getLocality(latLng: LatLng): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            var subLocality = ""
            if (!addresses[0].subLocality.isNullOrEmpty()) {
                subLocality = addresses[0].subLocality
            } else {
                if (!addresses[0].locality.isNullOrEmpty()) {
                    subLocality = addresses[0].locality
                }
            }
            return subLocality
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


    private fun getCountry(latLng: LatLng): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            var subLocality = ""
            if (!addresses[0].countryName.isNullOrEmpty()) {
                subLocality = addresses[0].countryName
            }
            return subLocality
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun setAddress(maplatLng: LatLng?) {
        val geocoder = Geocoder(requireContext())
        try {
            val addressList = geocoder.getFromLocation(maplatLng!!.latitude, maplatLng.longitude, 1)
            if (addressList != null && addressList.size > 0) {
                val locality = addressList[0].getAddressLine(0)
                val country = addressList[0].countryName
                if (locality != null && country != null)
                {
                    current_latitude = addressList[0].latitude
                    current_longitude = addressList[0].longitude
                    Log.e("cureent_lati", current_latitude.toString())
                    Log.e("cureent_longni", current_longitude.toString())
                    strAddress = addressList[0].getAddressLine(0)
                    Log.e("tesr", "" + strAddress)
                }
                if (addressList[0].locality != null) {
                    flat_villa = addressList[0].featureName
                    building_name = addressList[0].featureName
                    val abc = addressList[0].adminArea + addressList[0].subAdminArea
                    val def = addressList[0].premises
                    val ghi = addressList[0].thoroughfare + addressList[0].subThoroughfare
                    val jkl = addressList[0].adminArea
                    street_area = addressList[0].subLocality +  " " + addressList[0].locality
                    countryName = addressList[0].countryName
                    stateName = addressList[0].adminArea
                    for (i in 0 until emiratesList.size){
                      /*  if (stateName!!.equals(emiratesList[i].name)||stateName!!.equals(emiratesList[i].name_ar)){
                            //countryId = emiratesList[i].countryId
                            break
                        }*/
                    }
                    Log.e("flat_villa", "" + flat_villa)
                    Log.e("country", "" + addressList[0].countryName)
                    Log.e("street_area", "" + street_area)
                    //Log.e("countryId", "" + countryId)
                    Log.e("abc", "" + abc)
                    Log.e("def", "" + def)
                    Log.e("ghi", "" + ghi)
                    Log.e("jkl", "" + jkl)
                    strCity = addressList[0].locality + addressList[0].countryCode + addressList[0].countryName
                    val addList = strAddress.split(",".toRegex()).toTypedArray()
                    Log.e("addList", "" + addList.toString())
                    strAddress = ""
                    for (s in addList) {
                        strAddress = if (strCity.equals(s.trim { it <= ' ' }, ignoreCase = true)) {
                            break
                        } else {
                            strAddress + s
                        }
                    }
                    Log.e("address ", strAddress)

                    if(addressList[0].postalCode!=null) {
                        mView!!.et_flat_villa.setText(flat_villa!!)
                        mView!!.et_building_name.setText(flat_villa!!)
                        mView!!.et_street_area.setText(street_area!!)
                        for (i in 0 until emiratesList.size){
                            if (stateName!!.equals(emiratesList[i].name)||countryName!!.equals(emiratesList[i].nameAr)){
                                emiratesId = emiratesList[i].countryId
                                break
                            }
                        }

                    }
                }
            }else{
                Log.e("err1", "err1")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
package com.heena.supplier.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.bottomsheets.AddAddressBottomSheetFragment
import com.heena.supplier.models.AddEditDeleteAddressResponse
import com.heena.supplier.models.CountryItem
import com.heena.supplier.models.CountryResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_address_sheet.*
import kotlinx.android.synthetic.main.fragment_address_sheet.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class AddressSheetFragment : Fragment(),
    OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener{

    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location?=null
    private var current_latitude = 0.0
    private var current_longitude = 0.0
    private var strAddress = ""
    private var strCity = ""
    private var flat_villa: String ?=null
    private var building_name : String?=null
    private var title : String?=null
    private var street_area : String?=null
    private var is_default : Int = 0
    private var countryList = ArrayList<CountryItem>()
    private var countryId : Int?=null
    private var countryName : String?=null
    private var is_set_default = false
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    var placeClick = false
    private var addressMap = HashMap<String, String>()
    private var direction : String = ""
    private var mView : View ?=null
    var placeAPIresultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val place = Autocomplete.getPlaceFromIntent(data!!)
            gMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, 15F))
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            direction = it.getString("direction").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_address_sheet, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        getCountires()
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            SharedPreferenceUtility.getInstance()
                .hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            SharedPreferenceUtility.getInstance()
                .hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(getString(R.string.google_map_api_key))
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
        placeClick =
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.PLACECLICK, false)

        mView!!.mapView.onCreate(mapViewBundle)
        mView!!.mapView.getMapAsync(this)


        /*mView!!.tv_location.setSafeOnClickListener {
            placeClick = true
            SharedPreferenceUtility.getInstance()
                .save(SharedPreferenceUtility.PLACECLICK, placeClick)
            val fields: MutableList<Place.Field> = ArrayList()
            fields.add(Place.Field.NAME)
            fields.add(Place.Field.ID)
            fields.add(Place.Field.LAT_LNG)
            fields.add(Place.Field.ADDRESS)
            fields.add(Place.Field.ADDRESS_COMPONENTS)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext())
            placeAPIresultLauncher.launch(intent)
        }*/

        mView!!.tv_enter_manually.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putString("status", "add")
            bundle.putInt("addressId", 0)
            bundle.putSerializable("addressMap", addressMap)
            val addAddressBottomSheetFragment =
                AddAddressBottomSheetFragment.newInstance(requireContext(), bundle)
            addAddressBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                AddAddressBottomSheetFragment.TAG
            )
        }

        mView!!.tv_submit.setSafeOnClickListener {
            for (i in 0 until countryList.size){
                if (countryName!!.equals(countryList[i].name)||countryName!!.equals(countryList[i].name_ar)){
                    countryId = countryList[i].country_id
                    break
                }
            }
            if (direction.equals("Payment Fragment")) {
                Log.e("Address", tv_location.text.toString())
                Log.e("Lat", mLatitude.toString())
                Log.e("Lng", mLongitude.toString())

                SharedPreferenceUtility.getInstance()
                    .save(SharedPreferenceUtility.SavedAddress, tv_location.text.toString())
                SharedPreferenceUtility.getInstance()
                    .save(SharedPreferenceUtility.SavedLat, mLatitude.toString())
                SharedPreferenceUtility.getInstance()
                    .save(SharedPreferenceUtility.SavedLng, mLongitude.toString())
                findNavController().popBackStack()
            } else {
                validateAndSave(countryId)
            }
        }
    }

    private fun getCountires() {
        if (Utility.isNetworkAvailable()){
            val call = Utility.apiInterface.getCountries(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
            call!!.enqueue(object : Callback<CountryResponse?> {
                override fun onResponse(call: Call<CountryResponse?>, response: Response<CountryResponse?>) {
                    try {
                        if (response.body() != null) {
                            if (response.body()!!.status==1){
                                countryList.clear()
                                countryList = response.body()!!.country as ArrayList<CountryItem>
                            }else{
                                LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                            }
                        }else {
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

    private fun validateAndSave(countryId: Int?) {
        title = mView!!.et_title.text.toString().trim()
        if(TextUtils.isEmpty(title)){
            mView!!.et_title.requestFocus()
            mView!!.et_title.error = getString(R.string.please_enter_valid_title)
        }else if (countryId==null || countryId==0){
            LogUtils.shortToast(requireContext(), requireContext().getString(R.string.no_country_found))
        }else{
            saveAddress(countryId)
        }
    }

    private fun saveAddress(country_Id: Int?) {
        val builder = APIClient.createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "user_id"),
            arrayOf(flat_villa.toString(),
                title.toString(),
                street_area.toString(),
                is_default.toString(),
                country_Id.toString(),
                building_name.toString(),
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString()))
        val call = Utility.apiInterface.addAddress(builder.build())
        call!!.enqueue(object : Callback<AddEditDeleteAddressResponse?> {
            override fun onResponse(call: Call<AddEditDeleteAddressResponse?>, response: Response<AddEditDeleteAddressResponse?>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.status == 1) {
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            findNavController().popBackStack()
                        }
                    } else {
                        LogUtils.shortToast(requireContext(), response.message())
                    }
                } else {
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<AddEditDeleteAddressResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.localizedMessage)
            }

        })
    }

    companion object {
        const val TAG = "AddressBottomSheetFragment"
        private var instance: SharedPreferenceUtility? = null
        private var gMap : GoogleMap?=null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Locale.setDefault(Locale.ENGLISH)
        gMap = googleMap
        gMap!!.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        gMap!!.getUiSettings().setCompassEnabled(true)
        gMap!!.uiSettings.isMapToolbarEnabled = true;
        gMap!!.uiSettings.isMyLocationButtonEnabled = true;
        fetchCurrentLocation()


    }

    fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }else{
            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
                    gMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    mView!!.mapView.onResume()
                    gMap!!.setOnCameraIdleListener(this)
                }else{
                    Log.e("Location", "" +location)
                }
            }
        }

    }

    override fun onCameraIdle() {
        val mapLatLng = gMap!!.cameraPosition.target
        setAddress(mapLatLng)
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
                    street_area = addressList[0].subLocality +  " " + addressList[0].locality
                    countryName = addressList[0].countryName
                    for (i in 0 until countryList.size){
                        if (countryName!!.equals(countryList[i].name)||countryName!!.equals(countryList[i].name_ar)){
                            countryId = countryList[i].country_id
                            break
                        }
                    }
                    Log.e("flat_villa", "" + flat_villa)
                    Log.e("country", "" + addressList[0].countryName)
                    Log.e("street_area", "" + street_area)
                    Log.e("countryId", "" + countryId)
                    Log.e("abc", "" + abc)
                    Log.e("def", "" + def)
                    Log.e("ghi", "" + ghi)
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
                        addressMap.put("flat_villa", flat_villa!!)
                        addressMap.put("street_area", street_area!!)
                        addressMap.put("country", countryName!!)
                        for (i in 0 until countryList.size){
                            if (countryName!!.equals(countryList[i].name)||countryName!!.equals(countryList[i].name_ar)){
                                countryId = countryList[i].country_id
                                break
                            }
                        }
                        addressMap.put("countryId", countryId.toString())
                    }
                    else
                    {
                        addressMap.put("flat_villa", flat_villa!!)
                        addressMap.put("street_area", street_area!!)
                        addressMap.put("country", countryName!!)
                    }
                    mView!!.tv_location.text = strAddress
                }else{
                    strAddress = ""
                    mView!!.tv_location.text = strAddress
                }
            }else{
                Log.e("err1", "err1")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
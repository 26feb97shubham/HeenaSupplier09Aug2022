package com.dev.heenasupplier.fragments

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
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.CountryListingAdapter
import com.dev.heenasupplier.bottomsheets.AddAddressBottomSheetFragment
import com.dev.heenasupplier.models.AddEditDeleteAddressResponse
import com.dev.heenasupplier.models.CountryItem
import com.dev.heenasupplier.models.CountryResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.fragment_address_sheet.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddressSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddressSheetFragment : Fragment(),
    OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener,
    CompoundButton.OnCheckedChangeListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
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
    lateinit var countryListingAdapter: CountryListingAdapter
    private var selectedCountry : String?=null
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
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fetchCurrentLocation()

        //requireActivity().tv_title.text = ""

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            SharedPreferenceUtility.getInstance()
                .hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            SharedPreferenceUtility.getInstance()
                .hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(getString(R.string.google_map_api_key))
        }

        getCountires()

        switchDefault1.setOnCheckedChangeListener(this)

        iv_toggle_off.setOnClickListener {
            is_set_default = true
            is_default = 0
            iv_toggle_off.visibility = View.GONE
            iv_toggle_on.visibility = View.VISIBLE
        }

        iv_toggle_on.setOnClickListener {
            is_set_default = false
            is_default = 1
            iv_toggle_off.visibility = View.VISIBLE
            iv_toggle_on.visibility = View.GONE
        }
        placeClick =
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.PLACECLICK, false)

        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)


        tv_location.setOnClickListener {
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
        }

        switchDefault1.setOnCheckedChangeListener(this)

        tv_enter_manually.setOnClickListener {
//            dismiss()
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

        tv_submit.setOnClickListener {

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
                validateAndSave()
            }
        }
    }

    private fun getCountires() {
        if (Utility.isNetworkAvailable()){
            val call = Utility.apiInterface.getCountries()
            call!!.enqueue(object : Callback<CountryResponse?> {
                override fun onResponse(call: Call<CountryResponse?>, response: Response<CountryResponse?>) {
                    try {
                        if (response.body() != null) {
                            if (response.body()!!.status==1){
                                cards_countries_listing.visibility = View.VISIBLE
                                rv_countries_listing.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                                countryList = response.body()!!.country as java.util.ArrayList<CountryItem>
                                countryListingAdapter = CountryListingAdapter(requireContext(), countryList, object :  ClickInterface.OnRecyclerItemClick{
                                    override fun OnClickAction(position: Int) {
                                        tv_emirate.text = countryList[position].name
                                        selectedCountry = tv_emirate.text.toString().trim()
                                        cards_countries_listing.visibility = View.GONE
                                        countryId = returnCountryId(selectedCountry!!, countryList)
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

    private fun validateAndSave() {
        title = et_title.text.toString().trim()

        if(TextUtils.isEmpty(title)){
            et_title.requestFocus()
            et_title.error = getString(R.string.please_enter_valid_title)
        }else{
            saveAddress()
        }
    }

    private fun saveAddress() {
        val builder = APIClient.createBuilder(arrayOf("flat", "title", "street", "is_default", "country_id", "building_name", "user_id"),
            arrayOf(flat_villa.toString(),
                title.toString(),
                street_area.toString(),
                is_default.toString(),
                countryId.toString(),
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
        private var onAddressCallback : ClickInterface.OnAddressClick?=null
        private var instance: SharedPreferenceUtility? = null
        private var gMap : GoogleMap?=null
        fun newInstance(context: Context?, onAddressCallback: ClickInterface.OnAddressClick): AddressSheetFragment {
            this.onAddressCallback = onAddressCallback
            return AddressSheetFragment()
        }
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap!!.setMinZoomPreference(12F)
        gMap!!.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        gMap!!.getUiSettings().setCompassEnabled(true)
        gMap!!.uiSettings.isMapToolbarEnabled = true;
        gMap!!.uiSettings.isMyLocationButtonEnabled = true;
        fetchCurrentLocation()

        val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        gMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
        mapView.onResume()
        gMap!!.setOnCameraIdleListener(this)
    }

    fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                lastLocation = location
            }else{
                Log.e("Location", "" +location)
            }
        }
    }

    override fun onCameraIdle() {
        val mapLatLng = gMap!!.cameraPosition.target
        setAddress(mapLatLng)
    }

    private fun returnCountryId(selectedCountry: String, countryList: ArrayList<CountryItem>): Int? {
        for (country : CountryItem in countryList) {
            if (country.name.equals(selectedCountry.toLowerCase())) {
                return country.country_id
            }
        }
        return null
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
//                    flat_villa = addressList[0].getAddressLine(1)
                    Log.e("tesr", "" + strAddress)
//                    Log.e("flat_villa", "" + flat_villa)
                }
                if (addressList[0].locality != null) {
                    flat_villa = addressList[0].featureName
                    building_name = addressList[0].featureName
                    val abc = addressList[0].adminArea + addressList[0].subAdminArea
                    val def = addressList[0].premises
                    val ghi = addressList[0].thoroughfare + addressList[0].subThoroughfare
                    street_area = addressList[0].subLocality +  " " + addressList[0].locality
                    countryName = addressList[0].countryName
                    countryId = returnCountryId(countryName!!, countryList)
                    Log.e("flat_villa", "" + flat_villa)
                    Log.e("country", "" + addressList[0].countryName)
                    Log.e("street_area", "" + street_area)
                    Log.e("countryId", "" + countryId)
                    Log.e("abc", "" + abc)
                    Log.e("def", "" + def)
                    Log.e("ghi", "" + ghi)
                    strCity = addressList[0].locality + addressList[0].countryCode + addressList[0].countryName
                    val addList = strAddress.split(",".toRegex()).toTypedArray()
//                    val addList = strAddress.split(",".toRegex(), 5)
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
                        addressMap.put("countryId", countryId!!.toString())
                    }
                    else
                    {
                        addressMap.put("flat_villa", flat_villa!!)
                        addressMap.put("street_area", street_area!!)
                        addressMap.put("country", countryName!!)
                    }
                    tv_location.text = strAddress
                }else{
                    strAddress = ""
                    tv_location.text = strAddress
                }
            }else{
                Log.e("err1", "err1")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.switchDefault1 -> {
                if (isChecked) {
                    is_default = 1
                } else {
                    is_default = 0
                }
            }
        }
    }
}
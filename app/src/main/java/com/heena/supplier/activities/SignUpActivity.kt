package com.heena.supplier.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heena.supplier.BuildConfig
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.CountryListingAdapter
import com.heena.supplier.broadcastreceiver.ConnectivityReceiver
import com.heena.supplier.custom.FetchPath
import com.heena.supplier.models.CountryItem
import com.heena.supplier.models.CountryResponse
import com.heena.supplier.models.SignUpResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.ConstClass.EMAILADDRESS
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.supplier.utils.Utility.IMAGE_DIRECTORY_NAME
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.isCharacterAllowed
import com.heena.supplier.utils.Utility.isNetworkAvailable
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_sign_up2.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    var username: String = ""
    private var mobilenumber: String = ""
    var emailaddress: String = ""
    private var emirates : String?= ""
    var location : String? = ""
    var password: String = ""
    private var confirmPassword: String = ""
    private val PERMISSIONS_1 = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val PERMISSIONS_2 = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    private var uri: Uri? = null
    private val MEDIA_TYPE_IMAGE = 1
    val PICK_IMAGE_FROM_GALLERY = 10
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var imagePath = ""
    private var countryList = ArrayList<CountryItem>()
    var AUTOCOMPLETE_REQUEST_CODE: Int = 500
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    var countryName: String = ""
    var myuserId : Int = 0
    private val mContext: SignUpActivity = this
    lateinit var countryListingAdapter: CountryListingAdapter
    private var selectedCountry : String?=null
    private var countryId : Int?=null
    var show_cnfrm_pass = false
    var show_pass = false
    var status = 0
    var my_click = ""
    var emiratesClick = false
    var isChecked: Boolean=false
    private var networkChangeReceiver: ConnectivityReceiver? = null

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
            registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()) { result ->
                var allAreGranted = true
                for(b in result.values) {
                    allAreGranted = allAreGranted && b
                }
                if(allAreGranted) {
                    Log.e("Granted", "Permissions")
                    if (my_click.equals("profile")){
                        openCameraDialog()
                    }else{
                        val fields: MutableList<Place.Field> = ArrayList()
                        fields.add(Place.Field.NAME)
                        fields.add(Place.Field.ID)
                        fields.add(Place.Field.LAT_LNG)
                        fields.add(Place.Field.ADDRESS)
                        fields.add(Place.Field.ADDRESS_COMPONENTS)
                        status = AUTOCOMPLETE_REQUEST_CODE
                        // Start the autocomplete intent.
                        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(
                                this
                        )
                        resultLauncher.launch(intent)
                    }
                }else{
                    LogUtils.shortToast(this, getString(R.string.please_allow_permissions))
                    Log.e("Denied", "Permissions")
                }
            }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (status.equals(CAMERA_CAPTURE_IMAGE_REQUEST_CODE)){
            if (it.resultCode == Activity.RESULT_OK){
                if (uri != null) {
                    imagePath = ""
                    Log.e("uri", uri.toString())
                    imagePath = uri!!.path!!
                    Log.e("image_path", imagePath)
                    Glide.with(this).load("file:///$imagePath").placeholder(R.drawable.user).into(civ_profile)
                } else {
                    LogUtils.shortToast(this, "something went wrong! please try again")
                }
            }
        }else if (status.equals(PICK_IMAGE_FROM_GALLERY)){
            if (it.resultCode==Activity.RESULT_OK){
                val data: Intent? = it.data
                if (data!!.data != null) {
                    imagePath = ""
                    val uri = data.data
                    imagePath = if (uri.toString().startsWith("content")) {
                        FetchPath.getPath(this, uri!!)!!
                    } else {
                        uri!!.path!!
                    }
                    Log.e("image_path", imagePath)
                    Glide.with(this).applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.user)).load("file:///$imagePath").into(civ_profile)
                }
            }
        }else if (status.equals(AUTOCOMPLETE_REQUEST_CODE)){
            if (it.resultCode==Activity.RESULT_OK){
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
                        edtlocation_signup.text = address
                        break
                    }
                    ard++
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)
        tv_login.setText(getString(R.string.log_in))
        networkChangeReceiver = ConnectivityReceiver()
        networkChangeReceiver!!.NetworkChangeReceiver(this)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
        setUpViews()
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
                                rv_countries_listing.layoutManager = LinearLayoutManager(this@SignUpActivity, LinearLayoutManager.VERTICAL, false)
                                countryList = response.body()!!.country as ArrayList<CountryItem>
                                countryListingAdapter = CountryListingAdapter(this@SignUpActivity, countryList, object :  ClickInterface.OnRecyclerItemClick{
                                    override fun OnClickAction(position: Int) {
                                        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "").equals("ar")){
                                            tv_emirate.text = countryList[position].name_ar
                                        }else{
                                            tv_emirate.text = countryList[position].name
                                        }
                                        selectedCountry = tv_emirate.text.toString().trim()
                                        countryId = countryList[position].country_id
                                        Log.e("countryId", countryId.toString())
                                        cards_countries_listing.visibility = View.GONE
                                    }
                                })
                                rv_countries_listing.adapter = countryListingAdapter
                            }else{
                                cards_countries_listing.visibility = View.GONE
                                LogUtils.shortToast(this@SignUpActivity, getString(R.string.response_isnt_successful))
                            }
                        }else {
                            cards_countries_listing.visibility = View.GONE
                            LogUtils.shortToast(this@SignUpActivity, getString(R.string.response_isnt_successful))
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
                    LogUtils.shortToast(this@SignUpActivity,throwable.message)
                }
            })
        }else{
            LogUtils.shortToast(this, getString(R.string.check_internet))
        }

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpViews() {
        btnSignUp.setSafeOnClickListener {
            btnSignUp.startAnimation(AlphaAnimation(1f, 0.5f))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this, btnSignUp)
            if(!Utility.hasConnection(this)){
                val noInternetDialog = NoInternetDialog()
                noInternetDialog.isCancelable = false
                noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                    override fun retry() {
                        noInternetDialog.dismiss()
                        validateAndSignUp()
                    }

                })
                noInternetDialog.show(supportFragmentManager, "Login Activity")
            }
            validateAndSignUp()
        }

        tv_login.setSafeOnClickListener {
            tv_login.startAnimation(AlphaAnimation(1f,0.5f))
            startActivity(Intent(this, LoginActivity::class.java))
        }

        scrollView.setOnTouchListener(object : View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                edtUsername_signup.clearFocus()
                edtemailaddress_signup.clearFocus()
                edtmobilenumber_signup.clearFocus()
                edtpassword_signup.clearFocus()
                edtcnfrmpassword_signup.clearFocus()
                return false
            }

        })
        edtcnfrmpassword_signup.doOnTextChanged { text, start, before, count ->
            val pass =edtpassword_signup.text.toString()

            if(!TextUtils.isEmpty(pass)){
                if(!pass.equals(text.toString(), false)){
                    edtcnfrmpassword_signup.error=getString(R.string.password_doesnt_match_with_verify_password)
                }
            }
            else{
                edtpassword_signup.error=getString(R.string.please_first_enter_your_password)
            }

        }

        iv_cnfrm_pass_show_hide.setOnClickListener {
            if (show_cnfrm_pass){
                show_cnfrm_pass = false
                edtcnfrmpassword_signup.transformationMethod = null
                iv_cnfrm_pass_show_hide.setImageResource(R.drawable.visible)
            }else{
                show_cnfrm_pass = true
                edtpassword_signup.transformationMethod = PasswordTransformationMethod()
                iv_cnfrm_pass_show_hide.setImageResource(R.drawable.invisible)
            }
        }

        iv_pass_show_hide.setOnClickListener {
            if (show_pass){
                show_pass = false
                edtpassword_signup.transformationMethod = null
                iv_pass_show_hide.setImageResource(R.drawable.visible)
            }else{
                show_pass = true
                edtpassword_signup.transformationMethod = PasswordTransformationMethod()
                iv_pass_show_hide.setImageResource(R.drawable.invisible)
            }
        }

        civ_profile.setOnClickListener {
            civ_profile.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "profile"
            activityResultLauncher.launch(PERMISSIONS_1)
        }

        edtlocation_signup.setSafeOnClickListener {
            edtlocation_signup.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "location"
            activityResultLauncher.launch(PERMISSIONS_2)
        }

        tv_emirate.setSafeOnClickListener {
            if (!emiratesClick){
                emiratesClick = true

                getCountires()


            }else{
                emiratesClick = false
                cards_countries_listing.visibility = View.GONE
            }
        }

        imgChk.setOnClickListener {
            imgChk.startAnimation(AlphaAnimation(1f, 0.5f))
            if(isChecked){
                isChecked=false
                imgChk.setImageResource(R.drawable.un_check)
            }
            else{
                isChecked=true
                imgChk.setImageResource(R.drawable.check)
            }
        }

        txtTermsConditions.setSafeOnClickListener {
            startActivity(Intent(this, TermsAndConditionsActivity::class.java))
        }
    }

    private fun openCameraDialog() {
        val items = arrayOf<CharSequence>(getString(R.string.camera), getString(R.string.gallery), getString(R.string.cancel))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.add_photo))
        builder.setItems(items) { dialogInterface, i ->
            if (items[i] == getString(R.string.camera)) {
                captureImage()
            } else if (items[i] == getString(R.string.gallery)) {
                chooseImage()
            } else if (items[i] == getString(R.string.cancel)) {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        status = CAMERA_CAPTURE_IMAGE_REQUEST_CODE
        resultLauncher.launch(intent)
    }

    fun getOutputMediaFileUri(type: Int): Uri {
        return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", getOutputMediaFile(type)!!)
    }

    private fun getOutputMediaFile(type: Int): File? {
        val mediaStorageDir = File(
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME)
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
        val mediaFile: File
        mediaFile = if (type == MEDIA_TYPE_IMAGE) {
            File(mediaStorageDir.path + File.separator
                    + "IMG_" + timeStamp + ".png")
        } else if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            File(mediaStorageDir.path + File.separator
                    + "VID_" + timeStamp + ".mp4")
        } else {
            return null
        }
        return mediaFile
    }
    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        status = PICK_IMAGE_FROM_GALLERY
        resultLauncher.launch(intent)
    }

    private fun getLocality(latLng: LatLng): String {
        val geocoder = Geocoder(mContext, Locale.getDefault())
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
        val geocoder = Geocoder(mContext, Locale.getDefault())
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


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun validateAndSignUp() {
        username = edtUsername_signup.text.toString().trim()
        mobilenumber = edtmobilenumber_signup.text.toString().trim()
        emailaddress = edtemailaddress_signup.text.toString().trim()
        emirates = tv_emirate.text.toString().trim()
        location = edtlocation_signup.text.toString().trim()
        password= edtpassword_signup.text.toString().trim()
        confirmPassword= edtcnfrmpassword_signup.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            scrollView.scrollTo(0, 150)
            edtUsername_signup.requestFocus()
            edtUsername_signup.error=getString(R.string.please_enter_your_username)
        }else if (!isCharacterAllowed(username)) {
            scrollView.scrollTo(0, 150)
            edtUsername_signup.requestFocus()
            edtUsername_signup.error=getString(R.string.emojis_are_not_allowed)
        }/*else if(!SharedPreferenceUtility.getInstance().isUserNameValid(username)) {
            scrollView.scrollTo(0, 150)
            edtUsername_signup.requestFocus()
            edtUsername_signup.error=getString(R.string.username_invalid)
        }*/else if (TextUtils.isEmpty(mobilenumber)) {
            scrollView.scrollTo(0, 180)
            edtmobilenumber_signup.requestFocus()
            edtmobilenumber_signup.error=getString(R.string.please_enter_your_phone_number)
        }else if ((mobilenumber.length < 7 || mobilenumber.length > 15)) {
            scrollView.scrollTo(0, 180)
            edtmobilenumber_signup.requestFocus()
            edtmobilenumber_signup.error=getString(R.string.mob_num_length_valid)
        }else if (TextUtils.isEmpty(emailaddress)) {
            scrollView.scrollTo(0, 210)
            edtemailaddress_signup.requestFocus()
            edtemailaddress_signup.error=getString(R.string.please_enter_valid_email)
        }else if (!SharedPreferenceUtility.getInstance().isEmailValid(emailaddress)) {
            scrollView.scrollTo(0, 210)
            edtemailaddress_signup.requestFocus()
            edtemailaddress_signup.error=getString(R.string.please_enter_valid_email)
        }else if (TextUtils.isEmpty(emirates)) {
            scrollView.scrollTo(0, 210)
            tv_emirate.requestFocus()
            tv_emirate.error=getString(R.string.please_enter_valid_emirates)
        }else if (TextUtils.isEmpty(location)) {
            scrollView.scrollTo(0, 210)
            edtlocation_signup.requestFocus()
            edtlocation_signup.error=getString(R.string.please_enter_valid_loc)
        }else if (TextUtils.isEmpty(password)) {
            edtpassword_signup.requestFocus()
            scrollView.scrollTo(0, 240)
            edtpassword_signup.error=getString(R.string.please_enter_your_password)
        }else if (password.length < 6) {
            edtpassword_signup.requestFocus()
            edtpassword_signup.error=getString(R.string.verify_password_length_valid)
        }else if (!SharedPreferenceUtility.getInstance().isPasswordValid(password)) {
            edtpassword_signup.requestFocus()
            scrollView.scrollTo(0, 240)
            edtpassword_signup.error=getString(R.string.password_length_valid)
        }else if (TextUtils.isEmpty(confirmPassword)) {
            scrollView.scrollTo(0, 270)
            edtcnfrmpassword_signup.requestFocus()
            edtcnfrmpassword_signup.error=getString(R.string.please_verify_your_password)
        }else if (!confirmPassword.equals(password)) {
            scrollView.scrollTo(0, 270)
            edtcnfrmpassword_signup.requestFocus()
            edtcnfrmpassword_signup.error=getString(R.string.password_doesnt_match_with_verify_password)
        }else if(!isChecked){
            LogUtils.shortToast(this, getString(R.string.please_accept_terms_conditions))
        }else{
            getSignUp()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getSignUp() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        progressBar.visibility= View.VISIBLE

        if (isNetworkAvailable()){
                val builder = APIClient.createMultipartBodyBuilder(arrayOf("username","country_code", "phone", "email", "password", "country_id", "lat", "long", "device_token","device_type", "lang", "address"),
                arrayOf(username.trim({ it <= ' ' }),
                        "+971",
                        mobilenumber.trim({ it <= ' ' }),
                        emailaddress.trim({it <= ' '}),
                        password.trim({it <= ' '}),
                        countryId.toString(),
                        mLatitude.toString(),
                        mLongitude.toString(),
                        SharedPreferenceUtility.getInstance()
                            .get(SharedPreferenceUtility.FCMTOKEN, "")
                            .toString(),
                        "1",
                        SharedPreferenceUtility.getInstance()
                            .get(SharedPreferenceUtility.SelectedLang, "")
                            .toString(),
                        location!!.trim({it <= ' '})))

            if (imagePath != "") {
                val file = File(imagePath)
                Log.e("file name ", file.name)
                val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
                builder!!.addFormDataPart("image", file.name, requestBody)
            }else{
                imagePath = this.getDrawable(R.drawable.dark_logo).toString()
            }

            val call = apiInterface.signUp(builder!!.build())
            call!!.enqueue(object : Callback<SignUpResponse?> {
                override fun onResponse(call: Call<SignUpResponse?>, response: Response<SignUpResponse?>) {
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if(response.isSuccessful){
                            if (response.body()!!.status==1){
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ProfilePic, response.body()!!.user!!.image)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.TOKEN, response.body()!!.token)
                                myuserId = response.body()!!.user!!.user_id!!
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserId, myuserId)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserEmail, emailaddress)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.Address, location)
                                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.Username, username)
                                LogUtils.longToast(this@SignUpActivity, response.body()!!.message)
                                startActivity(Intent(applicationContext,OtpVerificationActivity::class.java).
                                putExtra("ref", "1").
                                putExtra(EMAILADDRESS, emailaddress))
                            }else{
                                LogUtils.longToast(this@SignUpActivity, response.body()!!.message)
                            }
                        }else{
                            LogUtils.longToast(this@SignUpActivity,getString(R.string.response_isnt_successful))
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<SignUpResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    LogUtils.shortToast(this@SignUpActivity,throwable.localizedMessage)
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            })
        }else{
            LogUtils.shortToast(this@SignUpActivity, getString(R.string.check_internet))
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver)
        }
    }

    override fun onBackPressed() {
        Utility.exitApp(this, this)
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

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utility.getFCMToken()
    }
}
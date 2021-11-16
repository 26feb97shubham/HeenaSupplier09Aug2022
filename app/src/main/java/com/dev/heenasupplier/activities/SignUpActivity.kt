package com.dev.heenasupplier.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dev.heenasupplier.BuildConfig
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.CountryListingAdapter
import com.dev.heenasupplier.custom.FetchPath
import com.dev.heenasupplier.models.CountryItem
import com.dev.heenasupplier.models.CountryResponse
import com.dev.heenasupplier.models.SignUpResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.ConstClass.EMAILADDRESS
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.dev.heenasupplier.utils.Utility.Companion.IMAGE_DIRECTORY_NAME
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import com.dev.heenasupplier.utils.Utility.Companion.isNetworkAvailable
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_sign_up2.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity() {
    var username: String = ""
    var mobilenumber: String = ""
    var emailaddress: String = ""
    var emirates : String?= ""
    var location : String? = ""
    var password: String = ""
    var confirmPassword: String = ""
    private val PERMISSION_CAMERA_EXTERNAL_STORAGE_CODE = 301
    private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var uri: Uri? = null
    val MEDIA_TYPE_IMAGE = 1
    val PICK_IMAGE_FROM_GALLERY = 10
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var imagePath = ""
    private var message : String = ""
    private var selectCountryCode = ""
    private var countryCodes=ArrayList<String>()
    var cCodeList= arrayListOf<String>()
    private var countryList = ArrayList<CountryItem>()
    lateinit var adp: ArrayAdapter<String>
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)
        tv_login.setText(getString(R.string.log_in))
        setUpViews()
    }

    private fun getCountires() {
        if (isNetworkAvailable()){
            val call = apiInterface.getCountries()
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

    private fun returnCountryId(selectedCountry: String, countryList: ArrayList<CountryItem>): Int? {
        for (country : CountryItem in countryList) {
            if (country.name.equals(selectedCountry)) {
                return country.country_id
            }
        }
        return null
    }


    private fun setUpViews() {
        btnSignUp.setOnClickListener {
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

/*            startActivity(Intent(applicationContext,MembershipRegistrationActivity::class.java).putExtra("emailaddress", emailaddress))
            finishAffinity()*/
        }

        tv_login.setOnClickListener {
            tv_login.startAnimation(AlphaAnimation(1f,0.5f))
            startActivity(Intent(this, LoginActivity::class.java))
        }

        scrollView.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                edtUsername_signup.clearFocus()
                edtemailaddress_signup.clearFocus()
                edtmobilenumber_signup.clearFocus()
                edtpassword_signup.clearFocus()
                edtcnfrmpassword_signup.clearFocus()
                return false
            }

        })

        edtcnfrmpassword_signup.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val pass =edtpassword_signup.text.toString()

                if(!TextUtils.isEmpty(pass)){
                    if(!pass.equals(charSeq.toString(), false)){
                        edtcnfrmpassword_signup.error=getString(R.string.password_doesnt_match_with_verify_password)
                    }
                }
                else{
                    edtpassword_signup.error=getString(R.string.please_first_enter_your_password)
                }

            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

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
            requestToUploadProfilePhoto()
        }

        edtlocation_signup.setOnClickListener {
            val fields: MutableList<Place.Field> = ArrayList()
            fields.add(Place.Field.NAME)
            fields.add(Place.Field.ID)
            fields.add(Place.Field.LAT_LNG)
            fields.add(Place.Field.ADDRESS)
            fields.add(Place.Field.ADDRESS_COMPONENTS)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        tv_emirate.setOnClickListener {
            getCountires()
        }
    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }


    private fun requestToUploadProfilePhoto() {
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CAMERA_EXTERNAL_STORAGE_CODE)
        } else if (hasPermissions(this, *PERMISSIONS)) {
            openCameraDialog()
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
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
    }

    fun getOutputMediaFileUri(type: Int): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.toString() + ".provider", getOutputMediaFile(type)!!)
        } else {
            Uri.fromFile(getOutputMediaFile(type))
        }
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
        startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY)
    }

    fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA_EXTERNAL_STORAGE_CODE) {
            if (grantResults.size > 0) { /*  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {*/
                if (hasAllPermissionsGranted(grantResults)) {
                    openCameraDialog()
                } else {
                    LogUtils.shortToast(this, "Please grant both Camera and Storage permissions")

                }
            } else if (!hasAllPermissionsGranted(grantResults)) {
                LogUtils.shortToast(this, "Please grant both Camera and Storage permissions")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) { //previewCapturedImage();
                if (uri != null) {
                    imagePath = ""
                    Log.e("uri", uri.toString())
                    imagePath = uri!!.path!!
                    Glide.with(this).load("file:///$imagePath").placeholder(R.drawable.user).into(civ_profile)
                } else {
                    LogUtils.shortToast(this, "something went wrong! please try again")
                }
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            if (data.data != null) {
                imagePath = ""
                val uri = data.data
                imagePath = if (uri.toString().startsWith("content")) {
                    FetchPath.getPath(this, uri!!)!!
                } else {
                    uri!!.path!!
                }
                Glide.with(this).applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.user)).load("file:///$imagePath").into(civ_profile)
            }
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE && null != data) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
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

                    var flag: Boolean = false
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
        if (mContext != null) {
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
        }
        return ""
    }


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
            LogUtils.shortToast(this, getString(R.string.please_enter_your_username))
        }else if (!isCharacterAllowed(username)) {
            scrollView.scrollTo(0, 150)
            edtUsername_signup.requestFocus()
            edtUsername_signup.error=getString(R.string.emojis_are_not_allowed)
              LogUtils.shortToast(this, getString(R.string.emojis_are_not_allowed))
        }else if(!SharedPreferenceUtility.getInstance().isUserNameValid(username)) {
            scrollView.scrollTo(0, 150)
            edtUsername_signup.requestFocus()
            edtUsername_signup.error=getString(R.string.username_invalid)
            LogUtils.shortToast(this, getString(R.string.username_invalid))
        }else if (TextUtils.isEmpty(mobilenumber)) {
            scrollView.scrollTo(0, 180)
            edtmobilenumber_signup.requestFocus()
            edtmobilenumber_signup.error=getString(R.string.please_enter_your_phone_number)
             LogUtils.shortToast(this, getString(R.string.please_enter_your_phone_number))

        }else if ((mobilenumber.length < 7 || mobilenumber.length > 15)) {
            scrollView.scrollTo(0, 180)
            edtmobilenumber_signup.requestFocus()
            edtmobilenumber_signup.error=getString(R.string.mob_num_length_valid)
             LogUtils.shortToast(this, getString(R.string.mob_num_length_valid))
        }else if (TextUtils.isEmpty(emailaddress)) {
            scrollView.scrollTo(0, 210)
            edtemailaddress_signup.requestFocus()
            edtemailaddress_signup.error=getString(R.string.please_enter_valid_email)
            LogUtils.shortToast(this, getString(R.string.please_enter_valid_email))
        }else if (!SharedPreferenceUtility.getInstance().isEmailValid(emailaddress)) {
            scrollView.scrollTo(0, 210)
            edtemailaddress_signup.requestFocus()
            edtemailaddress_signup.error=getString(R.string.please_enter_valid_email)
            LogUtils.shortToast(this, getString(R.string.please_enter_valid_email))
        }else if (TextUtils.isEmpty(emirates)) {
            scrollView.scrollTo(0, 210)
            tv_emirate.requestFocus()
            tv_emirate.error=getString(R.string.please_enter_valid_emirates)
            LogUtils.shortToast(this, getString(R.string.please_enter_valid_emirates))
        }else if (TextUtils.isEmpty(location)) {
            scrollView.scrollTo(0, 210)
            edtlocation_signup.requestFocus()
            edtlocation_signup.error=getString(R.string.please_enter_valid_loc)
            LogUtils.shortToast(this, getString(R.string.please_enter_valid_loc))
        }else if (TextUtils.isEmpty(password)) {
            edtpassword_signup.requestFocus()
            scrollView.scrollTo(0, 240)
            edtpassword_signup.error=getString(R.string.please_enter_your_password)
            LogUtils.shortToast(this, getString(R.string.please_enter_your_password))
        }else if (password.length < 6) {
            edtpassword_signup.requestFocus()
            edtpassword_signup.error=getString(R.string.verify_password_length_valid)
                        LogUtils.shortToast(this, getString(R.string.verify_password_length_valid))
        }else if (!SharedPreferenceUtility.getInstance().isPasswordValid(password)) {
            edtpassword_signup.requestFocus()
            scrollView.scrollTo(0, 240)
            edtpassword_signup.error=getString(R.string.password_length_valid)
            LogUtils.shortToast(this, getString(R.string.password_length_valid))
        }else if (TextUtils.isEmpty(confirmPassword)) {
            scrollView.scrollTo(0, 270)
            edtcnfrmpassword_signup.requestFocus()
            edtcnfrmpassword_signup.error=getString(R.string.please_verify_your_password)
            LogUtils.shortToast(this, getString(R.string.please_verify_your_password))
        }else if (!confirmPassword.equals(password)) {
            scrollView.scrollTo(0, 270)
            edtcnfrmpassword_signup.requestFocus()
            edtcnfrmpassword_signup.error=getString(R.string.password_doesnt_match_with_verify_password)
            LogUtils.shortToast(this, getString(R.string.password_doesnt_match_with_verify_password))
        }else{
            getSignUp()
        }
    }

    private fun getSignUp() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        progressBar.visibility= View.VISIBLE

        if (isNetworkAvailable()){
            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
            val builder = APIClient.createMultipartBodyBuilder(arrayOf("username", "phone", "email", "password", "country_id", "lat", "long", "device_token", "lang", "address"),
                arrayOf(username.trim({ it <= ' ' }),
                        mobilenumber.trim({ it <= ' ' }),
                        emailaddress.trim({it <= ' '}),
                        password.trim({it <= ' '}),
                        countryId.toString(),
                        mLatitude.toString(),
                        mLongitude.toString(),
                        SharedPreferenceUtility.getInstance()
                            .get(SharedPreferenceUtility.FCMTOKEN, "")
                            .toString(),
                        SharedPreferenceUtility.getInstance()
                            .get(SharedPreferenceUtility.SelectedLang, "")
                            .toString(),
                        location!!.trim({it <= ' '})))

            if (imagePath != "") {
                val file = File(imagePath)
                Log.e("file name ", file.name)
                val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
                builder!!.addFormDataPart("image", file.name, requestBody)
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
                                LogUtils.longToast(this@SignUpActivity, response.body()!!.message)
                                startActivity(Intent(applicationContext,OtpVerificationActivity::class.java).putExtra("ref", "1").putExtra(EMAILADDRESS, emailaddress))
                                finishAffinity()
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

    private fun isCharacterAllowed(validateString: String): Boolean {
        var containsInvalidChar = false
        for (i in 0 until validateString.length) {
            val type = Character.getType(validateString[i])
            containsInvalidChar = !(type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt())
        }
        return containsInvalidChar
    }
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm!!.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
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
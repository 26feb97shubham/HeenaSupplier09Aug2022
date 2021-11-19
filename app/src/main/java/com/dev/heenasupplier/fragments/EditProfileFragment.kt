package com.dev.heenasupplier.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dev.heenasupplier.BuildConfig
import com.dev.heenasupplier.R
import com.dev.heenasupplier.custom.FetchPath
import com.dev.heenasupplier.models.ProfileShowResponse
import com.dev.heenasupplier.models.UpdateProfileResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.dev.heenasupplier.utils.Utility.Companion.IMAGE_DIRECTORY_NAME
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import kotlinx.android.synthetic.main.activity_sign_up2.view.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mView : View? = null
    private val PERMISSION_CAMERA_EXTERNAL_STORAGE_CODE = 301
    private val PERMISSIONS_1 = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val PERMISSIONS_2 = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    private var uri: Uri? = null
    val MEDIA_TYPE_IMAGE = 1
    val PICK_IMAGE_FROM_GALLERY = 10
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var imagePath = ""
    private var name = ""
    var profile_picture:String=""
    var username: String = ""
    var fullname: String = ""
    var mobilenumber: String = ""
    var emailaddress: String = ""
    var location : String? = ""
    var password: String = ""
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    var AUTOCOMPLETE_REQUEST_CODE: Int = 500
    var countryName: String = ""
    var profile_pic_changed = false
    var status = 0
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
                    if (my_click.equals("profile")){
                        openCameraDialog()
                    }else{
                        val fields: MutableList<Place.Field> = java.util.ArrayList()
                        fields.add(Place.Field.NAME)
                        fields.add(Place.Field.ID)
                        fields.add(Place.Field.LAT_LNG)
                        fields.add(Place.Field.ADDRESS)
                        fields.add(Place.Field.ADDRESS_COMPONENTS)
                        status = AUTOCOMPLETE_REQUEST_CODE
                        // Start the autocomplete intent.
                        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(
                                requireContext()
                        )
                        resultLauncher.launch(intent)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.please_allow_permissions))
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
                    Glide.with(this).load("file:///$imagePath").placeholder(R.drawable.user).into(mView!!.civ_profile_update)
                } else {
                    LogUtils.shortToast(requireContext(), "something went wrong! please try again")
                }
            }
        }else if (status.equals(PICK_IMAGE_FROM_GALLERY)){
            if (it.resultCode==Activity.RESULT_OK){
                val data: Intent? = it.data
                if (data!!.data != null) {
                    imagePath = ""
                    val uri = data.data
                    imagePath = if (uri.toString().startsWith("content")) {
                        FetchPath.getPath(requireContext(), uri!!)!!
                    } else {
                        uri!!.path!!
                    }
                    Log.e("image_path", imagePath)
                    Glide.with(this).applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.user)).load("file:///$imagePath").into(mView!!.civ_profile_update)
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
                        mView!!.edtlocation_update.text = address
                        break
                    }
                    ard++
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        showProfile()

        mView!!.btnUpdate.setOnClickListener {
            mView!!.btnUpdate.startAnimation(AlphaAnimation(1f, 0.5f))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), mView!!.btnUpdate)
            validateAndUpdate()
        }


        mView!!.scrollViewUpdate.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                mView!!.edtUsername_update.clearFocus()
                mView!!.edtemailaddress_update.clearFocus()
                mView!!.edtlocation_update.clearFocus()
                mView!!.edtmobilenumber_update.clearFocus()
                mView!!.edtFullName_update.clearFocus()
                return false
            }

        })

        mView!!.editProfileUpdate.setOnClickListener {
            profile_pic_changed = true
            mView!!.editProfileUpdate.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "profile"
            activityResultLauncher.launch(PERMISSIONS_1)
        }

        mView!!.edtlocation_update.setOnClickListener {
            mView!!.edtlocation_update.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "location"
            activityResultLauncher.launch(PERMISSIONS_2)
        }
    }

    private fun validateAndUpdate() {
        username = mView!!.edtUsername_update.text.toString().trim()
        fullname = mView!!.edtFullName_update.text.toString().trim()
        mobilenumber = mView!!.edtmobilenumber_update.text.toString().trim()
        emailaddress = mView!!.edtemailaddress_update.text.toString().trim()
        location = mView!!.edtlocation_update.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            mView!!.scrollViewUpdate.scrollTo(0, 150)
            mView!!.edtUsername_update.requestFocus()
            mView!!.edtUsername_update.error=getString(R.string.please_enter_your_full_name)
            LogUtils.shortToast(requireContext(), getString(R.string.please_enter_your_full_name))
        } else if (!SharedPreferenceUtility.getInstance().isCharacterAllowed(username)) {
            mView!!.scrollViewUpdate.scrollTo(0, 150)
            mView!!.edtUsername_update.requestFocus()
            mView!!.edtUsername_update.error=getString(R.string.emojis_are_not_allowed)
            LogUtils.shortToast(requireContext(), getString(R.string.emojis_are_not_allowed))
        } else if (TextUtils.isEmpty(fullname)){
            mView!!.scrollViewUpdate.scrollTo(0, 150)
            mView!!.edtFullName_update.requestFocus()
            mView!!.edtFullName_update.error=getString(R.string.please_enter_your_full_name)
            LogUtils.shortToast(requireContext(), getString(R.string.please_enter_your_full_name))
        } else if (TextUtils.isEmpty(mobilenumber)) {
            mView!!.scrollViewUpdate.scrollTo(0, 150)
            mView!!.edtmobilenumber_signup.requestFocus()
            mView!!.edtmobilenumber_signup.error=getString(R.string.please_enter_your_phone_number)
            LogUtils.shortToast(requireContext(), getString(R.string.please_enter_your_phone_number))
        }
        else if ((mobilenumber.length < 7 || mobilenumber.length > 15)) {
            mView!!.scrollViewUpdate.scrollTo(0, 150)
            mView!!.edtmobilenumber_update.requestFocus()
            mView!!.edtmobilenumber_update.error=getString(R.string.mob_num_length_valid)
            LogUtils.shortToast(requireContext(), getString(R.string.mob_num_length_valid))
        }
        else if (TextUtils.isEmpty(emailaddress) && !SharedPreferenceUtility.getInstance().isEmailValid(emailaddress)) {
            mView!!.scrollViewUpdate.scrollTo(0, 150)
            mView!!.edtemailaddress_update.requestFocus()
            mView!!.edtemailaddress_update.error=getString(R.string.please_enter_valid_email)
            LogUtils.shortToast(requireContext(), getString(R.string.please_enter_valid_email))
        }
        else if (TextUtils.isEmpty(location)) {
            mView!!.scrollViewUpdate.scrollTo(0, 150)
            mView!!.edtlocation_update.requestFocus()
            mView!!.edtlocation_update.error=getString(R.string.please_enter_valid_loc)
            LogUtils.shortToast(requireContext(), getString(R.string.please_enter_valid_email))
        }
        else{
            Update()
        }
    }

    private fun Update() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_update.visibility= View.VISIBLE
        val builder = APIClient.createMultipartBodyBuilder(arrayOf("user_id", "name", "address"),
        arrayOf(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0).toString(),
        fullname,
        location.toString(),
        mLatitude.toString(),
        mLongitude.toString()))

        Log.e("image_path", imagePath)
        if (imagePath != "") {
            if (profile_pic_changed){
                profile_pic_changed = false
                val file = File(imagePath)
                Log.e("file name ", file.name)
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ProfilePic,imagePath)
                val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
                builder!!.addFormDataPart("image", file.name, requestBody)
            }else{
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.ProfilePic,imagePath)
                val requestBody = RequestBody.create(MediaType.parse("image/*"), imagePath)
                builder!!.addFormDataPart("image", imagePath, requestBody)
            }
        }

        val call = apiInterface.updateProfile(builder!!.build())
        call?.enqueue(object : Callback<UpdateProfileResponse?>{
            override fun onResponse(call: Call<UpdateProfileResponse?>, response: Response<UpdateProfileResponse?>) {
                mView!!.progressBar_update.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            findNavController().navigate(R.id.homeFragment)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<UpdateProfileResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.progressBar_update.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }


    private fun openCameraDialog() {
        val items = arrayOf<CharSequence>(getString(R.string.camera), getString(R.string.gallery), getString(R.string.cancel))
        val builder = AlertDialog.Builder(requireContext())
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


    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        status = PICK_IMAGE_FROM_GALLERY
        resultLauncher.launch(intent)
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID.toString() + ".provider", getOutputMediaFile(type)!!)
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

    private fun showProfile() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_update.visibility= View.VISIBLE
        val call = Utility.apiInterface.showProfile(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0))
        call?.enqueue(object : Callback<ProfileShowResponse?> {
            override fun onResponse(
                    call: Call<ProfileShowResponse?>,
                    response: Response<ProfileShowResponse?>
            ) {
                mView!!.progressBar_update.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            imagePath = response.body()!!.profile!!.image!!
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image).into(mView!!.civ_profile_update)
                            mView!!.edtUsername_update.setText(response.body()!!.profile!!.username)
                            mView!!.edtFullName_update.setText(response.body()!!.profile!!.name)
                            mView!!.edtemailaddress_update.setText(response.body()!!.profile!!.email)
                            mView!!.edtmobilenumber_update.setText(response.body()!!.profile!!.phone)
                            mView!!.edtlocation_update.setText(response.body()!!.profile!!.address)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ProfileShowResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.progressBar_update.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                EditProfileFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
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
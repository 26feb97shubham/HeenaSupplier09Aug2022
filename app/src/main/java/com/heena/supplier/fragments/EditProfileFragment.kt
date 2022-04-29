package com.heena.supplier.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.heena.supplier.BuildConfig
import com.heena.supplier.R
import com.heena.supplier.custom.FetchPath
import com.heena.supplier.models.ProfileShowResponse
import com.heena.supplier.models.UpdateProfileResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.Utility.IMAGE_DIRECTORY_NAME
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import kotlinx.android.synthetic.main.activity_home2.*
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

class EditProfileFragment : Fragment() {

    private var mView : View? = null
    private val PERMISSIONS_1 =  arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val PERMISSIONS_2 = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    private var uri: Uri? = null
    val MEDIA_TYPE_IMAGE = 1
    val PICK_IMAGE_FROM_GALLERY = 10
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var imagePath = ""
    private var docpath = ""
    var username: String = ""
    var fullname: String = ""
    var mobilenumber: String = ""
    var emailaddress: String = ""
    var location : String? = ""
    var password: String = ""
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    var AUTOCOMPLETE_REQUEST_CODE: Int = 500
    val PICK_DOC = 11
    var countryName: String = ""
    var profile_pic_changed = false
    var status = 0
    var my_click = ""
    var profile_picture:String=""

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
                    }else if (my_click.equals("document")) {
                        openCameraDialog2()
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
                    Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                        requireContext().getString(R.string.please_allow_permissions),
                        requireContext())
                }
            }


    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (status.equals(CAMERA_CAPTURE_IMAGE_REQUEST_CODE)){
            if (it.resultCode == Activity.RESULT_OK){
                if (uri != null) {
                    if(my_click.equals("document")){
                        docpath = ""
                        docpath = uri!!.path!!
                        Utility.showSnackBarOnValidationSuccess(mView!!.editProfileFragmentConstraintlayout,
                            requireContext().getString(R.string.document_uploaded),
                            requireContext())
                        mView!!.txtUpdateLicenseEditProfile.text = getString(R.string.document_updated)
                        Glide.with(this).load("file:///$docpath").placeholder(R.drawable.attach).into(mView!!.imgAttachEditProfile)
                    }else{
                        imagePath = ""
                        Log.e("uri", uri.toString())
                        imagePath = uri!!.path!!
                        Log.e("image_path", imagePath)
                        Glide.with(this).load("file:///$imagePath").placeholder(R.drawable.user).into(mView!!.civ_profile_update)
                    }
                } else {
                    Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                        requireContext().getString(R.string.something_went_wrong),
                        requireContext())
                }
            }
        }else if (status.equals(PICK_IMAGE_FROM_GALLERY)){
            if (it.resultCode==Activity.RESULT_OK){
                val data: Intent? = it.data
                if (data!!.data != null) {
                    if(my_click.equals("document")){
                        docpath = ""
                        val uri = data.data
                        docpath = if (uri.toString().startsWith("content")) {
                            FetchPath.getPath(requireContext(), uri!!)!!
                        } else {
                            uri!!.path!!
                        }
                        mView!!.txtUpdateLicenseEditProfile.text = getString(R.string.document_updated)
                        Utility.showSnackBarOnValidationSuccess(mView!!.editProfileFragmentConstraintlayout,
                            requireContext().getString(R.string.document_updated),
                            requireContext())
                        Glide.with(this).load("file:///$docpath").placeholder(R.drawable.attach).into(mView!!.imgAttachEditProfile)
                    }else{
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
            }
        }else if (status.equals(PICK_DOC)){
            if(it.resultCode==Activity.RESULT_OK && it.data!=null){
                val files: java.util.ArrayList<MediaFile> = it.data!!.getParcelableArrayListExtra(
                    FilePickerActivity.MEDIA_FILES)!!
                if (files.size != 0) {
                    docpath=""
                    for (i in 0 until files.size) {
                        val filePath = FetchPath.getPath(requireContext(), files[i].uri)
                        if (filePath!!.contains(".doc")
                            || filePath.contains(".docx") || filePath.contains(".pdf") || filePath.contains(".txt")) {
                            docpath=filePath
//                        mView.txtUpdateLicense.text = getString(R.string.document_updated)
                            Utility.showSnackBarOnValidationSuccess(mView!!.editProfileFragmentConstraintlayout,
                                requireContext().getString(R.string.document_updated),
                                requireContext())
                            when {
                                docpath.contains(".pdf") -> {
                                    Glide.with(requireContext()).load(docpath).placeholder(R.drawable.pdfbox).into(mView!!.imgAttachEditProfile)
                                }
                                docpath.contains(".doc") || docpath.contains(".docx") -> {
                                    Glide.with(requireContext()).load(docpath).placeholder(R.drawable.docbox).into(mView!!.imgAttachEditProfile)
                                }
                                docpath.contains(".txt") -> {
                                    Glide.with(requireContext()).load(docpath).placeholder(R.drawable.txt).into(mView!!.imgAttachEditProfile)
                                }
                                else -> {
                                    Glide.with(requireContext()).load(docpath).placeholder(R.drawable.txt).into(mView!!.imgAttachEditProfile)
                                }
                            }
                        }
                    }
                }
            }
        } else if (status.equals(AUTOCOMPLETE_REQUEST_CODE)){
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
                        mView!!.edtlocation_update.text = address
                        break
                    }
                    ard++
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        showProfile()

        mView!!.btnUpdate.setSafeOnClickListener {
            mView!!.btnUpdate.startAnimation(AlphaAnimation(1f, 0.5f))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), mView!!.btnUpdate)
            validateAndUpdate()
        }

        mView!!.editProfileUpdate.setSafeOnClickListener {
            profile_pic_changed = true
            mView!!.editProfileUpdate.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "profile"
            activityResultLauncher.launch(PERMISSIONS_1)
        }

        mView!!.edtlocation_update.setSafeOnClickListener {
            mView!!.edtlocation_update.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "location"
            activityResultLauncher.launch(PERMISSIONS_2)
        }

        /*mView!!.imgAttachEditProfile.setSafeOnClickListener {
            mView!!.imgAttachEditProfile.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "document"
            activityResultLauncher.launch(PERMISSIONS_1)

        }

        mView!!.txtUpdateLicenseEditProfile.setSafeOnClickListener {
            mView!!.txtUpdateLicenseEditProfile.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "document"
            activityResultLauncher.launch(PERMISSIONS_1)
        }*/
    }

    private fun validateAndUpdate() {
        username = mView!!.edtUsername_update.text.toString().trim()
        fullname = mView!!.edtFullName_update.text.toString().trim()
        mobilenumber = mView!!.edtmobilenumber_update.text.toString().trim()
        emailaddress = mView!!.edtemailaddress_update.text.toString().trim()
        location = mView!!.edtlocation_update.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                requireContext().getString(R.string.please_enter_your_full_name),
                requireContext())
        } else if (!sharedPreferenceInstance!!.isCharacterAllowed(username)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                requireContext().getString(R.string.emojis_are_not_allowed),
                requireContext())

        } else if (TextUtils.isEmpty(fullname)){
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                requireContext().getString(R.string.please_enter_your_full_name),
                requireContext())

        } else if (TextUtils.isEmpty(mobilenumber)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                requireContext().getString(R.string.please_enter_your_phone_number),
                requireContext())

        }
        else if ((mobilenumber.length < 7 || mobilenumber.length > 15)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                requireContext().getString(R.string.mob_num_length_valid),
                requireContext())

        }
        else if (TextUtils.isEmpty(emailaddress) && !sharedPreferenceInstance!!.isEmailValid(emailaddress)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                requireContext().getString(R.string.please_enter_valid_email),
                requireContext())

        }
        else if (TextUtils.isEmpty(location)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                requireContext().getString(R.string.please_enter_valid_loc),
                requireContext())
        }
        else{
            Update()
        }
    }

    private fun Update() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_update.visibility= View.VISIBLE

        var call : Call<UpdateProfileResponse?>?=null

        if(imagePath.equals("")){
            val builder = APIClient.createBuilder(arrayOf("user_id", "name", "address", "image", "lang"),
                arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0).toString(),
                    fullname,
                    location.toString(),
                    "",
                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
            call = apiInterface.updateProfile(builder.build())

        }else{
            val builder = APIClient.createMultipartBodyBuilder(arrayOf("user_id", "name", "address", "lang"),
                arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0).toString(),
                    fullname,
                    location.toString(),
                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))

            Log.e("image_path", imagePath)
            if (imagePath != "") {
                if (profile_pic_changed){
                    profile_pic_changed = false
                    val file = File(imagePath)
                    Log.e("file name ", file.name)
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.ProfilePic,imagePath)
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
                    builder!!.addFormDataPart("image", file.name, requestBody)
                }else{
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.ProfilePic,imagePath)
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), imagePath)
                    builder!!.addFormDataPart("image", imagePath, requestBody)
                }
            }
            call = apiInterface.updateProfile(builder!!.build())
        }

        call?.enqueue(object : Callback<UpdateProfileResponse?>{
            override fun onResponse(call: Call<UpdateProfileResponse?>, response: Response<UpdateProfileResponse?>) {
                mView!!.progressBar_update.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.Fullname,fullname)
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.Username,username)
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.Address,location)
                            Utility.showSnackBarOnResponseSuccess(mView!!.editProfileFragmentConstraintlayout,
                                response.body()!!.message.toString(),
                                requireContext())

                            Handler().postDelayed({ findNavController().popBackStack() }, 1200)

                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintlayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintlayout,
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

            override fun onFailure(call: Call<UpdateProfileResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintlayout,
                    throwable.message!!,
                    requireContext())
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
        val call = Utility.apiInterface.showProfile(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
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
                            imagePath = ""
                            docpath = ""
                            docpath = response.body()!!.profile!!.trade_license!!
                            profile_picture = response.body()!!.profile!!.image!!
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image).listener(object : RequestListener<Drawable>{
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    mView!!.edit_image_progress_bar.visibility = View.GONE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                            }).placeholder(R.drawable.user)
                                .into(mView!!.civ_profile_update)
                            mView!!.edtUsername_update.setText(response.body()!!.profile!!.username)
                            mView!!.edtFullName_update.setText(response.body()!!.profile!!.name)
                            mView!!.edtemailaddress_update.setText(response.body()!!.profile!!.email)
                            mView!!.edtmobilenumber_update.setText(response.body()!!.profile!!.country_code + response.body()!!.profile!!.phone)
                            mView!!.edtlocation_update.setText(response.body()!!.profile!!.address)

                            when {
                                sharedPreferenceInstance!![SharedPreferenceUtility.DocPath, ""].isNotEmpty() -> {
                                    when {
                                        docpath.contains(".pdf") -> {
                                            Glide.with(requireContext()).load(docpath).placeholder(R.drawable.pdfbox).into(mView!!.imgAttachEditProfile)
                                        }
                                        docpath.contains(".doc") || docpath.contains(".docx") -> {
                                            Glide.with(requireContext()).load(docpath).placeholder(R.drawable.docbox).into(mView!!.imgAttachEditProfile)
                                        }
                                        docpath.contains(".txt") -> {
                                            Glide.with(requireContext()).load(docpath).placeholder(R.drawable.txt).into(mView!!.imgAttachEditProfile)
                                        }
                                        else -> {
                                            Glide.with(requireContext()).load(docpath).placeholder(R.drawable.txt).into(mView!!.imgAttachEditProfile)
                                        }
                                    }
                                }
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintlayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
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

            override fun onFailure(call: Call<ProfileShowResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintlayout,
                    throwable.message!!,
                    requireContext())
                mView!!.progressBar_update.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun openCameraDialog2() {
        val items = arrayOf<CharSequence>(getString(R.string.camera), getString(R.string.gallery), getString(R.string.document), getString(R.string.cancel))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_documents))
        builder.setItems(items) { dialogInterface, i ->
            if (items[i] == getString(R.string.camera)) {
                captureImage()
            } else if (items[i] == getString(R.string.gallery)) {
                chooseImage()
            }
            else if (items[i] == getString(R.string.document)) {
                chooseDoc()
            }else if (items[i] == getString(R.string.cancel)) {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    private fun chooseDoc() {
        val intent= Intent(requireContext(), FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setShowFiles(true)
                .setShowImages(false)
                .setShowAudios(false)
                .setShowVideos(false)
                .setMaxSelection(1)
                .setSuffixes("txt", "pdf","doc", "docx")
                .setSkipZeroSizeFiles(true)
                .build())
        status = PICK_DOC
        resultLauncher.launch(intent)

    }
}
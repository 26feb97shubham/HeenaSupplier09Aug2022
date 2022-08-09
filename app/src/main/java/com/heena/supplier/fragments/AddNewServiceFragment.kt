package com.heena.supplier.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.BuildConfig
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.AddNewPhotosAdapter
import com.heena.supplier.adapters.CategoryAdapter
import com.heena.supplier.extras.DecimalDigitsInputFilter
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.custom.FetchPath
import com.heena.supplier.utils.Utility.IMAGE_DIRECTORY_NAME
import com.heena.supplier.utils.Utility.createImageFromContentUri
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_add_new_service.view.*
import kotlinx.android.synthetic.main.fragment_add_new_service.view.tv_save_service
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
import kotlin.collections.ArrayList


class AddNewServiceFragment : Fragment() {

    private var mView : View?=null
    private var uri: Uri? = null
    private var imagePath = ""
    var AUTOCOMPLETE_REQUEST_CODE: Int = 500
    var mLatitude: Double = 0.0
    var mLongitude: Double = 0.0
    var countryName: String = ""
    lateinit var addNewPhotosAdapter: AddNewPhotosAdapter
    var categoryList = ArrayList<CategoryItem>()
    var PICK_IMAGE_MULTIPLE = 101
    var service_title = ""
    var address : String = ""
    var selected_category : String = ""
    var price = ""
//    var child_price = ""
    var service_description = ""
    var category_id : Int?=null
    var service_id = 0
    var subscription_id = 0
    var service_status = ""
    var service : Service?= null
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    private var categoryAdapter : CategoryAdapter?=null
    private var category_clicked = false
    val MEDIA_TYPE_IMAGE = 1
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 103
    var galleryItemList = ArrayList<GalleryItem>()
    private var galleryItemListSize : Int = 0
    var pathList=ArrayList<PhotoData>()

    private val PERMISSIONS_1 =  arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val PERMISSIONS_2 = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
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
                    if (my_click.equals("upload_photos")){
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
                    Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                        requireContext().getString(R.string.please_allow_permissions),
                        requireContext())
                    Log.e("Denied", "Permissions")
                }
            }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (status.equals(CAMERA_CAPTURE_IMAGE_REQUEST_CODE)){
            if (it.resultCode == Activity.RESULT_OK){
                val photoData = PhotoData()
                photoData.status = "new"
                photoData.path = imagePath
                pathList.add(photoData)
                setServicePhotoAdapter(pathList)
                imagePath = ""
            }
        }else if (status.equals(PICK_IMAGE_MULTIPLE)){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                if (it.resultCode==Activity.RESULT_OK){
                    val data: Intent? = it.data
                    imagePath = ""
                    if (data!!.clipData != null) {
                        val count: Int = data.clipData!!.itemCount
                        Log.e("count", ""+count)
                        if (count+pathList.size<=5){
                            for (i in 0 until count) {
                                val selectedImage: Uri = data.clipData!!.getItemAt(i).uri
                                val file = createImageFromContentUri(requireContext(),selectedImage)
                                imagePath = file!!.absolutePath
                                val photoData = PhotoData()
                                photoData.status = "new"
                                photoData.path = imagePath
                                pathList.add(photoData)
                                Log.e("pathList", pathList.size.toString())
                            }
                            setServicePhotoAdapter(pathList)
                        }else{
                            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                                requireContext().getString(R.string.only_five_images_can_be_selected_at_once),
                                requireContext())
                        }
                    }
                    else if (data.data != null) {
                        var imagePath= ""
                        val imageURI = data.data
                        val file = createImageFromContentUri(requireContext(),imageURI!!)
                        imagePath = file!!.absolutePath

                        val photoData = PhotoData()
                        photoData.status = "new"
                        photoData.path = imagePath
                        pathList.add(photoData)
                        setServicePhotoAdapter(pathList)
                    }else{
                        Log.e("error_data", data.toString())
                    }
                }
            } else {
                if (it.resultCode==Activity.RESULT_OK){
                    val data: Intent? = it.data
                    imagePath = ""
                    if (data!!.clipData != null) {
                        val count: Int = data.clipData!!.itemCount
                        Log.e("count", ""+count)
                        if (count+pathList.size<=5){
                            for (i in 0 until count) {
                                val selectedImage: Uri = data.clipData!!.getItemAt(i).uri

                                val file = createImageFromContentUri(requireContext(),selectedImage)
                                //imagePath = FetchPath.getPath(requireActivity(), selectedImage)!!
                                imagePath = file!!.absolutePath
                                val photoData = PhotoData()
                                photoData.status = "new"
                                photoData.path = imagePath
                                pathList.add(photoData)
                            }
                            setServicePhotoAdapter(pathList)
                        }else{
                            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                                requireContext().getString(R.string.only_five_images_can_be_selected_at_once),
                                requireContext())
                        }
                    }
                    else if (data.data != null) {
                        val imageURI = data.data
                        imagePath = ""
                        /*imagePath = if (imageURI.toString().startsWith("content")) ({
                            FetchPath.getPath(requireActivity(), imageURI!!)
                        }).toString() else {
                            imageURI!!.path!!
                        }*/

                        val file = createImageFromContentUri(requireContext(),imageURI!!)
                        imagePath = file!!.absolutePath

                        Log.e("activity", ""+requireActivity())
                        val photoData = PhotoData()
                        photoData.status = "new"
                        photoData.path = imagePath
                        pathList.add(photoData)
                        setServicePhotoAdapter(pathList)
                    }else{
                        Log.e("error_data", data.toString())
                    }
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
                        mView!!.tv_location.text = address
                        break
                    }
                    ard++
                }
            }
        }
    }


    @SuppressLint("Range")
    fun getImageFilePath(uri: Uri, context: Context) {
        val file = File(uri.path)
        val filePath = file.path.split(":").toTypedArray()
        val image_id = filePath[filePath.size - 1]
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Images.Media._ID + " = ? ",
            arrayOf(image_id),
            null
        )
        try {
            while (cursor!!.moveToNext()){
                val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                val photoData = PhotoData()
                photoData.status = "new"
                photoData.path = imagePath
                pathList.add(photoData)
            }
        }catch (e : Exception){
            Log.e("exception", e.message.toString())
        }finally {
            cursor?.close()
        }
    }




    fun setServicePhotoAdapter(pathList: ArrayList<PhotoData>) {
        mView!!.rv_uploaded_photos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        addNewPhotosAdapter = AddNewPhotosAdapter(requireContext(), pathList, service_status, object : ClickInterface.OnRecyclerItemClick {
            override fun OnClickAction(position: Int) {
                if (service_status.equals("show")){
                    val gallery = ArrayList<String>()
                    for (i in 0 until pathList.size){
                        gallery.add(pathList[i].path)
                    }
                    val bundle = Bundle()
                    bundle.putStringArrayList("gallery", gallery)
                    findNavController().navigate(R.id.viewImageFragment, bundle)
                }else{
                    if(pathList[position].status.equals("old")) {
                        val alert = android.app.AlertDialog.Builder(requireContext())
                        alert.setMessage(requireContext().getString(R.string.delete_message))
                        alert.setCancelable(false)
                        alert.setPositiveButton(getString(R.string.yes)) { dialog, i ->
                            dialog.cancel()
                            deleteServerimage(position)
                        }
                        alert.setNegativeButton(getString(R.string.no)) { dialog, i ->
                            dialog.cancel()
                        }
                        alert.show()
                    }
                    else {
                        Log.e("check", "" + position)
                        pathList.removeAt(position)
                        mView!!.iv_upload_photo.isEnabled = (pathList.size<5)
                        Log.e("check size", "" + pathList.size)
                        mView!!.rv_uploaded_photos.adapter=addNewPhotosAdapter
                        addNewPhotosAdapter.notifyDataSetChanged()
                    }
                    mView!!.iv_upload_photo.alpha = 1f
                    mView!!.iv_upload_photo.isEnabled = true
                }
            }
        })
        mView!!.rv_uploaded_photos.adapter = addNewPhotosAdapter
        addNewPhotosAdapter.notifyDataSetChanged()
    }

    @SuppressLint("Range")
    private fun getRealPath(ur: Uri?): String? {
        var realpath = ""
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

        // Get the cursor
        val cursor: Cursor = requireContext().contentResolver.query(ur!!,
            filePathColumn, null, null, null
        )!!
        try {
            while (cursor.moveToNext()){
                realpath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
            }
        }catch (e : Exception){
            Log.e("exception", e.message.toString())
        }finally {
            cursor.close()
        }
        return realpath
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            service_status = it.getString("status").toString()
            service_id = it.getInt("service_id")
            subscription_id = it.getInt("subscription_id")
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView  = inflater.inflate(R.layout.fragment_add_new_service, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCategories()

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

        if (service_status.equals("show")){
            showService(service_id)
            setNonEditableAndNonClickableFields()
            mView!!.tv_add_new_service_txt.text = getString(R.string.view_service_details)
            mView!!.tv_save_service.visibility = View.GONE
        }else{
            mView!!.tv_save_service.visibility = View.VISIBLE
            mView!!.llUploadPhotos.visibility = View.VISIBLE
            if (service_status.equals("edit")){
                showService(service_id)
                mView!!.tv_add_new_service_txt.text = getString(R.string.edit_service)
                mView!!.tv_save_service.text = getString(R.string.update)
            }else{
                pathList.clear()
                mView!!.tv_add_new_service_txt.text = getString(R.string.add_new_service)
                mView!!.tv_save_service.text = getString(R.string.save)
            }
        }

        mView!!.tv_location.setSafeOnClickListener {
            mView!!.tv_location.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "location"
            activityResultLauncher.launch(PERMISSIONS_2)
        }

        mView!!.iv_upload_photo.setSafeOnClickListener {
            Log.e("path_list_size", ""+pathList.size)
            if (pathList.size<5){
                mView!!.iv_upload_photo.isEnabled = true
                mView!!.iv_upload_photo.startAnimation(AlphaAnimation(1f, 0.5f))
                my_click = "upload_photos"
                activityResultLauncher.launch(PERMISSIONS_1)
            }else{
                Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                    requireContext().getString(R.string.only_five_images_can_be_selected_at_once),
                    requireContext())
                mView!!.iv_upload_photo.isEnabled = false
            }
        }

        mView!!.ll_service_location.setSafeOnClickListener {
            mView!!.ll_service_location.startAnimation(AlphaAnimation(1f, 0.5f))
            my_click = "location"
            activityResultLauncher.launch(PERMISSIONS_2)
        }

        mView!!.tv_save_service.setSafeOnClickListener {
            mView!!.tv_save_service.startAnimation(AlphaAnimation(1f, 0.5f))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), mView!!.tv_save_service)
            validateAndSave()
        }

        mView!!.spinner_category_card.setSafeOnClickListener {
            if (!category_clicked){
                category_clicked = true
                mView!!.ll_categoryItems.visibility = View.VISIBLE
            }else{
                category_clicked = false
                mView!!.ll_categoryItems.visibility = View.GONE
            }
        }

        mView!!.et_price.setFilters(arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2)))

    }

    private fun showService(serviceId: Int) {
        mView!!.add_service_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showService(serviceId, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ShowServiceResponse?>{
            override fun onResponse(call: Call<ShowServiceResponse?>, response: Response<ShowServiceResponse?>) {
                mView!!.add_service_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        galleryItemList.clear()
                        pathList.clear()
                        galleryItemList = response.body()!!.gallery as ArrayList<GalleryItem>
                        galleryItemListSize = galleryItemList.size
                        for (i in 0 until galleryItemList.size){
                            val photoData = PhotoData()
                            photoData.status = "old"
                            photoData.path = galleryItemList[i].name.toString()
                            pathList.add(photoData)
                        }
                        service = response.body()!!.service
                        setUploadPhotos(pathList)
                        mView!!.et_service_title.setText(response.body()!!.service!!.name)
                        mView!!.et_price.setText(response.body()!!.service!!.price!!.main)
                        mView!!.et_service_desc.setText(response.body()!!.service!!.description)
                        mView!!.tv_location.text = response.body()!!.service!!.address
                        mView!!.spinner_category.text = service!!.category!!.name.toString()
                        selected_category = service!!.category!!.name.toString()
                        category_id = Utility.returnCategory(selected_category, categoryList)
                        address = service!!.address.toString()
                        mLatitude = service!!.lat!!.toDouble()
                        mLongitude = service!!.long!!.toDouble()
                        price = service!!.price!!.main!!.toString()
                        service_description = service!!.description.toString()
                        service_title = service!!.name.toString()
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ShowServiceResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
                mView!!.add_service_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun setCategoryText(spinner: Spinner, text : String){
        for (i in 0 until spinner.adapter.count){
            if (spinner.adapter.getItem(i).toString().contains(text)){
                spinner.setSelection(i)
            }
        }
    }

    private fun validateAndSave() {
        service_title = mView!!.et_service_title.text.toString().trim()
        service_description = mView!!.et_service_desc.text.toString().trim()
        price = mView!!.et_price.text.toString().trim()
        address = mView!!.tv_location.text.toString().trim()
        if (pathList.size==0){
            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                requireContext().getString(R.string.please_select_atleast_one_image_to_proceed),
                requireContext())
        }else if(TextUtils.isEmpty(service_title)){
            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_service_title),
                requireContext())
        }else if (TextUtils.isEmpty(address)){
            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_loc),
                requireContext())
        }else if (TextUtils.isEmpty(selected_category)){
            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                requireContext().getString(R.string.no_category_selected),
                requireContext())
        }else if (TextUtils.isEmpty(price)){
            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_price),
                requireContext())
        }else if (price.startsWith("0")){
            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_price),
                requireContext())
        } else if (TextUtils.isEmpty(service_description)){
            Utility.showSnackBarValidationError(mView!!.addNewServiceFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_service_desc),
                requireContext())
        }else{
            if(service_status.equals("edit")){
                update()
            }else{
                save()
            }
        }
    }

    private fun update(){
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.add_service_progressBar.visibility= View.VISIBLE
        val builder = APIClient.createMultipartBodyBuilder(arrayOf("service_id", "category_id", "name", "price",
        "address", "lat", "long", "description", "lang"),
        arrayOf(service!!.service_id.toString(),
        category_id.toString(),
        service_title,
        price,
        address,
        mLatitude.toString(),
        mLongitude.toString(),
        service_description,
        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        Log.e("path_list_size", ""+pathList)
        for(i in 0 until pathList.size){
            val file = File(pathList[i].path)
            if(file.exists()){
                val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                builder!!.addFormDataPart("images[]", file.name, requestBody)
            }
        }
        Log.e("path_list_size", ""+pathList)
        val call = apiInterface.editService(builder!!.build())
        call?.enqueue(object : Callback<EditServiceResponse?>{
            override fun onResponse(call: Call<EditServiceResponse?>, response: Response<EditServiceResponse?>) {
                mView!!.add_service_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        mView!!.add_service_progressBar.visibility = View.GONE
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        if (response.body()!!.status == 1) {
                            Utility.showSnackBarOnResponseSuccess(mView!!.addNewServiceFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            findNavController().popBackStack()
                        } else {
                            Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
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

            override fun onFailure(call: Call<EditServiceResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
                mView!!.add_service_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun save() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.add_service_progressBar.visibility= View.VISIBLE
        val builder = APIClient.createMultipartBodyBuilder(arrayOf("user_id", "category_id",
            "name", "price", "address", "lat",
            "long", "description", "subscription_id", "lang", "manager_email"),
                arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0).toString(),
                        category_id.toString(),
                        service_title,
                        price,
                        address,
                        mLatitude.toString(),
                        mLongitude.toString(),
                        service_description,
                    subscription_id.toString(),
                    sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
                sharedPreferenceInstance!![SharedPreferenceUtility.UserEmail, ""]))

        for(i in 0 until pathList.size){
            val file = File(pathList[i].path)
            if(file.exists()){
                val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                builder!!.addFormDataPart("images[]", file.name, requestBody)
            }
        }
        val call = apiInterface.addService(builder!!.build())
        call!!.enqueue(object : Callback<AddServiceResponse?> {
            override fun onResponse(
                    call: Call<AddServiceResponse?>,
                    response: Response<AddServiceResponse?>
            ) {
                mView!!.add_service_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        mView!!.add_service_progressBar.visibility = View.GONE
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        if (response.body()!!.status == 1) {
                            Utility.showSnackBarOnResponseSuccess(mView!!.addNewServiceFragmentConstraintLayout,
                                response.body()!!.message,
                                requireContext())
                            findNavController().popBackStack()
                        } else {
                            Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                                response.body()!!.message,
                                requireContext())
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
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

            override fun onFailure(call: Call<AddServiceResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
                mView!!.add_service_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun openCameraDialog() {
        val items = arrayOf<CharSequence>(getString(R.string.camera), getString(R.string.gallery), getString(R.string.cancel))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.you_can_select_max_5_photos))
        builder.setItems(items) { dialogInterface, i ->
            if (items[i] == getString(R.string.camera)) {
                captureImage()
            } else if (items[i] == getString(R.string.gallery)) {
                chooseImageVideo()
            } else if (items[i] == getString(R.string.cancel)) {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    private fun chooseImageVideo() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            status = PICK_IMAGE_MULTIPLE
            resultLauncher.launch(intent)
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            status = PICK_IMAGE_MULTIPLE
            resultLauncher.launch(intent)
        }

    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file_name = "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss",Locale.ENGLISH).format(Date())}"
        val storageDirectroy: File = requireContext().externalCacheDir!!
        try {
            val imageFile: File = File.createTempFile(file_name, ".jpeg", storageDirectroy)
            imagePath = imageFile.absolutePath
            val imageURI = FileProvider.getUriForFile(requireContext(), requireContext().applicationInfo.packageName+".provider", imageFile)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
            status = CAMERA_CAPTURE_IMAGE_REQUEST_CODE
            resultLauncher.launch(intent)
        }catch (e : IOException){
            e.printStackTrace()
        }
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

    private fun getCategories() {
        val call = apiInterface.categoryList(sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<CategoryListResponse?> {
            override fun onResponse(
                    call: Call<CategoryListResponse?>,
                    response: Response<CategoryListResponse?>
            ) {
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            mView!!.spinner_category_card.visibility = View.VISIBLE
                            categoryList.clear()
                            categoryList = response.body()!!.category as ArrayList<CategoryItem>
                            mView!!.rv_category_add_new_service.layoutManager = LinearLayoutManager(requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false)
                            categoryAdapter = CategoryAdapter(requireContext(), categoryList, object : ClickInterface.OnCategoryItemClick{
                                override fun OnClickAction(position: Int, category: CategoryItem) {
                                    mView!!.spinner_category.text = category.name
                                    selected_category =   mView!!.spinner_category.text.toString().trim()
                                    mView!!.ll_categoryItems.visibility = View.GONE
                                    category_clicked = false
                                    category_id = Utility.returnCategory(selected_category, categoryList)
                                    Log.e("category_id", category_id.toString())
                                }
                            })
                            mView!!.rv_category_add_new_service.adapter = categoryAdapter
                            categoryAdapter!!.notifyDataSetChanged()
                        } else {
                            mView!!.ll_categoryItems.visibility = View.GONE
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    } else {
                        mView!!.ll_categoryItems.visibility = View.GONE
                        Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
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

            override fun onFailure(call: Call<CategoryListResponse?>, t: Throwable) {
                Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                    t.message.toString(),
                    requireContext())
            }
        })
    }

    private fun deleteServerimage(postion: Int) {
        val call=apiInterface.deleteServiceImage(galleryItemList?.get(postion)?.gallery_id.toString(), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ServiceImageDeleteResponse?> {
            override fun onResponse(
                    call: Call<ServiceImageDeleteResponse?>,
                    response: Response<ServiceImageDeleteResponse?>
            ) {
                if (response.isSuccessful && response.code() == 200) {
                    Log.e("check", "" + postion)
                    pathList.removeAt(postion)
                    mView!!.iv_upload_photo.isEnabled = (pathList.size<5)
                    Utility.showSnackBarOnResponseSuccess(mView!!.addNewServiceFragmentConstraintLayout,
                        response.body()!!.message.toString(),
                        requireContext())
                    Log.e("check size", "" + pathList.size)
                    galleryItemListSize=galleryItemListSize-1
                    mView!!.rv_uploaded_photos.adapter=addNewPhotosAdapter
                    addNewPhotosAdapter.notifyDataSetChanged()
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ServiceImageDeleteResponse?>, t: Throwable) {
                Utility.showSnackBarOnResponseError(mView!!.addNewServiceFragmentConstraintLayout,
                    t.message.toString(),
                    requireContext())
            }

        })
    }


    private fun setUploadPhotos(galleryPhotos: ArrayList<PhotoData>) {
        mView!!.rv_uploaded_photos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        addNewPhotosAdapter = AddNewPhotosAdapter(requireContext(), galleryPhotos,
            service_status,
            object : ClickInterface.OnRecyclerItemClick {
            override fun OnClickAction(position: Int) {
                if (!service_status.equals("show")){
                    if(galleryPhotos[position].status.equals("old")) {
                        val alert = android.app.AlertDialog.Builder(requireContext())
                        alert.setMessage(requireContext().getString(R.string.delete_message))
                        alert.setCancelable(false)
                        alert.setPositiveButton(getString(R.string.yes)) { dialog, i ->
                            dialog.cancel()
                            deleteServerimage(position)
                        }
                        alert.setNegativeButton(getString(R.string.no)) { dialog, i ->
                            dialog.cancel()
                        }
                        alert.show()
                    }
                    else {
                        Log.e("check", "" + position)
                        pathList.removeAt(position)
                        Log.e("check size", "" + pathList.size)
                        mView!!.rv_uploaded_photos.adapter=addNewPhotosAdapter
                        addNewPhotosAdapter.notifyDataSetChanged()
                    }
                    mView!!.iv_upload_photo.alpha = 1f
                    mView!!.iv_upload_photo.isEnabled = true
                }else{
                    val gallery = ArrayList<String>()
                    for (i in 0 until pathList.size){
                        gallery.add(pathList[i].path)
                    }
                    val bundle = Bundle()
                    bundle.putStringArrayList("gallery", gallery)
                    findNavController().navigate(R.id.viewImageFragment, bundle)
                }
            }
        })
        mView!!.rv_uploaded_photos.adapter = addNewPhotosAdapter
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

    private fun setNonEditableAndNonClickableFields(){
        mView!!.iv_upload_photo.isEnabled = false
        mView!!.et_service_title.isEnabled = false
        mView!!.et_price.isEnabled = false
        mView!!.llUploadPhotos.visibility = View.GONE
        mView!!.tv_location.isEnabled = false
        mView!!.spinner_category_card.isEnabled = false
        mView!!.spinner_category.isEnabled = false
        mView!!.et_service_desc.isEnabled = false
    }
}
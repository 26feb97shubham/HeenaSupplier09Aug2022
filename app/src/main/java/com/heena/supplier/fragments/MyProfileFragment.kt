package com.heena.supplier.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.supplier.BuildConfig
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.*
import com.heena.supplier.custom.FetchPath
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.IMAGE_DIRECTORY_NAME
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import kotlinx.android.synthetic.main.fragment_my_profile.view.*
import kotlinx.android.synthetic.main.side_top_view.view.*
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
import android.view.Gravity

import android.app.ProgressDialog
import android.os.*
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance


class MyProfileFragment : Fragment() {
    var mView : View ?=null
    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var uri: Uri? = null
    val MEDIA_TYPE_IMAGE = 1
    val PICK_IMAGE_FROM_GALLERY = 10
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var imagePath = ""
    lateinit var offersAndDiscountsAdapter: OffersAndDiscountsAdapter
    lateinit var servicesAdapter: ServicesAdapter
    lateinit var galleryStaggeredGridAdapter: GalleryStaggeredGridAdapter
    lateinit var reviewsAdapter: ReviewsAdapter
    private val galleryPhotos = ArrayList<String>()
    private var ImageUriList = ArrayList<Gallery>()
    val requestOption = RequestOptions().centerCrop()
    var serviceslisting = ArrayList<Service>()
    var offersListing = ArrayList<OfferItem>()
    var commentsList = ArrayList<CommentsItem>()
    var galleryImageList = ArrayList<String>()

    private var isButtonClicked = ""

    var status = 0
    var user_profile_name : String = ""

    private var subscription_id = 0

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if(allAreGranted) {
                Log.e("Granted", "Permissions")
                openCameraDialog()
            }else{
                Utility.showSnackBarValidationError(mView!!.myProfileFragmentConstraintLayout,
                    requireContext().getString(R.string.please_allow_permissions),
                    requireContext())
                val intent = Intent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    val uri = Uri.fromParts("package", requireContext().getPackageName(), null)
                    intent.data = uri
                    startActivity(intent)
                    Log.e("Denied", "Permissions")
                }

            }
        }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (status.equals(CAMERA_CAPTURE_IMAGE_REQUEST_CODE)){
            if (it.resultCode == Activity.RESULT_OK){
                if (uri != null) {
                    imagePath = ""
                    Log.e("uri", uri.toString())
                    imagePath = uri!!.path!!
                    galleryPhotos.add(imagePath)
                    setUploadPhotos(galleryPhotos)
                } else {
                    Utility.showSnackBarValidationError(mView!!.myProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.something_went_wrong),
                        requireContext())
                }
            }
        }else if (status.equals(PICK_IMAGE_FROM_GALLERY)){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                val data: Intent? = it.data
                galleryPhotos.clear()
                imagePath = ""
                if (data?.clipData != null) {
                    val cout: Int = data.clipData!!.itemCount
                    if(cout+galleryPhotos.size==0){
                        Utility.showSnackBarValidationError(mView!!.myProfileFragmentConstraintLayout,
                            requireContext().getString(R.string.please_select_atleast_one_image_to_proceed),
                            requireContext())
                    }else if(cout+galleryPhotos.size<=5){
                        for (i in 0 until cout) {
                            val imageurl: Uri = data.clipData!!.getItemAt(i).uri
                            imagePath = FetchPath.getPath(requireActivity(), imageurl)!!
                            galleryPhotos.add(imagePath)
                        }
                        setUploadPhotos(galleryPhotos)
                    }else{
                        Utility.showSnackBarValidationError(mView!!.myProfileFragmentConstraintLayout,
                            requireContext().getString(R.string.only_five_images_can_be_selected_at_once),
                            requireContext())
                    }
                } else if (data?.data!=null) {
                    val imagePath = data.data!!.path
                    galleryPhotos.add(imagePath.toString())
                    setUploadPhotos(galleryPhotos)
                }
            } else {
                if (it.resultCode==Activity.RESULT_OK){
                    val data: Intent? = it.data
                    imagePath = ""
                    if (data?.clipData != null) {
                        val cout: Int = data.clipData!!.itemCount
                        if(cout+galleryPhotos.size==0){
                            Utility.showSnackBarValidationError(mView!!.myProfileFragmentConstraintLayout,
                                requireContext().getString(R.string.please_select_atleast_one_image_to_proceed),
                                requireContext())
                        }else if(cout+galleryPhotos.size<=10){
                            for (i in 0 until cout) {
                                val imageurl: Uri = data.clipData!!.getItemAt(i).uri
                                imagePath = FetchPath.getPath(requireActivity(), imageurl)!!
                                galleryPhotos.add(imagePath)
                            }
                            setUploadPhotos(galleryPhotos)
                        }else{
                            Utility.showSnackBarValidationError(mView!!.myProfileFragmentConstraintLayout,
                                requireContext().getString(R.string.only_five_images_can_be_selected_at_once),
                                requireContext())
                        }
                    } else if (data?.data!=null) {
                        val imageURI = data.data!!
                        imagePath = FetchPath.getPath(requireActivity(), imageURI)!!
                        galleryPhotos.add(imagePath)
                        setUploadPhotos(galleryPhotos)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subscription_id = it.getInt("subscription_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_my_profile, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView!!.nestedScrollView.isSmoothScrollingEnabled = true

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    showProfile()
                    getServices()
                    getOffers()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "My Profile Fragment")
        }else{
            showProfile()
            getServices()
            getOffers()
        }

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

        Glide.with(this).load(
            sharedPreferenceInstance!!.get(
                SharedPreferenceUtility.ProfilePic,
                ""
            )
        )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    p0: GlideException?,
                    p1: Any?,
                    p2: com.bumptech.glide.request.target.Target<Drawable>?,
                    p3: Boolean
                ): Boolean {
                    Log.e("err", p0?.message.toString())
                    return false
                }

                override fun onResourceReady(
                    p0: Drawable?,
                    p1: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    p4: Boolean
                ): Boolean {


                    return false
                }
            }).apply(requestOption).into(mView!!.civ_profile)


        tv_add_new_offers.setSafeOnClickListener {
            if (sharedPreferenceInstance!![SharedPreferenceUtility.IsBankAdded, false]){
                val bundle = Bundle()
                bundle.putInt("offer_id", 0)
                bundle.putString("status", "add")
                findNavController().navigate(R.id.action_myProfileFragment_to_addNewOffersFragment, bundle)
            }else{
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    requireContext().getString(R.string.add_bank_details),
                    requireContext())
                Handler(Looper.getMainLooper()).postDelayed(object : Runnable{
                    override fun run() {
                        val bundle = Bundle()
                        bundle.putBoolean("isBankAdded", sharedPreferenceInstance!![SharedPreferenceUtility.IsBankAdded, false])
                        bundle.putSerializable("bank_details", null)
                        findNavController().navigate(R.id.editBankDetailsFragment, bundle)
                    }

                }, 1000)
            }

        }

        tv_add_new_service.setSafeOnClickListener {
            if (sharedPreferenceInstance!![SharedPreferenceUtility.IsBankAdded, false]){
                val bundle = Bundle()
                bundle.putInt("service_id", 0)
                bundle.putInt("subscription_id", 0)
                bundle.putString("status", "add")
                findNavController().navigate(R.id.action_myProfileFragment_to_addNewFeaturedFragment, bundle)
            }else{
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    requireContext().getString(R.string.add_bank_details),
                    requireContext())
                Handler(Looper.getMainLooper()).postDelayed(object : Runnable{
                    override fun run() {
                        val bundle = Bundle()
                        bundle.putBoolean("isBankAdded", sharedPreferenceInstance!![SharedPreferenceUtility.IsBankAdded, false])
                        bundle.putSerializable("bank_details", null)
                        findNavController().navigate(R.id.editBankDetailsFragment, bundle)
                    }

                }, 1000)
            }
        }

        mView!!.card_upload_photo.setSafeOnClickListener {
            mView!!.card_upload_photo.startAnimation(AlphaAnimation(1f, 0.5f))
            activityResultLauncher.launch(PERMISSIONS)
        }

        mView!!.tv_services.setSafeOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.VISIBLE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            isButtonClicked = "Service"
            getServices()
            getOffers()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))
        }

        mView!!.tv_gallery.setSafeOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.VISIBLE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            isButtonClicked = "Gallery"
            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))

            showGallery()
        }

        mView!!.tv_reviews.setSafeOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.VISIBLE
            isButtonClicked = "Reviews"
            getReviews()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.white))
        }
    }

    private fun showProfile() {
        mView!!.fragment_profile_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showProfile(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ProfileShowResponse?>{
            override fun onResponse(
                call: Call<ProfileShowResponse?>,
                response: Response<ProfileShowResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        mView!!.civProfilecontentProgressbar.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                                        mView!!.civProfilecontentProgressbar.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(mView!!.civ_profile)
                            if(response.body()!!.profile!!.name.equals("")){
                                user_profile_name = response.body()!!.profile!!.username!!
                            }else{
                                user_profile_name = response.body()!!.profile!!.name!!
                            }

                            if(response.body()!!.profile!!.comment_avg.equals("")){
                                mView!!.ratingBar.rating=0F
                                mView!!.txtRating.text = "0"
                            }else{
                                mView!!.ratingBar.rating = response.body()!!.profile!!.comment_avg!!.toFloat()
                                mView!!.txtRating.text = response.body()!!.profile!!.comment_avg!!.toString()
                            }
                            mView!!.tv_naqashat_name_location.text = user_profile_name
                            mView!!.tv_naqashat_experience.text = response.body()!!.profile!!.address!!
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
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
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())

            }

        })
    }

    private fun getReviews() {
        mView!!.fragment_profile_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showComments(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ShowCommentsResponse?>{
            override fun onResponse(call: Call<ShowCommentsResponse?>, response: Response<ShowCommentsResponse?>) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        mView!!.rv_reviews.visibility = View.VISIBLE
                        mView!!.ll_no_comments_found.visibility = View.GONE
                        commentsList.clear()
                        commentsList = response.body()!!.comments as ArrayList<CommentsItem>
                        mView!!.rv_reviews.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )

                        reviewsAdapter = ReviewsAdapter(
                            requireContext(),
                            commentsList)
                        mView!!.rv_reviews.adapter = reviewsAdapter
                    }else{
                        mView!!.rv_reviews.visibility = View.GONE
                        mView!!.ll_no_comments_found.visibility = View.VISIBLE
                    }
                }else{
                    mView!!.rv_reviews.visibility = View.GONE
                    mView!!.ll_no_comments_found.visibility = View.VISIBLE

                }
            }

            override fun onFailure(call: Call<ShowCommentsResponse?>, throwable: Throwable) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                mView!!.rv_reviews.visibility = View.GONE
                mView!!.ll_no_comments_found.visibility = View.VISIBLE
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    private fun getOffers() {
        val call = apiInterface.getOffersListing(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<OffersListingResponse?>{
            override fun onResponse(
                call: Call<OffersListingResponse?>,
                response: Response<OffersListingResponse?>
            ) {
                if(response.isSuccessful){
                    if (response.body()!!.status == 1){
                        offersListing.clear()
                        offersListing = response.body()!!.offer as ArrayList<OfferItem>
                        mView!!.rv_offers_n_discs.visibility = View.VISIBLE
                        mView!!.ll_no_offers_and_disc_found.visibility = View.GONE
                        mView!!.rv_offers_n_discs.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )

                        offersAndDiscountsAdapter = OffersAndDiscountsAdapter(
                            requireContext(),
                            offersListing,
                            object : ClickInterface.onOffersItemClick {
                                override fun onOfferDelete(position: Int) {
                                    val offerId = offersListing[position].offer_id
                                    val deleteOfferDialog = AlertDialog.Builder(requireContext())
                                    deleteOfferDialog.setCancelable(false)
                                    deleteOfferDialog.setTitle(requireContext().getString(R.string.delete_offer))
                                    deleteOfferDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_offer))
                                    deleteOfferDialog.setPositiveButton(requireContext().getString(R.string.delete), object : DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            deleteOffer(offerId!!, position)
                                            dialog!!.dismiss()
                                        }
                                    })
                                    deleteOfferDialog.setNegativeButton(requireContext().getString(R.string.cancel), object : DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            dialog!!.cancel()
                                        }
                                    })
                                    deleteOfferDialog.show()

                                }

                                override fun onOfferEdit(position: Int) {
                                    val updateOfferDialog = AlertDialog.Builder(requireContext())
                                    updateOfferDialog.setCancelable(false)
                                    updateOfferDialog.setTitle(requireContext().getString(R.string.update_offer))
                                    updateOfferDialog.setMessage(requireContext().getString(R.string.would_you_like_to_update_your_offer_details))
                                    updateOfferDialog.setPositiveButton(requireContext().getString(R.string.yes))
                                    { dialog, _ ->
                                        val offer_id = offersListing[position].offer_id
                                        val bundle = Bundle()
                                        bundle.putInt("offer_id", offer_id!!)
                                        bundle.putString("status", "edit")
                                        findNavController().navigate(R.id.action_myProfileFragment_to_addNewOffersFragment, bundle)
                                        dialog!!.dismiss()
                                    }
                                    updateOfferDialog.setNegativeButton(requireContext().getString(R.string.no))
                                    { dialog, _ -> dialog!!.cancel() }
                                    updateOfferDialog.show()
                                }

                                override fun onOfferView(position: Int) {
                                    val offer_id = offersListing[position].offer_id
                                    val bundle = Bundle()
                                    bundle.putInt("offer_id", offer_id!!)
                                    bundle.putString("status", "view")
                                    findNavController().navigate(R.id.action_myProfileFragment_to_addNewOffersFragment, bundle)
                                }
                            })
                        mView!!.rv_offers_n_discs.adapter = offersAndDiscountsAdapter
                    }else{
                        mView!!.rv_offers_n_discs.visibility = View.GONE
                        mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                    }
                }else{
                    mView!!.rv_offers_n_discs.visibility = View.GONE
                    mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<OffersListingResponse?>, throwable: Throwable) {
                mView!!.rv_offers_n_discs.visibility = View.GONE
                mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    private fun deleteOffer(offerId: Int, position: Int) {
        val call = apiInterface.deleteoffer(offerId, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<OfferDeleteResponse?>{
            override fun onResponse(call: Call<OfferDeleteResponse?>, response: Response<OfferDeleteResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        offersListing.removeAt(position)
                        if (mView!!.rv_offers_n_discs.adapter!=null){
                            mView!!.rv_offers_n_discs.adapter!!.notifyDataSetChanged()
                        }
                        if (offersListing.size==0){
                            mView!!.rv_offers_n_discs.visibility = View.GONE
                            mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_offers_n_discs.visibility = View.VISIBLE
                            mView!!.ll_no_offers_and_disc_found.visibility = View.GONE
                        }
                        Utility.showSnackBarOnResponseSuccess(mView!!.myProfileFragmentConstraintLayout,
                            requireContext().getString(R.string.offer_deleted_successfully),
                            requireContext())

                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<OfferDeleteResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    private fun getServices() {
        mView!!.fragment_profile_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.serviceslisting(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
        call!!.enqueue(object : Callback<ServiceListingResponse?>{
            override fun onResponse(
                call: Call<ServiceListingResponse?>,
                response: Response<ServiceListingResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting.clear()
                        serviceslisting = response.body()!!.service as ArrayList<Service>
                        mView!!.rv_services_listing.visibility = View.VISIBLE
                        mView!!.ll_no_service_found.visibility = View.GONE
                        mView!!.rv_services_listing.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        servicesAdapter = ServicesAdapter(requireContext(), serviceslisting,subscription_id, object : ClickInterface.onServicesItemClick{
                            override fun onServicClick(position: Int) {
                                val bundle = Bundle()
                                bundle.putStringArrayList("gallery",
                                    serviceslisting[position].gallery as ArrayList<String>?
                                )
                                findNavController().navigate(R.id.viewImageFragment, bundle)
                            }

                            override fun onServiceDele(position: Int) {
                                val service_id = serviceslisting.get(position).service_id
                                val deleteServiceDialog = AlertDialog.Builder(requireContext())
                                deleteServiceDialog.setCancelable(false)
                                deleteServiceDialog.setTitle(requireContext().getString(R.string.delete_service))
                                deleteServiceDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_service))
                                deleteServiceDialog.setPositiveButton(requireContext().getString(R.string.delete)
                                ) { dialog, _ ->
                                    deleteServices(service_id!!, position)
                                    dialog!!.dismiss()
                                }
                                deleteServiceDialog.setNegativeButton(requireContext().getString(R.string.cancel)
                                ) { dialog, _ -> dialog!!.cancel() }
                                deleteServiceDialog.show()
                            }

                            override fun onServiceEdit(position: Int) {
                                val updateServiceDialog = AlertDialog.Builder(requireContext())
                                updateServiceDialog.setCancelable(false)
                                updateServiceDialog.setTitle(requireContext().getString(R.string.update_service))
                                updateServiceDialog.setMessage(requireContext().getString(R.string.would_you_like_to_update_your_service_details))
                                updateServiceDialog.setPositiveButton(requireContext().getString(R.string.yes)
                                ) { dialog, _ ->
                                    val service_id = serviceslisting.get(position).service_id
                                    val bundle = Bundle()
                                    bundle.putInt("service_id", service_id!!)
                                    bundle.putString("status", "edit")
                                    findNavController().navigate(R.id.action_myProfileFragment_to_addNewFeaturedFragment, bundle)
                                    dialog!!.dismiss()
                                }
                                updateServiceDialog.setNegativeButton(requireContext().getString(R.string.no)
                                ) { dialog, _ -> dialog!!.cancel() }
                                updateServiceDialog.show()
                            }

                            override fun onServiceView(position: Int) {
                                val service_id = serviceslisting[position].service_id
                                val status = "show"
                                val bundle = Bundle()
                                bundle.putInt("service_id", service_id!!)
                                bundle.putString("status", status)
                                findNavController().navigate(R.id.action_myProfileFragment_to_addNewFeaturedFragment, bundle)
                            }
                        })
                        mView!!.rv_services_listing.adapter = servicesAdapter
                    }else{
                        mView!!.rv_services_listing.visibility = View.GONE
                        mView!!.ll_no_service_found.visibility = View.VISIBLE
                    }
                }else{
                    mView!!.rv_services_listing.visibility = View.GONE
                    mView!!.ll_no_service_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ServiceListingResponse?>, throwable: Throwable) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                mView!!.rv_services_listing.visibility = View.GONE
                mView!!.ll_no_service_found.visibility = View.VISIBLE
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    private fun deleteServices(serviceId: Int, position: Int) {
        mView!!.fragment_profile_progressBar.visibility = View.GONE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.deleteservice(serviceId,sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<DeleteServiceResponse?>{
            override fun onResponse(
                call: Call<DeleteServiceResponse?>,
                response: Response<DeleteServiceResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting.removeAt(position)
                        if (mView!!.rv_services_listing.adapter != null) {
                            mView!!.rv_services_listing.adapter!!.notifyDataSetChanged()
                        }
                        if (serviceslisting.size==0){
                            mView!!.rv_services_listing.visibility = View.GONE
                            mView!!.ll_no_service_found.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_services_listing.visibility = View.VISIBLE
                            mView!!.ll_no_service_found.visibility = View.GONE
                        }
                        Utility.showSnackBarOnResponseSuccess(mView!!.myProfileFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<DeleteServiceResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    private fun showGallery() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mView!!.fragment_profile_progressBar.visibility = View.VISIBLE
        val call = apiInterface.galleryListing(
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""],
            sharedPreferenceInstance!!.get(
                SharedPreferenceUtility.UserId,
                0
            )
        )
        call!!.enqueue(object : Callback<GalleryListResponse?> {
            override fun onResponse(
                call: Call<GalleryListResponse?>,
                response: Response<GalleryListResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL)
                staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        mView!!.rv_naqashat_gallery.visibility = View.VISIBLE
                        mView!!.ll_no_gallery_found.visibility = View.GONE
                        mView!!.rv_naqashat_gallery.layoutManager = staggeredGridLayoutManager
                        ImageUriList.clear()
                        ImageUriList = response.body()!!.gallery as ArrayList<Gallery>
                        galleryStaggeredGridAdapter = GalleryStaggeredGridAdapter(
                            requireContext(),
                            ImageUriList,
                            object : ClickInterface.OnGalleryItemClick {
                                override fun OnClickAction(position: Int) {
                                    val gallery_id = ImageUriList[position].gallery_id
                                    val deleteImageDialog = AlertDialog.Builder(requireContext())
                                    deleteImageDialog.setCancelable(false)
                                    deleteImageDialog.setTitle(requireContext().getString(R.string.delete_gallery_image))
                                    deleteImageDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_image))
                                    deleteImageDialog.setPositiveButton(requireContext().getString(R.string.delete)
                                    ) { dialog, _ ->
                                        deleteImage(gallery_id, position)
                                        dialog!!.dismiss()
                                    }
                                    deleteImageDialog.setNegativeButton(requireContext().getString(R.string.cancel)
                                    ) { dialog, _ -> dialog!!.cancel() }
                                    deleteImageDialog.show()
                                }

                                override fun onShowImage(position: Int) {
                                    galleryImageList.clear()
                                    galleryImageList.add(ImageUriList[position].image)

                                    val bundle = Bundle()
                                    bundle.putStringArrayList("gallery",
                                        galleryImageList as ArrayList<String>?
                                    )
                                    findNavController().navigate(R.id.viewImageFragment, bundle)
                                }

                            }
                        )
                        mView!!.rv_naqashat_gallery.adapter = galleryStaggeredGridAdapter
                    } else {
                        mView!!.rv_naqashat_gallery.visibility = View.GONE
                        mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                    }
                } else {
                    mView!!.rv_naqashat_gallery.visibility = View.GONE
                    mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<GalleryListResponse?>, throwable: Throwable) {
                mView!!.rv_naqashat_gallery.visibility = View.GONE
                mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
            }

        })
    }

    private fun deleteImage(galleryId: Int, position: Int) {
        val call = apiInterface.deletegalleryimage(galleryId, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<DeleteGalleryImage?> {
            override fun onResponse(
                call: Call<DeleteGalleryImage?>,
                response: Response<DeleteGalleryImage?>
            ) {
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            ImageUriList.removeAt(position)
                            if (mView!!.rv_naqashat_gallery.adapter != null) {
                                mView!!.rv_naqashat_gallery.adapter!!.notifyDataSetChanged()
                            }
                            if (ImageUriList.size==0){
                                mView!!.rv_naqashat_gallery.visibility = View.GONE
                                mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                            }else{
                                mView!!.rv_naqashat_gallery.visibility = View.VISIBLE
                                mView!!.ll_no_gallery_found.visibility = View.GONE
                            }
                            Utility.showSnackBarOnResponseSuccess(mView!!.myProfileFragmentConstraintLayout,
                                response.body()!!.message,
                                requireContext())

                        } else {
                            Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                                response.body()!!.message,
                                requireContext())
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
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

            override fun onFailure(call: Call<DeleteGalleryImage?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message!!.toString(),
                    requireContext())

            }
        })
    }

    private fun openCameraDialog() {
        val items = arrayOf<CharSequence>(
            getString(R.string.camera), getString(R.string.gallery), getString(
                R.string.cancel
            )
        )
        val builder = AlertDialog.Builder(requireActivity())
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                requireActivity(),
                BuildConfig.APPLICATION_ID.toString() + ".provider",
                getOutputMediaFile(
                    type
                )!!
            )
        } else {
            Uri.fromFile(getOutputMediaFile(type))
        }
    }

    private fun getOutputMediaFile(type: Int): File? {
        val mediaStorageDir = File(
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            IMAGE_DIRECTORY_NAME
        )
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        // Create a media file name
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val mediaFile: File
        mediaFile = if (type == MEDIA_TYPE_IMAGE) {
            File(
                mediaStorageDir.path + File.separator
                        + "IMG_" + timeStamp + ".png"
            )
        } else if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            File(
                mediaStorageDir.path + File.separator
                        + "VID_" + timeStamp + ".mp4"
            )
        } else {
            return null
        }
        return mediaFile
    }
    private fun chooseImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            status = PICK_IMAGE_FROM_GALLERY
            resultLauncher.launch(intent)
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            status = PICK_IMAGE_FROM_GALLERY
            resultLauncher.launch(intent)
        }

    }


    private fun getRealPath(ur: Uri?): String? {
        var realpath = ""
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

        // Get the cursor
        val cursor: Cursor = requireContext().contentResolver.query(ur!!,
            filePathColumn, null, null, null
        )!!
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        //Log.e("columnIndex", String.valueOf(MediaStore.Images.Media.DATA));
        realpath = cursor.getString(columnIndex)
        cursor.close()
        return realpath
    }

    private fun setUploadPhotos(galleryPhotos: ArrayList<String>) {
        mView!!.fragment_profile_progressBar.visibility = View.VISIBLE
        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        val builder = APIClient.createMultipartBodyBuilder(
            arrayOf("user_id", "lang"),
            arrayOf(
                sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0)
                    .toString(),
                sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
            )
        )
        for (i in 0 until galleryPhotos.size) {
            val file: File = File(galleryPhotos.get(i))
            builder!!.addFormDataPart(
                "image[]",
                file.name,
                RequestBody.create(MediaType.parse("image/*"), file)
            )
        }

        val call = apiInterface.uploadGallery(builder!!.build())
        call!!.enqueue(object : Callback<UploadGalleryResponse?> {
            override fun onResponse(
                call: Call<UploadGalleryResponse?>,
                response: Response<UploadGalleryResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            Utility.showSnackBarOnResponseSuccess(mView!!.myProfileFragmentConstraintLayout,
                                response.body()!!.message,
                                requireContext())
                            showGallery()
                        } else {
                            Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                                response.body()!!.message,
                                requireContext())
                        }
                    } else {
                        Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
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

            override fun onFailure(call: Call<UploadGalleryResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.myProfileFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())

                fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
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
        if (cursor != null) {
            cursor.moveToFirst()
            val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            galleryPhotos.add(imagePath)
            cursor.close()
        }
    }



    override fun onResume() {
        super.onResume()
        if (isButtonClicked.equals("Service")){
            mView!!.naqashat_services_layout.visibility = View.VISIBLE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            getServices()
            getOffers()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))
        }else if (isButtonClicked.equals("Gallery")){
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.VISIBLE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            showGallery()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))
        }else if (isButtonClicked.equals("Reviews")){
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.VISIBLE
            getReviews()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.white))
        }else{
            mView!!.naqashat_services_layout.visibility = View.VISIBLE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            getServices()
            getOffers()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))
        }
    }
}
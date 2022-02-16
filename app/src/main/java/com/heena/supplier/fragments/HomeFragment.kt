package com.heena.supplier.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.supplier.Dialogs.ExpiredMembershipDialogFragment
import com.heena.supplier.Dialogs.LogoutDialog
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.activities.HomeActivity
import com.heena.supplier.activities.LoginActivity
import com.heena.supplier.adapters.DashBoardBookingsAdapter
import com.heena.supplier.adapters.ServicesAdapter
import com.heena.supplier.bottomsheets.MembershipBottomSheetDialogFragment
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.SharedPreferenceUtility.Companion.SelectedLang
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_add_new_service.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_home.view.tv_no_bookings_found
import kotlinx.android.synthetic.main.fragment_my_profile.view.*
import kotlinx.android.synthetic.main.side_menu_layout.*
import kotlinx.android.synthetic.main.side_top_view.*
import kotlinx.android.synthetic.main.side_top_view.view.*
import kotlinx.android.synthetic.main.side_top_view.view.tv_location
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {
    lateinit var currentBookingsAdapter: DashBoardBookingsAdapter
    lateinit var servicesAdapter: ServicesAdapter
    var lang = ""
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    var serviceslisting = ArrayList<Service>()
    var bookingslisting = ArrayList<BookingItem>()
    private var membershipX : MembershipX?=null
    private var subscription : Subscriptions?=null
    private var subscription_id = 0
    private var membershipId :Int = 0
    var user_profile_name : String = ""
    var service : Service?= null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance()[SelectedLang, ""]
        )
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        instance = SharedPreferenceUtility.getInstance()
        lang = instance!![SelectedLang, ""].toString()

        Utility.setLanguage(requireContext(),lang)

        Log.e("userid", SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString())

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }


        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    getProfile()
                    getDashBoard()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Home Fragment")
        }else{
            getProfile()
            getDashBoard()
        }

        clickOnDrawer()
        clickOnHomeItems()
    }

    private fun getProfile() {
        mView!!.fragment_home_progressBar.visibility = VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showProfile(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0], SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<ProfileShowResponse?>{
            override fun onResponse(
                call: Call<ProfileShowResponse?>,
                response: Response<ProfileShowResponse?>
            ) {
                mView!!.fragment_home_progressBar.visibility = GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            val requestOption = RequestOptions().centerCrop()
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        return false
                                    }

                                    override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(requireActivity().menuImg)


                            Glide.with(requireContext()).load(response.body()!!.profile!!.image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =GONE
                                        return false
                                    }

                                    override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility = GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(requireActivity().headerView.userIcon)

                            user_profile_name = if(response.body()!!.profile!!.name.equals("")){
                                response.body()!!.profile!!.username!!
                            }else{
                                response.body()!!.profile!!.name!!
                            }

                            Log.e("username", user_profile_name)
                            Log.e("address", response.body()!!.profile!!.address!!)

                            requireActivity().tv_name.text = user_profile_name
                            requireActivity().tv_address.text = response.body()!!.profile!!.address!!
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
                mView!!.fragment_home_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun getDashBoard() {
        mView!!.fragment_home_progressBar.visibility = VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.getDashboard(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0], SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])

        call?.enqueue(object : Callback<DashboardResponse?>{
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<DashboardResponse?>, response: Response<DashboardResponse?>) {
                mView!!.fragment_home_progressBar.visibility = GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        mView!!.rv_services.visibility = VISIBLE
                        mView!!.tv_no_services_found.visibility = GONE
                        mView!!.rv_current_bookings.visibility = VISIBLE
                        mView!!.tv_no_bookings_found.visibility = GONE
                        membershipX = response.body()!!.membership!!
                        if (response.body()!!.subscriptions==null){
                            subscription = null
                            subscription_id = 0
                        }else{
                            subscription = response.body()!!.subscriptions!!
                            subscription_id = subscription!!.id
                        }

                        membershipId = membershipX!!.id
                        if (membershipId!=0){
                            mView!!.tv_membership_title_txt.text = response.body()!!.membership!!.name
                            mView!!.tv_membership_plan_price.text = "AED " + Utility.convertDoubleValueWithCommaSeparator(
                                response.body()!!.membership!!.amount!!.toDouble()
                            )
                            mView!!.linearprogressindicator.max = response.body()!!.membership!!.total_day
                            mView!!.linearprogressindicator1.max = response.body()!!.membership!!.total_day
                            mView!!.linearprogressindicator1.progress = response.body()!!.membership!!.day
                            mView!!.tv_expiration_date.text = response.body()!!.membership!!.end_date
                        }else{
                            val expiredMembershipDialogFragment = ExpiredMembershipDialogFragment()
                            expiredMembershipDialogFragment.isCancelable = false
                            expiredMembershipDialogFragment.subscribeCallback(object : ExpiredMembershipDialogFragment.SubscribeMembershipInterface{
                                override fun subscribe_membership() {
                                    expiredMembershipDialogFragment.dismiss()
                                    val membershipBottomSheetDialogFragment = MembershipBottomSheetDialogFragment.newInstance(requireContext(), membershipId)
                                    membershipBottomSheetDialogFragment.show(requireActivity().supportFragmentManager, MembershipBottomSheetDialogFragment.TAG)
                                    membershipBottomSheetDialogFragment.setSubscribeClickListenerCallback(object : MembershipBottomSheetDialogFragment.OnSubscribeClick{
                                        override fun OnSubscribe(membership: Membership) {
                                            val bundle = Bundle()
                                            Log.e("membership_data", ""+membership)
                                            bundle.putSerializable("membership", membership)
                                            bundle.putSerializable("subscription", null)
                                            bundle.putInt("direction", 1)
                                            findNavController().navigate(R.id.myPaymentFragment, bundle)
                                        }
                                    })
                                }

                            })
                            expiredMembershipDialogFragment.show(requireActivity().supportFragmentManager, "Home Fragment")
                        }
                        serviceslisting.clear()
                        bookingslisting.clear()
                        serviceslisting = response.body()!!.service as ArrayList<Service>
                        bookingslisting = response.body()!!.booking as ArrayList<BookingItem>
                        if (serviceslisting.size==0){
                            mView!!.rv_services.visibility = GONE
                            mView!!.tv_no_services_found.visibility = VISIBLE
                        }else{
                            mView!!.rv_services.visibility = VISIBLE
                            mView!!.tv_no_services_found.visibility = GONE
                        }
                        if (bookingslisting.size==0){
                            mView!!.rv_current_bookings.visibility = GONE
                            mView!!.tv_no_bookings_found.visibility = VISIBLE
                        }else{
                            mView!!.rv_current_bookings.visibility = VISIBLE
                            mView!!.tv_no_bookings_found.visibility = GONE
                        }
                        mView!!.rv_services.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                        servicesAdapter = ServicesAdapter(requireContext(), serviceslisting, subscription_id, object : ClickInterface.onServicesItemClick{
                            override fun onServicClick(position: Int) {
                                val bundle = Bundle()
                                bundle.putStringArrayList("gallery",
                                    serviceslisting[position].gallery as ArrayList<String>?
                                )
                                findNavController().navigate(R.id.viewImageFragment, bundle)
                            }

                            override fun onServiceDele(position: Int) {
                                val service_id = serviceslisting[position].service_id
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
                                    val service_id = serviceslisting[position].service_id
                                    val status = "edit"
                                    val bundle = Bundle()
                                    bundle.putInt("service_id", service_id!!)
                                    bundle.putString("status", status)
                                    findNavController().navigate(R.id.addNewServiceFragment, bundle)
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
                                findNavController().navigate(R.id.addNewServiceFragment, bundle)
                            }
                        })
                        mView!!.rv_services.adapter = servicesAdapter
                        servicesAdapter.notifyDataSetChanged()

                        mView!!.rv_current_bookings.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
                        currentBookingsAdapter = DashBoardBookingsAdapter(requireContext(), bookingslisting, object : ClickInterface.OnRecyclerItemClick{
                            override fun OnClickAction(position: Int) {
                                val bundle = Bundle()
                                bundle.putInt("booking_id", bookingslisting[position].booking_id!!)
                                findNavController().navigate(R.id.action_mainHomeFragment_to_bookingDetailsFragment, bundle)
                            }
                        })
                        mView!!.rv_current_bookings.adapter = currentBookingsAdapter
                        currentBookingsAdapter.notifyDataSetChanged()
                        mView!!.rv_current_bookings.scheduleLayoutAnimation()

                    }
                }else{
                    mView!!.rv_services.visibility = GONE
                    mView!!.tv_no_services_found.visibility = VISIBLE
                    mView!!.rv_current_bookings.visibility = GONE
                    mView!!.tv_no_bookings_found.visibility = VISIBLE
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<DashboardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                mView!!.fragment_home_progressBar.visibility= GONE
                mView!!.rv_services.visibility = GONE
                mView!!.tv_no_services_found.visibility = VISIBLE
                mView!!.rv_current_bookings.visibility = GONE
                mView!!.tv_no_bookings_found.visibility = VISIBLE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.shortToast(requireContext(), throwable.message)
            }

        })
    }

    private fun deleteServices(serviceId: Int, position: Int) {
        val call = apiInterface.deleteservice(serviceId,  SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<DeleteServiceResponse?>{
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<DeleteServiceResponse?>,
                response: Response<DeleteServiceResponse?>
            ) {
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting.removeAt(position)
                        if (mView!!.rv_services.adapter != null) {
                            mView!!.rv_services.adapter!!.notifyDataSetChanged()
                        }
                        LogUtils.longToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<DeleteServiceResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
            }

        })
    }

    private fun clickOnHomeItems() {
        mView!!.tv_viewall_txt.setSafeOnClickListener {
            val membershipBottomSheetDialogFragment = MembershipBottomSheetDialogFragment.newInstance(requireContext(), membershipId)
            membershipBottomSheetDialogFragment.show(requireActivity().supportFragmentManager, MembershipBottomSheetDialogFragment.TAG)
            membershipBottomSheetDialogFragment.setSubscribeClickListenerCallback(object : MembershipBottomSheetDialogFragment.OnSubscribeClick{
                override fun OnSubscribe(membership: Membership) {
                    val bundle = Bundle()
                    Log.e("membership_data", ""+membership)
                    bundle.putSerializable("membership", membership)
                    bundle.putSerializable("subscription", null)
                    bundle.putInt("direction", 1)
                    findNavController().navigate(R.id.myPaymentFragment, bundle)
                }
            })
        }

        mView!!.tv_viewall_txt2.setSafeOnClickListener {
            findNavController().navigate(R.id.myAppointmentsFragment)
        }
        mView!!.tv_viewall_txt3.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putInt("subscription_id", subscription_id)
            findNavController().navigate(R.id.myServicesFragment, bundle)
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }
    }

    private fun clickOnDrawer() {
        HomeActivity.clickDirestion = "drawer"
        requireActivity().llMyBanks.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llMyBanks)
            requireActivity().llMyBanks.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.bankDetailsFragment)
        }

        requireActivity().llAccount.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llAccount)
            requireActivity().llAccount.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putInt("subscription_id", subscription_id)
            findNavController().navigate(R.id.myProfileFragment, bundle)
        }

        requireActivity().llFeatured.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llFeatured)
            requireActivity().llFeatured.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            if (subscription!=null){
                val bundle = Bundle()
                Log.e("subscription_data", ""+subscription)
                bundle.putSerializable("subscription", subscription)
                findNavController().navigate(R.id.membershipStatusFragment, bundle)
            }else{
                findNavController().navigate(R.id.SubscriptionFragment)
            }
        }

        requireActivity().llNotifications.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llNotifications)
            requireActivity().llNotifications.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.notificationsFragment)
        }

        requireActivity().llRevenues.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llRevenues)
            requireActivity().llRevenues.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.revenuesFragment)
        }

        requireActivity().llMyCards.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llMyCards)
            requireActivity().llMyCards.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.myCardsFragment)
        }

        requireActivity().llMyLocation.setSafeOnClickListener {
            requireActivity().llMyLocation.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.myLocationsFragment)
        }

        requireActivity().llLanguages.setSafeOnClickListener {
            requireActivity().llLanguages.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.langFragment)
        }

        requireActivity().llAboutUs.setSafeOnClickListener {
            requireActivity().llAboutUs.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.about_us))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llContactUs.setSafeOnClickListener {
            requireActivity().llContactUs.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.contactUsFragment)
        }

        requireActivity().llTnC.setSafeOnClickListener {
            requireActivity().llTnC.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.terms_and_conditions))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llPrivacyPolicy.setSafeOnClickListener {
            requireActivity().llPrivacyPolicy.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.privacy_and_policy))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llFAQ.setSafeOnClickListener {
            requireActivity().llFAQ.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.frequently_asked_questions))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llHelpPage.setSafeOnClickListener {
            requireActivity().llHelpPage.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.helpFragment)
        }

        requireActivity().llLogout.setSafeOnClickListener {
            requireActivity().llLogout.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)

            val logoutDialog = LogoutDialog()
            logoutDialog.isCancelable = false
            logoutDialog.setDataCompletionCallback(object : LogoutDialog.LogoutInterface {
                override fun complete() {
                    logoutAPI()
                }
            })
            logoutDialog.show(requireActivity().supportFragmentManager, "HomeFragment")
        }

        requireActivity().llSettings.setSafeOnClickListener {
            requireActivity().llSettings.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.SettingsFragment)
        }
    }

    private fun logoutAPI() {
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            mView!!.fragment_home_progressBar.visibility = VISIBLE

            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)

            val call = apiInterface.logout(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0], SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
            call!!.enqueue(object : Callback<LogoutResponse?> {
                override fun onResponse(call: Call<LogoutResponse?>, response: Response<LogoutResponse?>) {
                    mView!!.fragment_home_progressBar.visibility = GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if (response.isSuccessful) {
                            if (response.body()!!.status == 1) {
                                SharedPreferenceUtility.getInstance().delete(SharedPreferenceUtility.IsLogin)
                                startActivity(Intent(requireContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                requireActivity().finishAffinity()
                            } else {
                                LogUtils.longToast(requireContext(), response.body()!!.message)
                            }
                        } else {
                            LogUtils.longToast(requireContext(), getString(R.string.response_isnt_successful))
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<LogoutResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                    mView!!.fragment_home_progressBar.visibility = GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            })

        }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @SuppressLint("StaticFieldLeak")
        var mView : View?=null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().iv_back.visibility = GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().iv_back.visibility = VISIBLE
    }

    override fun onStop() {
        super.onStop()
        requireActivity().iv_back.visibility = VISIBLE
    }
}
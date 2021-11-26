package com.dev.heenasupplier.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.dev.heenasupplier.Dialogs.LogoutDialog
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.activities.ChooseLoginSignUpActivity
import com.dev.heenasupplier.activities.HomeActivity
import com.dev.heenasupplier.adapters.DashBoardBookingsAdapter
import com.dev.heenasupplier.adapters.ServicesAdapter
import com.dev.heenasupplier.bottomsheets.MembershipBottomSheetDialogFragment
import com.dev.heenasupplier.models.*
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.SharedPreferenceUtility.Companion.SelectedLang
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_home.view.tv_no_bookings_found
import kotlinx.android.synthetic.main.side_menu_layout.*
import kotlinx.android.synthetic.main.side_top_view.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class HomeFragment : Fragment() {
    lateinit var currentBookingsAdapter: DashBoardBookingsAdapter
    lateinit var servicesAdapter: ServicesAdapter
    var lang = ""
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    var serviceslisting = ArrayList<Service>()
    var bookingslisting = ArrayList<BookingItem>()
    private var membershipX : MembershipX?=null
    private var membershipId :Int = 0
    var profile_picture : String = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        instance = SharedPreferenceUtility.getInstance()
        lang = instance!!.get(SelectedLang,"").toString()
        Utility.setLanguage(requireContext(),lang)

        Log.e("userid", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0).toString())

        profile_picture = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ProfilePic,"")

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    getDashBoard()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Home Fragment")
        }

        val requestOption = RequestOptions().centerCrop()
        Glide.with(this).load(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ProfilePic, ""))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                        Log.e("err", p0?.message.toString())
                        return false
                    }

                    override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {


                        return false
                    }
                }).apply(requestOption).into(requireActivity().menuImg)


        Glide.with(this).load(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ProfilePic, ""))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                        Log.e("err", p0?.message.toString())
                        return false
                    }

                    override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {


                        return false
                    }
                }).apply(requestOption).into(requireActivity().headerView.userIcon)

        getDashBoard()
        clickOnDrawer()
        clickOnHomeItems()
    }

    private fun getDashBoard() {
        mView!!.fragment_home_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.getDashboard(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0))

        call?.enqueue(object : Callback<DashboardResponse?>{
            override fun onResponse(call: Call<DashboardResponse?>, response: Response<DashboardResponse?>) {
                mView!!.fragment_home_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        mView!!.rv_services.visibility = View.VISIBLE
                        mView!!.tv_no_services_found.visibility = View.GONE
                        mView!!.rv_current_bookings.visibility = View.VISIBLE
                        mView!!.tv_no_bookings_found.visibility = View.GONE
                        membershipX = response.body()!!.membership!!
                        membershipId = membershipX!!.id
                        mView!!.tv_membership_title_txt.text = response.body()!!.membership!!.name
                        mView!!.tv_membership_plan_price.text = "AED " +response.body()!!.membership!!.amount.toString()
                        mView!!.linearprogressindicator.max = response.body()!!.membership!!.total_day
                        mView!!.linearprogressindicator1.max = response.body()!!.membership!!.total_day
                        mView!!.linearprogressindicator1.progress = response.body()!!.membership!!.day
                        mView!!.tv_expiration_date.text = response.body()!!.membership!!.end_date
                        serviceslisting = response.body()!!.service as ArrayList<Service>
                        bookingslisting = response.body()!!.booking as ArrayList<BookingItem>
                        if (serviceslisting.size==0){
                            mView!!.rv_services.visibility = View.GONE
                            mView!!.tv_no_services_found.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_services.visibility = View.VISIBLE
                            mView!!.tv_no_services_found.visibility = View.GONE
                        }
                        if (bookingslisting.size==0){
                            mView!!.rv_current_bookings.visibility = View.GONE
                            mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_current_bookings.visibility = View.VISIBLE
                            mView!!.tv_no_bookings_found.visibility = View.GONE
                        }
                        mView!!.rv_services.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                        servicesAdapter = ServicesAdapter(requireContext(), serviceslisting, object : ClickInterface.onServicesItemClick{
                            override fun onServicClick(position: Int) {
                                val bundle = Bundle()
                                bundle.putStringArrayList("gallery",
                                    serviceslisting[position].gallery as ArrayList<String>?
                                )
                                findNavController().navigate(R.id.viewImageFragment, bundle)
                            }

                            override fun onServiceDele(position: Int) {
                                val service_id = serviceslisting.get(position).service_id
                                deleteServices(service_id!!, position)
                            }

                            override fun onServiceEdit(position: Int) {
                                val service_id = serviceslisting.get(position).service_id
                                val status = "edit"
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
                    mView!!.rv_services.visibility = View.GONE
                    mView!!.tv_no_services_found.visibility = View.VISIBLE
                    mView!!.rv_current_bookings.visibility = View.GONE
                    mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<DashboardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                mView!!.fragment_home_progressBar.visibility= View.GONE
                mView!!.rv_services.visibility = View.GONE
                mView!!.tv_no_services_found.visibility = View.VISIBLE
                mView!!.rv_current_bookings.visibility = View.GONE
                mView!!.tv_no_bookings_found.visibility = View.VISIBLE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.shortToast(requireContext(), throwable.message)
            }

        })
    }

    private fun deleteServices(serviceId: Int, position: Int) {
        val call = apiInterface.deleteservice(serviceId)
        call!!.enqueue(object : Callback<DeleteServiceResponse?>{
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
        mView!!.tv_viewall_txt.setOnClickListener {
            val membershipBottomSheetDialogFragment = MembershipBottomSheetDialogFragment.newInstance(requireContext(), membershipId)
            membershipBottomSheetDialogFragment.show(requireActivity().supportFragmentManager, MembershipBottomSheetDialogFragment.TAG)
            membershipBottomSheetDialogFragment.setSubscribeClickListenerCallback(object : MembershipBottomSheetDialogFragment.OnSubscribeClick{
                override fun OnSubscribe(membership: Membership) {
                    val bundle = Bundle()
                    Log.e("membership_data", ""+membership)
                    bundle.putSerializable("membership", membership)
                    findNavController().navigate(R.id.myPaymentFragment, bundle)
                }
            })
        }

        mView!!.membership_card.setOnClickListener {
            val bundle = Bundle()
            Log.e("membership_data", ""+membershipX)
            bundle.putSerializable("membershipX", membershipX)
            findNavController().navigate(R.id.membershipStatusFragment, bundle)
        }

        mView!!.tv_viewall_txt2.setOnClickListener {
            findNavController().navigate(R.id.myAppointmentsFragment)
        }
        mView!!.tv_viewall_txt3.setOnClickListener {
            findNavController().navigate(R.id.myServicesFragment)
        }

        requireActivity().iv_notification.setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }
    }

    private fun clickOnDrawer() {
        HomeActivity.clickDirestion = "drawer"
        requireActivity().llMyBanks.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llMyBanks)
            requireActivity().llMyBanks.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.bankDetailsFragment)
        }

        requireActivity().llAccount.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llAccount)
            requireActivity().llAccount.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.myProfileFragment)
        }

        requireActivity().llFeatured.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llFeatured)
            requireActivity().llFeatured.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.SubscriptionFragment)
        }

        requireActivity().llNotifications.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llNotifications)
            requireActivity().llNotifications.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.notificationsFragment)
        }

        requireActivity().llRevenues.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llRevenues)
            requireActivity().llRevenues.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.revenuesFragment)
        }

        requireActivity().llMyCards.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().llMyCards)
            requireActivity().llMyCards.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.myCardsFragment)
        }

        requireActivity().llMyLocation.setOnClickListener {
            requireActivity().llMyLocation.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.myLocationsFragment)
        }

        requireActivity().llLanguages.setOnClickListener {
            requireActivity().llLanguages.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.langFragment)
        }

        requireActivity().llAboutUs.setOnClickListener {
            requireActivity().llAboutUs.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.about_us))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llContactUs.setOnClickListener {
            requireActivity().llContactUs.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.contactUsFragment)
        }

        requireActivity().llTnC.setOnClickListener {
            requireActivity().llTnC.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.terms_and_conditions))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llPrivacyPolicy.setOnClickListener {
            requireActivity().llPrivacyPolicy.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.privacy_and_policy))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llFAQ.setOnClickListener {
            requireActivity().llFAQ.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            val bundle = Bundle()
            bundle.putString("title", getString(R.string.frequently_asked_questions))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        requireActivity().llHelpPage.setOnClickListener {
            requireActivity().llHelpPage.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.helpFragment)
        }

        requireActivity().llLogout.setOnClickListener {
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

        requireActivity().llSettings.setOnClickListener {
            requireActivity().llSettings.startAnimation(AlphaAnimation(1f, 0.5f))
            requireActivity().drawerLayout.closeDrawer(GravityCompat.START)
            findNavController().navigate(R.id.SettingsFragment)
        }
    }

    private fun logoutAPI() {
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            mView!!.fragment_home_progressBar.visibility = View.VISIBLE

            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)

            val call = apiInterface.logout(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0))
            call!!.enqueue(object : Callback<LogoutResponse?> {
                override fun onResponse(call: Call<LogoutResponse?>, response: Response<LogoutResponse?>) {
                    mView!!.fragment_home_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if (response.isSuccessful) {
                            if (response.body()!!.status == 1) {
                                SharedPreferenceUtility.getInstance().delete(SharedPreferenceUtility.IsLogin)
                                startActivity(Intent(requireContext(), ChooseLoginSignUpActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
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
                    mView!!.fragment_home_progressBar.visibility = View.GONE
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
        requireActivity().iv_back.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().iv_back.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        requireActivity().iv_back.visibility = View.VISIBLE
    }
}
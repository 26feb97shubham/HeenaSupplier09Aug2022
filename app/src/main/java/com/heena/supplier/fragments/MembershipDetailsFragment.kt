package com.heena.supplier.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.ServicesAdapter
import com.heena.supplier.models.*
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_membership_details.*
import kotlinx.android.synthetic.main.fragment_membership_details.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembershipDetailsFragment : Fragment() {
    private var subscriptions : Subscriptions?=null
    private var subscription_id : Int = 0
    private var mView : View?=null
    var serviceslisting = ArrayList<Service>()
    lateinit var servicesAdapter: ServicesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subscriptions = it.getSerializable("subscription") as Subscriptions?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_membership_details, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        if (subscriptions!=null){
            subscription_id = subscriptions!!.id
        }else{
            subscription_id = 0
        }

        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getServices()

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        Log.e("membership", ""+subscriptions)
        mView!!.tv_membership_plan_price_details.text = "AED " +subscriptions!!.amount.toString()
        mView!!.linearprogressindicator_details.max = subscriptions!!.days
        mView!!.linearprogressindicator1_details.max = subscriptions!!.days
        mView!!.linearprogressindicator1_details.progress = subscriptions!!.ended_day
        mView!!.tv_expiration_date_details.text = subscriptions!!.end_date

        tv_add_new_featured!!.setSafeOnClickListener {
            var service_id = 0
            var status = "add"
            val bundle = Bundle()
            bundle.putInt("service_id", service_id)
            bundle.putInt("subscription_id", subscription_id)
            bundle.putString("status", status)
            findNavController().navigate(R.id.action_membershipStatusFragment_to_addNewFeaturedFragment, bundle)
        }
    }

    private fun getServices() {
        frag_membership_details_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.featuredserviceslisting(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0),
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        call!!.enqueue(object : Callback<FeaturedServicesListingResponse?> {
            override fun onResponse(
                    call: Call<FeaturedServicesListingResponse?>,
                    response: Response<FeaturedServicesListingResponse?>
            ) {
                mView!!.frag_membership_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting.clear()
                        serviceslisting = response.body()!!.service as ArrayList<Service>
                        mView!!.rv_add_new_featured_listing.visibility = View.VISIBLE
                        mView!!.ll_no_services_found_membership_details.visibility = View.GONE
                        mView!!.rv_add_new_featured_listing.layoutManager = LinearLayoutManager(
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
                                val serviceid = serviceslisting.get(position).service_id
                                val deleteServiceDialog = AlertDialog.Builder(requireContext())
                                deleteServiceDialog.setCancelable(false)
                                deleteServiceDialog.setTitle(requireContext().getString(R.string.delete_service))
                                deleteServiceDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_service))
                                deleteServiceDialog.setPositiveButton(requireContext().getString(R.string.delete)
                                ) { dialog, _ ->
                                    deleteServices(serviceid!!, position)
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
                                    val status = "edit"
                                    val bundle = Bundle()
                                    bundle.putInt("service_id", service_id!!)
                                    bundle.putString("status", status)
                                    findNavController().navigate(R.id.action_membershipStatusFragment_to_addNewFeaturedFragment, bundle)
                                    dialog!!.dismiss()
                                }
                                updateServiceDialog.setNegativeButton(requireContext().getString(R.string.no)
                                ) { dialog, _ -> dialog!!.cancel() }
                                updateServiceDialog.show()
                            }

                        })
                        mView!!.rv_add_new_featured_listing.adapter = servicesAdapter
                        servicesAdapter.notifyDataSetChanged()
                    }else{
                        mView!!.rv_add_new_featured_listing.visibility = View.GONE
                        mView!!.ll_no_services_found_membership_details.visibility = View.VISIBLE
                    }
                }else{
                    mView!!.rv_add_new_featured_listing.visibility = View.GONE
                    mView!!.ll_no_services_found_membership_details.visibility = View.VISIBLE
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<FeaturedServicesListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                mView!!.frag_membership_details_progressBar.visibility= View.GONE
                mView!!.rv_add_new_featured_listing.visibility = View.GONE
                mView!!.ll_no_services_found_membership_details.visibility = View.VISIBLE
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
                        if (mView!!.rv_add_new_featured_listing.adapter != null) {
                            mView!!.rv_add_new_featured_listing.adapter!!.notifyDataSetChanged()
                        }
                        if (serviceslisting.size==0){
                            mView!!.rv_add_new_featured_listing.visibility = View.GONE
                            mView!!.ll_no_services_found_membership_details.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_add_new_featured_listing.visibility = View.VISIBLE
                            mView!!.ll_no_services_found_membership_details.visibility = View.GONE
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
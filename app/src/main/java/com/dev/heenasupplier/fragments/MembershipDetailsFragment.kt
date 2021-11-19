package com.dev.heenasupplier.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.AddNewFeaturedAdapter
import com.dev.heenasupplier.adapters.ServicesAdapter
import com.dev.heenasupplier.models.DeleteServiceResponse
import com.dev.heenasupplier.models.MembershipX
import com.dev.heenasupplier.models.Service
import com.dev.heenasupplier.models.ServiceListingResponse
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_home.view.tv_membership_title_txt
import kotlinx.android.synthetic.main.fragment_membership_details.*
import kotlinx.android.synthetic.main.fragment_membership_details.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembershipDetailsFragment : Fragment() {
    lateinit var addNewFeaturedAdapter: AddNewFeaturedAdapter
    private var membershipX : MembershipX?=null
    private var mView : View?=null
    var serviceslisting = ArrayList<Service>()
    lateinit var servicesAdapter: ServicesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            membershipX = it.getSerializable("membershipX") as MembershipX?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_membership_details, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getServices()

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        Log.e("membership", ""+membershipX)
        mView!!.tv_membership_title_txt_details.text = membershipX!!.name
        mView!!.tv_membership_plan_price_details.text = "AED " +membershipX!!.amount.toString()
        mView!!.linearprogressindicator_details.max = membershipX!!.total_day
        mView!!.linearprogressindicator1_details.max = membershipX!!.total_day
        mView!!.linearprogressindicator1_details.progress = membershipX!!.day
        mView!!.tv_expiration_date_details.text = membershipX!!.end_date

        tv_add_new_featured!!.setOnClickListener {
            var service_id = 0
            var status = "add"
            val bundle = Bundle()
            bundle.putInt("service_id", service_id)
            bundle.putString("status", status)
            findNavController().navigate(R.id.action_membershipStatusFragment_to_addNewFeaturedFragment, bundle)
        }
    }

    private fun getServices() {
        frag_membership_details_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.serviceslisting(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0),
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        call!!.enqueue(object : Callback<ServiceListingResponse?> {
            override fun onResponse(
                    call: Call<ServiceListingResponse?>,
                    response: Response<ServiceListingResponse?>
            ) {
                mView!!.frag_membership_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting = response.body()!!.service as ArrayList<Service>
                        if (serviceslisting.size==0){
                            mView!!.rv_add_new_featured_listing.visibility = View.GONE
                            mView!!.ll_no_services_found_membership_details.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_add_new_featured_listing.visibility = View.VISIBLE
                            mView!!.ll_no_services_found_membership_details.visibility = View.GONE
                        }
                        mView!!.rv_add_new_featured_listing.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                        )

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
                                findNavController().navigate(R.id.action_membershipStatusFragment_to_addNewFeaturedFragment, bundle)
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

            override fun onFailure(call: Call<ServiceListingResponse?>, throwable: Throwable) {
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
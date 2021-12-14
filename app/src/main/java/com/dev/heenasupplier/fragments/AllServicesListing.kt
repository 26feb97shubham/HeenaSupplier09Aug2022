package com.dev.heenasupplier.fragments

import android.os.Bundle
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
import com.dev.heenasupplier.adapters.ServicesAdapter
import com.dev.heenasupplier.models.DeleteServiceResponse
import com.dev.heenasupplier.models.Service
import com.dev.heenasupplier.models.ServiceListingResponse
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.activity_notifications_fragment.view.*
import kotlinx.android.synthetic.main.fragment_all_services_listing.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_my_profile.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AllServicesListing : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mView : View? = null
    var serviceslisting = ArrayList<Service>()
    lateinit var servicesAdapter: ServicesAdapter
    private var subscription_id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           subscription_id = it.getInt("subscription_id")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_all_services_listing, container, false)
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        getServices(false)
        mView!!.all_servieces_swipeRefresh.setOnRefreshListener {
            getServices(true)
        }
        return mView
    }

    private fun getServices(isRefresh: Boolean) {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if(!isRefresh) {
            mView!!.all_services_frag_progressBar.visibility = View.VISIBLE
        }
        val call = apiInterface.serviceslisting(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0),
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        call!!.enqueue(object : Callback<ServiceListingResponse?> {
            override fun onResponse(
                    call: Call<ServiceListingResponse?>,
                    response: Response<ServiceListingResponse?>
            ) {
                mView!!.all_services_frag_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if(mView!!.all_servieces_swipeRefresh.isRefreshing){
                    mView!!.all_servieces_swipeRefresh.isRefreshing=false
                }
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting = response.body()!!.service as ArrayList<Service>
                        if (serviceslisting.size==0){
                            mView!!.noServicesView.visibility = View.VISIBLE
                            mView!!.rvServicesList.visibility = View.GONE
                        }else{
                            mView!!.noServicesView.visibility = View.GONE
                            mView!!.rvServicesList.visibility = View.VISIBLE
                        }
                        mView!!.rvServicesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                        servicesAdapter = ServicesAdapter(requireContext(), serviceslisting, subscription_id,object : ClickInterface.onServicesItemClick{
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
                                findNavController().navigate(R.id.action_myServicesFragment_to_addNewFeaturedFragment, bundle)
                            }

                        })
                        mView!!.rvServicesList.adapter = servicesAdapter
                        servicesAdapter.notifyDataSetChanged()
                    }else{
                        mView!!.noServicesView.visibility = View.VISIBLE
                        mView!!.rvServicesList.visibility = View.GONE
                    }
                }else{
                    mView!!.noServicesView.visibility = View.VISIBLE
                    mView!!.rvServicesList.visibility = View.GONE
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<ServiceListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.all_services_frag_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if(mView!!.all_servieces_swipeRefresh.isRefreshing){
                    mView!!.all_servieces_swipeRefresh.isRefreshing=false
                }
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
                        if (mView!!.rvServicesList.adapter != null) {
                            mView!!.rvServicesList.adapter!!.notifyDataSetChanged()
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                AllServicesListing().apply {
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
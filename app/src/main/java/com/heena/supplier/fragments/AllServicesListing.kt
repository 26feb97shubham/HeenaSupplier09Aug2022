package com.heena.supplier.fragments

import android.os.Bundle
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
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.DeleteServiceResponse
import com.heena.supplier.models.Service
import com.heena.supplier.models.ServiceListingResponse
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_all_services_listing.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllServicesListing : Fragment() {
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
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
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
        val call = apiInterface.serviceslisting(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0),
                sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, ""))
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
                        serviceslisting.clear()
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
                                    val status = "edit"
                                    val bundle = Bundle()
                                    bundle.putInt("service_id", service_id!!)
                                    bundle.putString("status", status)
                                    findNavController().navigate(R.id.action_myServicesFragment_to_addNewFeaturedFragment, bundle)
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
                                findNavController().navigate(R.id.action_myServicesFragment_to_addNewFeaturedFragment, bundle)
                            }
                        })
                        mView!!.rvServicesList.adapter = servicesAdapter
                    }else{
                        mView!!.noServicesView.visibility = View.VISIBLE
                        mView!!.rvServicesList.visibility = View.GONE
                        Utility.showSnackBarOnResponseError(mView!!.allServicesListingFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                }else{
                    mView!!.noServicesView.visibility = View.VISIBLE
                    mView!!.rvServicesList.visibility = View.GONE
                    Utility.showSnackBarOnResponseError(mView!!.allServicesListingFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ServiceListingResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarValidationError(mView!!.allServicesListingFragmentConstraintLayout,
                    throwable.message!!.toString(),
                    requireContext())
                mView!!.all_services_frag_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if(mView!!.all_servieces_swipeRefresh.isRefreshing){
                    mView!!.all_servieces_swipeRefresh.isRefreshing=false
                }
            }

        })
    }

    private fun deleteServices(serviceId: Int, position: Int) {
        val call = apiInterface.deleteservice(serviceId, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
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

                        if (serviceslisting.size==0){
                            mView!!.noServicesView.visibility = View.VISIBLE
                            mView!!.rvServicesList.visibility = View.GONE
                        }else{
                            mView!!.noServicesView.visibility = View.GONE
                            mView!!.rvServicesList.visibility = View.VISIBLE
                        }
                        Utility.showSnackBarOnResponseSuccess(mView!!.allServicesListingFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.allServicesListingFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.allServicesListingFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<DeleteServiceResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.allServicesListingFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
            }
        })
    }
}
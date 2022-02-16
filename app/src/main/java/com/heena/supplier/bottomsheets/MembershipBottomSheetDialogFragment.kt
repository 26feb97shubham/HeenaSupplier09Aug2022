package com.heena.supplier.bottomsheets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.adapters.MembershipPlansListAdapter
import com.heena.supplier.models.Membership
import com.heena.supplier.models.MembershipListResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.fragment_membership_bottom_sheet_dialog.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MembershipBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var OnSubscribeCallback : OnSubscribeClick?= null
    private lateinit var membershipPlansListAdapter: MembershipPlansListAdapter
    private var membershipList = ArrayList<Membership>()
    private var membership : Membership?=null
    var membershipId : Int = 0
    private var mView : View?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView  = inflater.inflate(
                R.layout.fragment_membership_bottom_sheet_dialog, container, false)
        getMembershipList()
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mView!!.tv_subscribe!!.setSafeOnClickListener { // dismiss dialog
            if (membershipPlansListAdapter.getSelected()!=null){
                membershipId = membershipPlansListAdapter.getSelected()!!.membership_id
                if (dashboardMembershipId==membershipId){
                    LogUtils.shortToast(requireContext(), getString(R.string.plan_already_purchased))
                }else{
                    membership?.let { it1 -> OnSubscribeCallback!!.OnSubscribe(it1) }
                    dismiss()
                }
            }else{
                LogUtils.shortToast(requireContext(), getString(R.string.please_select_the_membership_plan_first_to_continue))
            }
        }
    }

    private fun getMembershipList() {
        if(Utility.isNetworkAvailable()){
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            mView!!.membership_bottom_sheet_progressBar.visibility= View.VISIBLE
            val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
            val call = apiInterface.membershipList(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
            call!!.enqueue(object : Callback<MembershipListResponse?> {
                override fun onResponse(call: Call<MembershipListResponse?>, response: Response<MembershipListResponse?>) {
                    mView!!.membership_bottom_sheet_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    try {
                        if (response.isSuccessful){
                            if (response.body()!=null){
                                if(response.body()!!.status==1){
                                    membershipList = response.body()!!.membership as ArrayList<Membership>
                                    mView!!.rv_membership_plans.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                                    membershipPlansListAdapter = MembershipPlansListAdapter(requireContext(),membershipList, object : ClickInterface.OnRecyclerItemClick{
                                        override fun OnClickAction(position: Int) {
                                            membershipId  = membershipList[position].membership_id
                                            membership = membershipList[position]
                                        }
                                    })

                                    mView!!.rv_membership_plans.adapter = membershipPlansListAdapter
                                    mView!!.pageIndicator2.attachTo(mView!!.rv_membership_plans)
                                    membershipPlansListAdapter.notifyDataSetChanged()
                                }else{
                                    LogUtils.longToast(requireContext(), response.body()!!.message)
                                }

                            }
                        }else{
                            LogUtils.longToast(requireContext(),getString(R.string.response_isnt_successful))
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<MembershipListResponse?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    LogUtils.shortToast(requireContext(),throwable.localizedMessage)
                    mView!!.membership_bottom_sheet_progressBar.visibility = View.GONE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }

            })
        }
    }

    fun setSubscribeClickListenerCallback(OnSubscribeCallback: OnSubscribeClick){
        this.OnSubscribeCallback = OnSubscribeCallback
    }


    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }


    interface OnSubscribeClick{
        fun OnSubscribe(membership : Membership)
    }

    companion object {
        const val TAG = "MembershipBottomSheetDialogFragment"
        private var instance: SharedPreferenceUtility? = null
        private var dashboardMembershipId = 0
        fun newInstance(context: Context?, membershipId: Int): MembershipBottomSheetDialogFragment {
            //this.context = context;\
            dashboardMembershipId = membershipId
            return MembershipBottomSheetDialogFragment()
        }

        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

}
package com.heena.supplier.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.adapters.NotificationsAdapter
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.custom.SwipeToDeleteCallback
import com.heena.supplier.models.Notification
import com.heena.supplier.models.NotificationResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.rest.APIInterface
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.activity_notifications_fragment.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class NotificationsFragment : Fragment() {
    var notificationList=ArrayList<Notification>()
    lateinit var mView:View
    private var param1: String? = null
    private var param2: String? = null
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.activity_notifications_fragment, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        setUpViews()
        getNotifications(false)
        return mView
    }

    private fun getNotifications(isRefresh: Boolean) {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if(!isRefresh) {
            mView.notifications_frag_progressBar.visibility = View.VISIBLE
        }

        val call = apiInterface.getNotifications(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<NotificationResponse?>{
            override fun onResponse(call: Call<NotificationResponse?>, response: Response<NotificationResponse?>) {
                mView.notifications_frag_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if(mView.swipeRefresh.isRefreshing){
                    mView.swipeRefresh.isRefreshing=false
                }
                try {
                    if (response.isSuccessful){
                        if (response.body()!=null){
                            if (response.body()!!.status==1){
                                notificationList.clear()
                                notificationList = response.body()!!.notification
                                if (notificationList.size==0){
                                    mView.noNotificationView.visibility = View.VISIBLE
                                    mView.rvNotificationsList.visibility = View.GONE
                                }else{
                                    mView.noNotificationView.visibility = View.GONE
                                    mView.rvNotificationsList.visibility = View.VISIBLE
                                    setNotificationAdapter(notificationList)
                                }
                            }else{
                                mView.noNotificationView.visibility = View.VISIBLE
                                mView.rvNotificationsList.visibility = View.GONE
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.notificationsFragmentConstraintLayout,
                                response.message(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.notificationsFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<NotificationResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.notificationsFragmentConstraintLayout,
                    throwable.message.toString(),
                    requireContext())
                mView.notifications_frag_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if(mView.swipeRefresh.isRefreshing){
                    mView.swipeRefresh.isRefreshing=false
                }
            }

        })
    }

    private fun setNotificationAdapter(notificationList: ArrayList<Notification>) {
        mView.rvNotificationsList.apply {
            this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = NotificationsAdapter(requireContext(), notificationList)
        }
    }


    private fun setUpViews() {

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    getNotifications(true)
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Notification Fragment")
        }else{
            getNotifications(true)
        }

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        mView.swipeRefresh.setOnRefreshListener {
            getNotifications(true)
        }

       // swipeLeftToDeleteItem()
    }

   /* private fun swipeLeftToDeleteItem() {
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition //get position which is swipe
                if (direction == ItemTouchHelper.LEFT) {    //if swipe left
                    val builder = AlertDialog.Builder(requireContext()) //alert for confirm to delete
                    builder.setTitle(getString(R.string.delete)) //set message
                    builder.setMessage(getString(R.string.are_you_sure_you_want_to_delete_the_notification)) //set message
                    builder.setPositiveButton(getString(R.string.delete), DialogInterface.OnClickListener { dialog, which ->
                        notificationDelete(position)
                        //when click on DELETE
                        *//*notificationsAdapter.notifyItemRemoved(position) //item removed from recylcerview
                        *//**//* sqldatabase.execSQL("delete from " + TABLE_NAME.toString() + " where _id='" + (position + 1).toString() + "'") //query for delete*//**//*
                        notificationList.removeAt(position) //then remove item*//*
                        return@OnClickListener
                    }).setNegativeButton(getString(R.string.cancel), DialogInterface.OnClickListener { dialog, which ->

                        //not removing items if cancel is done
                        notificationsAdapter.notifyItemRemoved(position + 1) //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                        notificationsAdapter.notifyItemRangeChanged(position, notificationsAdapter.getItemCount()) //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                        return@OnClickListener
                    }).show().setCanceledOnTouchOutside(false) //show alert dialog
                }
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(mView.rvNotificationsList)
    }
    private fun notificationDelete(pos: Int) {
     *//*   requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView.progressBar.visibility= View.VISIBLE

        val apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)

        val builder = ApiClient.createBuilder(arrayOf("notification_id", "user_id", "lang"),
                arrayOf(notificationList[pos].id.toString(), sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))


        val call = apiInterface.notificationDelete(builder.build())
        call!!.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                mView.progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())

                        if(jsonObject.getInt("response")==1){
                            notificationsAdapter.notifyItemRemoved(pos)
                            notificationList.removeAt(pos)
                            notificationsAdapter.notifyDataSetChanged()
                        }
                        else{
                            LogUtils.shortToast(requireContext(), jsonObject.getString("message"))
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

            override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView.progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })*//*
    }*/

    override fun onResume() {
        super.onResume()
        requireActivity().iv_notification.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().iv_notification.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        requireActivity().iv_notification.visibility = View.VISIBLE
    }
}
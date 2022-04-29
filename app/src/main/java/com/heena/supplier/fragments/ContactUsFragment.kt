package com.heena.supplier.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.ContactUsResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_contact_us.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ContactUsFragment : Fragment() {
    private var mView : View ?= null
    var fullName : String = ""
    var emailAddress : String = ""
    var mymessage : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(
            R.layout.fragment_contact_us, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        setUpViews()
        return mView
    }

    private fun setUpViews() {
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

        mView!!.tv_contact_us_submit.setSafeOnClickListener {
           validate()
        }
    }

    private fun validate(){
        fullName = mView!!.et_fullname.text.toString().trim()
        emailAddress = mView!!.et_email_address.text.toString().trim()
        mymessage = mView!!.et_message.text.toString().trim()

        if (TextUtils.isEmpty(fullName)){
            Utility.showSnackBarValidationError(mView!!.contactUsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_full_name),
                requireContext())
        }else if(TextUtils.isEmpty(emailAddress)) {
            Utility.showSnackBarValidationError(mView!!.contactUsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_email_address),
                requireContext())
        }else if(!sharedPreferenceInstance!!.isEmailValid(emailAddress)) {
            Utility.showSnackBarValidationError(mView!!.contactUsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_email),
                requireContext())
        }else if (TextUtils.isEmpty(mymessage)){
            Utility.showSnackBarValidationError(mView!!.contactUsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_message),
                requireContext())
        }else{
            save()
        }
    }

    private fun save() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.fragment_contact_us_progressBar.visibility= View.VISIBLE
        val builder = APIClient.createBuilder(arrayOf("user_id","name","email","message","lang"),
                arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId,0).toString(),
                        fullName,
                        emailAddress,
                        mymessage,
                        sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang,"")))

        val call = apiInterface.contactUs(builder.build())
        call?.enqueue(object : Callback<ContactUsResponse?> {
            override fun onResponse(call: Call<ContactUsResponse?>, response: Response<ContactUsResponse?>) {
                mView!!.fragment_contact_us_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.contactUsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            findNavController().navigate(R.id.action_contactUsFragment_to_homeFragment)
                        } else{
                            Utility.showSnackBarOnResponseError(mView!!.contactUsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseSuccess(mView!!.contactUsFragmentConstraintLayout,
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

            override fun onFailure(call: Call<ContactUsResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseSuccess(mView!!.contactUsFragmentConstraintLayout,
                    throwable.message!!,
                    requireContext())
                mView!!.fragment_contact_us_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }
}
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
import com.heena.supplier.models.ContactUsResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.Companion.apiInterface
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
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        mView!!.tv_contact_us_submit.setOnClickListener {
           validate()
        }
    }

    private fun validate(){
        fullName = mView!!.et_fullname.text.toString().trim()
        emailAddress = mView!!.et_email_address.text.toString().trim()
        mymessage = mView!!.et_message.text.toString().trim()

        if (TextUtils.isEmpty(fullName)){
            mView!!.et_fullname.requestFocus()
            mView!!.et_fullname.error = getString(R.string.please_enter_your_full_name)
        }else if(TextUtils.isEmpty(emailAddress)) {
            mView!!.et_email_address.requestFocus()
            mView!!.et_email_address.error=getString(R.string.please_enter_valid_email)
        }else if(!SharedPreferenceUtility.getInstance().isEmailValid(emailAddress)) {
            mView!!.et_email_address.requestFocus()
            mView!!.et_email_address.error=getString(R.string.please_enter_valid_email)
        }else if (TextUtils.isEmpty(mymessage)){
            mView!!.et_message.requestFocus()
            mView!!.et_message.error = getString(R.string.please_enter_valid_message)
        }else{
            save()
        }
    }

    private fun save() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.fragment_contact_us_progressBar.visibility= View.VISIBLE
        val builder = APIClient.createBuilder(arrayOf("user_id","name","email","message","lang"),
                arrayOf(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0).toString(),
                        fullName,
                        emailAddress,
                        mymessage,
                        SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang,"")))

        val call = apiInterface.contactUs(builder.build())
        call?.enqueue(object : Callback<ContactUsResponse?> {
            override fun onResponse(call: Call<ContactUsResponse?>, response: Response<ContactUsResponse?>) {
                mView!!.fragment_contact_us_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if(response.isSuccessful){
                        if (response.body()!!.status==1){
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                            findNavController().navigate(R.id.action_contactUsFragment_to_homeFragment)
                        }
                        else{
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    }else{
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

            override fun onFailure(call: Call<ContactUsResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.fragment_contact_us_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
package com.heena.supplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_settings.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var mView : View?=null
    var profile_picture:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_settings, container, false)
        setUpViews()
        return mView
    }

    /*private fun showProfile() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar.visibility= View.VISIBLE
        val call = apiInterface.showProfile(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0))
        call?.enqueue(object : Callback<ProfileShowResponse?>{
            override fun onResponse(
                call: Call<ProfileShowResponse?>,
                response: Response<ProfileShowResponse?>
            ) {
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image).into(mView!!.civ_profile_image)
                            profile_picture = response.body()!!.profile!!.image.toString()
                            mView!!.tv_profileName.text = response.body()!!.profile!!.username
                          *//*  mView!!.tv_phone.text = response.body()!!.profile!!.phone
                            mView!!.tv_email.text = response.body()!!.profile!!.email*//*
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
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }*/

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        mView!!.txtChangePass.setOnClickListener {
            mView!!.txtChangePass.startAnimation(AlphaAnimation(1f, 0.5f))
            val args= Bundle()
            args.putString("profile_picture", profile_picture)
            findNavController().navigate(R.id.action_SettingsFragment_to_changePasswordFragment, args)
        }

        mView!!.btneditprofile.setOnClickListener {
            mView!!.btneditprofile.startAnimation(AlphaAnimation(1f, .5f))
            findNavController().navigate(R.id.action_SettingsFragment_to_editProfileFragment)
        }
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
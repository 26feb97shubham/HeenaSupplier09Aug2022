package com.heena.supplier.fragments

import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.models.ChangePasswordResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_change_password.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChangePasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChangePasswordFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mView : View?=null
    var oldPassword: String = ""
    var newPassword: String = ""
    var confirmPassword: String = ""
    var profile_picture: String = ""
    var oldPassVis=false
    var newPassVis=false
    var confmPassVis=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profile_picture = it.getString("profile_picture").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_change_password, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        mView!!.btnChangePass.setOnClickListener {
            mView!!.btnChangePass.startAnimation(AlphaAnimation(1f, 0.5f))
            validateAndChangePassword()
        }

        mView!!.edtNewPass.doOnTextChanged { charSeq, start, before, count ->
            if(SharedPreferenceUtility.getInstance().isPasswordValid(charSeq.toString())){
                mView!!.imgPassVerify.visibility=View.VISIBLE

            }
            else{
                mView!!.imgPassVerify.visibility=View.GONE

            }
        }

        mView!!.edtConfirmPassword.doOnTextChanged { charSeq, start, before, count ->
            val pass = mView!!.edtNewPass.text.toString()

            if(!TextUtils.isEmpty(pass)){
                if(!pass.equals(charSeq.toString(), false)){
                    mView!!.imgConfPassVerify.visibility=View.GONE
                    mView!!.txtPassMatch.visibility=View.GONE
                }
                else{
                    mView!!.imgConfPassVerify.visibility=View.VISIBLE
                    mView!!.txtPassMatch.visibility=View.VISIBLE
                    SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), mView!!.edtConfirmPassword)
                }
            }
            else{
                mView!!.edtNewPass.error=getString(R.string.please_first_enter_your_password)
            }
        }



        mView!!.imgEyeOldPass.setOnClickListener {
            if(oldPassVis){
                oldPassVis=false
                val start=mView!!.edtOldPass.selectionStart
                val end=mView!!.edtOldPass.selectionEnd
                mView!!.edtOldPass.transformationMethod = null
                mView!!.edtOldPass.setSelection(start, end)
                mView!!.imgEyeOldPass.setImageResource(R.drawable.visible)
            }
            else{
                oldPassVis=true
                val start=mView!!.edtOldPass.selectionStart
                val end=mView!!.edtOldPass.selectionEnd
                mView!!.edtOldPass.transformationMethod = PasswordTransformationMethod()
                mView!!.edtOldPass.setSelection(start, end)
                mView!!.imgEyeOldPass.setImageResource(R.drawable.invisible)
            }
        }

        mView!!.imgEyeNewPass.setOnClickListener {
            if(newPassVis){
                newPassVis=false
                val start=mView!!.edtNewPass.selectionStart
                val end=mView!!.edtNewPass.selectionEnd
                mView!!.edtNewPass.transformationMethod = null
                mView!!.edtNewPass.setSelection(start, end)
                mView!!.imgEyeNewPass.setImageResource(R.drawable.visible)
            }
            else{
                newPassVis=true
                val start=mView!!.edtNewPass.selectionStart
                val end=mView!!.edtNewPass.selectionEnd
                mView!!.edtNewPass.transformationMethod = PasswordTransformationMethod()
                mView!!.edtNewPass.setSelection(start, end)
                mView!!.imgEyeNewPass.setImageResource(R.drawable.invisible)
            }
        }
        mView!!.imgEyeConfPass.setOnClickListener {
            if(confmPassVis){
                confmPassVis=false
                val start=mView!!.edtConfirmPassword.selectionStart
                val end=mView!!.edtConfirmPassword.selectionEnd
                mView!!.edtConfirmPassword.transformationMethod = null
                mView!!.edtConfirmPassword.setSelection(start, end)
                mView!!.imgEyeConfPass.setImageResource(R.drawable.visible)
            }
            else{
                confmPassVis=true
                val start=mView!!.edtConfirmPassword.selectionStart
                val end=mView!!.edtConfirmPassword.selectionEnd
                mView!!.edtConfirmPassword.transformationMethod = PasswordTransformationMethod()
                mView!!.edtConfirmPassword.setSelection(start, end)
                mView!!.imgEyeConfPass.setImageResource(R.drawable.invisible)
            }
        }
    }

    private fun validateAndChangePassword() {
        oldPassword= mView!!.edtOldPass.text.toString()
        newPassword= mView!!.edtNewPass.text.toString()
        confirmPassword= mView!!.edtConfirmPassword.text.toString()

        if (TextUtils.isEmpty(oldPassword)) {
            mView!!.edtOldPass.requestFocus()
            mView!!.edtOldPass.error=getString(R.string.please_enter_your_old_password)
        }
        else if (!SharedPreferenceUtility.getInstance().isPasswordValid(oldPassword)) {
            mView!!.edtOldPass.requestFocus()
            mView!!.edtOldPass.error=getString(R.string.password_length_valid)
        }
        else if (TextUtils.isEmpty(newPassword)) {
            mView!!.edtNewPass.requestFocus()
            mView!!.edtNewPass.error=getString(R.string.please_enter_your_new_password)
        }
        else if (!SharedPreferenceUtility.getInstance().isPasswordValid(newPassword)) {
            mView!!.edtNewPass.requestFocus()
            mView!!.edtNewPass.error=getString(R.string.password_length_valid)
        }
        else if (TextUtils.isEmpty(confirmPassword)) {
            mView!!.edtConfirmPassword.requestFocus()
            mView!!.edtConfirmPassword.error=getString(R.string.please_verify_your_password)
        }
        else if (!confirmPassword.equals(newPassword)) {
            mView!!.edtConfirmPassword.requestFocus()
            mView!!.edtConfirmPassword.error=getString(R.string.password_doesnt_match_with_confirm_password)
        }
        else{
            changePassword()
        }

    }
    private fun changePassword() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar.visibility= View.VISIBLE

        val builder = APIClient.createBuilder(arrayOf("user_id", "new_pwd", "old_pwd", "lang"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString()
                , newPassword, oldPassword, SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].toString()))
        val call = apiInterface.changePassword(builder.build())
        call!!.enqueue(object : Callback<ChangePasswordResponse?> {
            override fun onResponse(call: Call<ChangePasswordResponse?>, response: Response<ChangePasswordResponse?>) {
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            findNavController().popBackStack()
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

            override fun onFailure(call: Call<ChangePasswordResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChangePasswordFragment().apply {
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
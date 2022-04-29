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
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.ChangePasswordResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_change_password.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ChangePasswordFragment : Fragment() {
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

        mView!!.btnChangePass.setSafeOnClickListener {
            mView!!.btnChangePass.startAnimation(AlphaAnimation(1f, 0.5f))
            validateAndChangePassword()
        }

        mView!!.edtNewPass.doOnTextChanged { charSeq, start, before, count ->
            if(sharedPreferenceInstance!!.isPasswordValid(charSeq.toString())){
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
                    sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), mView!!.edtConfirmPassword)
                }
            }
            else{
                Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                    requireContext().getString(R.string.please_first_enter_your_password),
                    requireContext())
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
            Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_old_password),
                requireContext())
        }
        else if (!sharedPreferenceInstance!!.isPasswordValid(oldPassword)) {
            Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                requireContext().getString(R.string.password_length_valid),
                requireContext())
        }else if (!oldPassword.equals(sharedPreferenceInstance!!.get(SharedPreferenceUtility.OldPassword, ""))){
            Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                requireContext().getString(R.string.password_doesnt_match_with_old_password),
                requireContext())
        }
        else if (TextUtils.isEmpty(newPassword)) {
            Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_new_password),
                requireContext())
        }
        else if (!sharedPreferenceInstance!!.isPasswordValid(newPassword)) {
            Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                requireContext().getString(R.string.password_length_valid),
                requireContext())
        }
        else if (TextUtils.isEmpty(confirmPassword)) {
            Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_confirm_password),
                requireContext())
        }
        else if (!confirmPassword.equals(newPassword)) {
            Utility.showSnackBarValidationError(mView!!.changePasswordFragmentConstraintLayout,
                requireContext().getString(R.string.password_doesnt_match_with_confirm_password),
                requireContext())
        }
        else{
            changePassword()
        }

    }
    private fun changePassword() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar.visibility= View.VISIBLE

        val builder = APIClient.createBuilder(arrayOf("user_id", "new_pwd", "old_pwd", "lang"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString()
                , newPassword, oldPassword, sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString()))
        val call = apiInterface.changePassword(builder.build())
        call!!.enqueue(object : Callback<ChangePasswordResponse?> {
            override fun onResponse(call: Call<ChangePasswordResponse?>, response: Response<ChangePasswordResponse?>) {
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.changePasswordFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            findNavController().popBackStack()
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.changePasswordFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.changePasswordFragmentConstraintLayout,
                            response.message(),
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

            override fun onFailure(call: Call<ChangePasswordResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.changePasswordFragmentConstraintLayout,
                    throwable.message!!,
                    requireContext())
                mView!!.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

    }
}
package com.heena.supplier.fragments

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.AddEditBankResponse
import com.heena.supplier.models.Bank
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_edit_bank_details.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

class EditBankDetailsFragment : Fragment() {
    private var mView : View ?= null
    private var isBankAdded = false

    private var bankName : String ?= null
    private var fullName : String ?= null
    private var acc_no : String ?= null
    private var iban_no : String ?= null

    private var bank : Bank?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isBankAdded = it.getBoolean("isBankAdded")
            bank = it.getSerializable("bank_details") as Bank?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(
            R.layout.fragment_edit_bank_details, container, false)
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

        if (!isBankAdded){
            mView!!.et_bank_name.hint = ""
            mView!!.et_fullname.hint = ""
            mView!!.et_acc_number.hint = ""
            mView!!.et_iban.hint = ""

            mView!!.et_bank_name.clearFocus()
            mView!!.et_fullname.clearFocus()
            mView!!.et_acc_number.clearFocus()
            mView!!.et_iban.clearFocus()

            mView!!.tv_save_bank_details.text = getString(R.string.save)

        }else{
            updateFields(bank)
            mView!!.tv_save_bank_details.text = getString(R.string.update)
        }

        mView!!.et_acc_number.doAfterTextChanged {
            if (it!!.length > 0 && it.length%5==0) {
                val c: Char = it[it.length-1]
                if (' ' == c) {
                    it.delete(it.length - 1, it.length)
                }
            }
            if (it.length > 0 && it.length%5 == 0) {
                val c: Char = it[it.length-1]
                val sizeArray = arrayOf(4,9,14,19)
                if (Character.isDigit(c) && TextUtils.split(it.toString(), " ").size <= it.length-1) {
                    it.insert(it.length - 1, " ")
                }
            }
        }

        mView!!.tv_save_bank_details.setSafeOnClickListener {
            validateandsave()
        }
    }

    private fun validateandsave() {
        bankName = mView!!.et_bank_name.text.toString().trim()
        fullName = mView!!.et_fullname.text.toString().trim()
        acc_no = mView!!.et_acc_number.text.toString().trim()
        iban_no = mView!!.et_iban.text.toString().trim()
        val accountNumber = acc_no!!.replace(" ", "")

        if (TextUtils.isEmpty(bankName)){
            Utility.showSnackBarValidationError(mView!!.editBankDetailsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_bank_name),
                requireContext())
        }else if (TextUtils.isEmpty(fullName)){
            Utility.showSnackBarValidationError(mView!!.editBankDetailsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_full_name),
                requireContext())
        }else if(TextUtils.isEmpty(acc_no)){
            Utility.showSnackBarValidationError(mView!!.editBankDetailsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_account_number),
                requireContext())
        }else if(!sharedPreferenceInstance!!.isAccountNoValid(accountNumber)){
            Utility.showSnackBarValidationError(mView!!.editBankDetailsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_account_number),
                requireContext())
        }else if(TextUtils.isEmpty(iban_no)){
            Utility.showSnackBarValidationError(mView!!.editBankDetailsFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_iban_number),
                requireContext())
        }else{
            save()
        }
    }

    private fun save() {
        mView!!.frag_edit_bank_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(arrayOf("user_id","full_name", "bank_name", "account_num", "iban", "lang"),
        arrayOf(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0).toString(),
        fullName.toString(),
        bankName.toString(),
        acc_no.toString(),
        iban_no.toString(),
        sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))

        val call = apiInterface.addEditBanks(builder.build())
        call?.enqueue(object : Callback<AddEditBankResponse?>{
            override fun onResponse(
                call: Call<AddEditBankResponse?>,
                response: Response<AddEditBankResponse?>
            ) {
                mView!!.frag_edit_bank_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.editBankDetailsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.IsBankAdded, true)
                            Handler().postDelayed({ findNavController().popBackStack() }, 1000)
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.editBankDetailsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.editBankDetailsFragmentConstraintLayout,
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

            override fun onFailure(call: Call<AddEditBankResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.editBankDetailsFragmentConstraintLayout,
                    throwable.message!!,
                    requireContext())
                mView!!.frag_edit_bank_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun updateFields(bank: Bank?) {
        mView!!.et_bank_name.setText( bank!!.bank_name)
        mView!!.et_fullname.setText( bank.full_name)
        mView!!.et_acc_number.setText( bank.account_num)
        mView!!.et_iban.setText( bank.iban)
    }

    override fun onResume() {
        super.onResume()
        if (!isBankAdded){
            mView!!.et_bank_name.hint = ""
            mView!!.et_fullname.hint = ""
            mView!!.et_acc_number.hint = ""
            mView!!.et_iban.hint = ""

            mView!!.et_bank_name.clearFocus()
            mView!!.et_fullname.clearFocus()
            mView!!.et_acc_number.clearFocus()
            mView!!.et_iban.clearFocus()

            mView!!.tv_save_bank_details.text = getString(R.string.save)

        }else{
            updateFields(bank)
            mView!!.tv_save_bank_details.text = getString(R.string.update)
        }
    }
}
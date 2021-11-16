package com.dev.heenasupplier.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.dev.heenasupplier.R
import com.dev.heenasupplier.models.AddEditBankResponse
import com.dev.heenasupplier.models.Bank
import com.dev.heenasupplier.models.BankDetailsResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility.Companion.apiInterface
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_edit_bank_details.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(
            R.layout.fragment_edit_bank_details, container, false)
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setOnClickListener {
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
            showBankDetails()
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

        mView!!.tv_save_bank_details.setOnClickListener {
           /* findNavController().navigate(R.id.action_editBankDetailsFragment_to_myCardsFragment)*/
            validateandsave()
        }
    }

    private fun validateandsave() {
        bankName = mView!!.et_bank_name.text.toString().trim()
        fullName = mView!!.et_fullname.text.toString().trim()
        acc_no = mView!!.et_acc_number.text.toString().trim()
        iban_no = mView!!.et_iban.text.toString().trim()

        if (TextUtils.isEmpty(bankName)){
            mView!!.et_bank_name.requestFocus()
            mView!!.et_bank_name.error = getString(R.string.please_enter_valid_bank_name)
        }else if (TextUtils.isEmpty(fullName)){
            mView!!.et_fullname.requestFocus()
            mView!!.et_fullname.error = getString(R.string.please_enter_your_full_name)
        }else if(TextUtils.isEmpty(acc_no)){
            mView!!.et_acc_number.requestFocus()
            mView!!.et_acc_number.error = getString(R.string.please_enter_valid_account_number)
        }else if(!SharedPreferenceUtility.getInstance().isAccountNoValid(acc_no!!)){
            mView!!.et_acc_number.requestFocus()
            mView!!.et_acc_number.error = getString(R.string.please_enter_valid_account_number)
        }else if(TextUtils.isEmpty(iban_no)){
            mView!!.et_iban.requestFocus()
            mView!!.et_iban.error = getString(R.string.please_enter_valid_iban)
        }/*else if(!SharedPreferenceUtility.getInstance().isIbanValid(iban_no!!)){
            mView!!.et_iban.requestFocus()
            mView!!.et_iban.error = getString(R.string.please_enter_valid_iban)
        }*/else{
            save()
        }
    }

    private fun save() {
        mView!!.frag_edit_bank_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val builder = APIClient.createBuilder(arrayOf("user_id","full_name", "bank_name", "account_num", "iban"),
        arrayOf(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString(),
        fullName.toString(),
        bankName.toString(),
        acc_no.toString(),
        iban_no.toString()))

        val call = apiInterface.addEditBanks(builder!!.build())
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
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                            findNavController().navigate(R.id.action_editBankDetailsFragment_to_bankDetailsFragment)
                        }else{
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    }else{
                        LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
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
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.frag_edit_bank_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun showBankDetails() {
        mView!!.frag_edit_bank_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val call  = apiInterface.showBanks(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0))
        call?.enqueue(object : Callback<BankDetailsResponse?>{
            override fun onResponse(
                call: Call<BankDetailsResponse?>,
                response: Response<BankDetailsResponse?>
            ) {
                mView!!.frag_edit_bank_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            bank = response.body()!!.bank
                            updateFields()
                        }else{
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    }else{
                        LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<BankDetailsResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.frag_edit_bank_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun updateFields() {
        mView!!.et_bank_name.setText( bank!!.bank_name)
        mView!!.et_fullname.setText( bank!!.full_name)
        mView!!.et_acc_number.setText( bank!!.account_num)
        mView!!.et_iban.setText( bank!!.iban)
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
            showBankDetails()
            mView!!.tv_save_bank_details.text = getString(R.string.update)
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
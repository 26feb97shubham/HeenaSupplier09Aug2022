package com.heena.supplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.models.Bank
import com.heena.supplier.models.BankDetailsResponse
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_bank_details.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class BankDetailsFragment : Fragment() {
    private var mView : View ?= null
    private var isBankAdded = false
    private var bank : Bank?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_bank_details, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        showBankDetails()
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.tv_edit_bank_details.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isBankAdded", isBankAdded)
            bundle.putSerializable("bank_details", bank)
            findNavController().navigate(R.id.action_bankDetailsFragment_to_editBankDetailsFragment, bundle)
        }

        if(!isBankAdded){
            mView!!.no_banks_found.visibility = View.VISIBLE
            mView!!.add_new_bank.visibility = View.VISIBLE
            mView!!.sv_bank_details.visibility = View.GONE
        }else{
            mView!!.no_banks_found.visibility = View.GONE
            mView!!.add_new_bank.visibility = View.GONE
            mView!!.sv_bank_details.visibility = View.VISIBLE
        }

        mView!!.add_new_bank.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isBankAdded", isBankAdded)
            findNavController().navigate(R.id.action_bankDetailsFragment_to_editBankDetailsFragment, bundle)
        }
    }

    private fun showBankDetails() {
        mView!!.frag_bank_details_progressBar.visibility = View.VISIBLE
        mView!!.no_banks_found.visibility = View.GONE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val call  = Utility.apiInterface.showBanks(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
        call?.enqueue(object : Callback<BankDetailsResponse?> {
            override fun onResponse(
                call: Call<BankDetailsResponse?>,
                response: Response<BankDetailsResponse?>
            ) {
                mView!!.frag_bank_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            mView!!.no_banks_found.visibility = View.GONE
                            mView!!.add_new_bank.visibility = View.GONE
                            mView!!.sv_bank_details.visibility = View.VISIBLE
                            isBankAdded = true
                            bank = response.body()!!.bank
                            updateFields()
                        }else{
                            mView!!.no_banks_found.visibility = View.VISIBLE
                            mView!!.add_new_bank.visibility = View.VISIBLE
                            mView!!.sv_bank_details.visibility = View.GONE
                            isBankAdded = false
                        }
                    }else{
                        mView!!.no_banks_found.visibility = View.VISIBLE
                        mView!!.add_new_bank.visibility = View.VISIBLE
                        mView!!.sv_bank_details.visibility = View.GONE
                        isBankAdded = false
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
                mView!!.no_banks_found.visibility = View.VISIBLE
                mView!!.add_new_bank.visibility = View.VISIBLE
                mView!!.sv_bank_details.visibility = View.GONE
                LogUtils.e("msg", throwable.message)
                isBankAdded = false
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.frag_bank_details_progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun updateFields() {
        mView!!.tv_bank_name.setText( bank!!.bank_name)
        mView!!.tv_fullname.setText( bank!!.full_name)
        mView!!.tv_acc_number.setText( bank!!.account_num)
        mView!!.tv_iban_number.setText( bank!!.iban)
    }

    override fun onResume() {
        super.onResume()
        showBankDetails()
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
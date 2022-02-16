package com.heena.supplier.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.heena.supplier.R
import com.heena.supplier.extras.AsteriskPasswordTransformationMethod
import com.heena.supplier.models.AddDeleteCardResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.changeLanguage
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_add_new_card.*
import kotlinx.android.synthetic.main.fragment_add_new_card.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddNewCardFragment : Fragment() {
    var mView : View ?= null
    var card_title = ""
    var card_number = ""
    var card_cvv = ""
    var card_expiry = ""
    val sdf = SimpleDateFormat("MM/yyyy", Locale.US)
    var expiry_date : Date?=null
    var expired = false
    private var keyDel = 0
    private var a =""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(
                R.layout.fragment_add_new_card, container, false)
        changeLanguage(requireContext(), SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        sdf.isLenient = false
        setUpViews()
        return mView
    }


    @SuppressLint("UseCompatLoadingForDrawables")
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

        mView!!.tv_save_card.setSafeOnClickListener {
            card_expiry = mView!!.et_expiry.text.toString().trim()
            if (card_expiry.equals("")){
                LogUtils.shortToast(requireContext(), getString(R.string.please_enter_valid_expiry_date))
            }else{
                expiry_date=sdf.parse(card_expiry)
                expired= expiry_date!!.before(Date())
                validateAndSave()
            }
        }

        addTextWatcher()

        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "").equals("en")){
            val drawable = requireContext().resources.getDrawable(R.drawable.card_bg)
            mView!!.ll_card.background = drawable
        }else{
            val drawable = requireContext().resources.getDrawable(R.drawable.arabic_card)
            mView!!.ll_card.background = drawable
        }
    }

    private fun validateAndSave() {
        card_title = mView!!.et_card_title.text.toString().trim()
        card_number = mView!!.et_card_number.text.toString().replace(" ", "").trim()
        card_cvv = mView!!.et_cvv.text.toString().trim()
        card_expiry = mView!!.et_expiry.text.toString().trim()

        if (TextUtils.isEmpty(card_title)){
            mView!!.et_card_title.requestFocus()
            mView!!.et_card_title.error = getString(R.string.please_enter_valid_card_title)
        }else if (TextUtils.isEmpty(card_number)){
            mView!!.et_card_number.requestFocus()
            mView!!.et_card_number.error = getString(R.string.please_enter_valid_card_number)
        }else if (card_number.length<16){
            mView!!.et_card_number.requestFocus()
            mView!!.et_card_number.error = getString(R.string.please_enter_valid_card_number)
        }else if (TextUtils.isEmpty(card_cvv)){
            mView!!.et_cvv.requestFocus()
            mView!!.et_cvv.error = getString(R.string.please_enter_valid_cvv)
        }else if (card_cvv.length<3){
            mView!!.et_cvv.requestFocus()
            mView!!.et_cvv.error = getString(R.string.please_enter_valid_cvv)
        }else if (expired){
            LogUtils.shortToast(requireContext(), getString(R.string.card_is_expired))
        }else{
            save()
        }
    }

    private fun save() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        progressBar_add_card.visibility= View.VISIBLE
        val builder = APIClient.createBuilder(arrayOf("user_id", "name", "number","cvv", "expiry_date", "type", "card_id", "lang"),
        arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(),
        card_title,
        card_number,
        card_cvv,
        card_expiry, "0", "", SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.addDeleteCard(builder.build())
        call!!.enqueue(object : Callback<AddDeleteCardResponse?>{
            override fun onResponse(
                call: Call<AddDeleteCardResponse?>,
                response: Response<AddDeleteCardResponse?>
            ) {
                progressBar_add_card.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                            findNavController().popBackStack()
                        }
                    }else{
                        LogUtils.longToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<AddDeleteCardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                progressBar_add_card.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun addTextWatcher(){
        mView!!.et_card_title.doOnTextChanged { text, start, before, count ->
            mView!!.tv_account_holdee_name_1.text = text
        }

        mView!!.et_card_number.doOnTextChanged { _, _, _, _ ->
            val text = mView!!.et_card_number
            var flag = true
            val eachBlock: List<String> = text.text.toString().split(" ")
            for (i in eachBlock.indices) {
                if (eachBlock[i].length > 4) {
                    flag = false
                }
            }
            if (flag) {
                text.setOnKeyListener { _, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_DEL) keyDel = 1
                    false
                }
                if (keyDel == 0) {
                    if ((text.text!!.length + 1) % 5 == 0) {
                        if (text.text.toString().split(" ").size <= 3) {
                            text.setText(text.text.toString() + " ")
                            text.setSelection(text?.text!!.length)
                        }
                    }
                    a = text?.text.toString()
                } else {
                    a = text?.text.toString()
                    keyDel = 0
                }
            } else {
                text.setText(a)
            }
            mView!!.tv_card_number_1.text = a
        }

        mView!!.et_expiry.doOnTextChanged { text, _, _, _ ->
            mView!!.tv_expiry_date_1.text = text
        }

        mView!!.et_expiry.doAfterTextChanged {
            if (it!!.length > 0 && it.length == 3) {
                val c: Char = it[it.length-1]
                if ('/' == c) {
                    it.delete(it.length - 1, it.length)
                }
            }
            if (it.length > 0 && it.length == 3) {
                val c: Char = it[it.length-1]
                if (Character.isDigit(c) && TextUtils.split(it.toString(), "/").size <= 2) {
                    it.insert(it.length - 1, "/")
                }
            }
        }

        mView!!.et_cvv.transformationMethod = AsteriskPasswordTransformationMethod()
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
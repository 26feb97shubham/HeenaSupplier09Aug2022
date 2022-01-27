package com.heena.supplier.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.heena.supplier.R
import com.heena.supplier.extras.AsteriskPasswordTransformationMethod
import com.heena.supplier.models.AddDeleteCardResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_add_new_card.*
import kotlinx.android.synthetic.main.fragment_add_new_card.et_card_number
import kotlinx.android.synthetic.main.fragment_add_new_card.et_card_title
import kotlinx.android.synthetic.main.fragment_add_new_card.et_cvv
import kotlinx.android.synthetic.main.fragment_add_new_card.et_expiry
import kotlinx.android.synthetic.main.fragment_add_new_card.ll_card
import kotlinx.android.synthetic.main.fragment_add_new_card.tv_account_holdee_name_1
import kotlinx.android.synthetic.main.fragment_add_new_card.tv_card_number_1
import kotlinx.android.synthetic.main.fragment_add_new_card.tv_expiry_date_1
import kotlinx.android.synthetic.main.fragment_add_new_card.tv_save_card
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddNewCardActivity : AppCompatActivity() {
    var card_title = ""
    var card_number = ""
    var card_cvv = ""
    var card_expiry = ""
    val sdf = SimpleDateFormat("MM/yyyy", Locale.US)
    var expiry_date : Date?=null
    var expired = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.changeLanguage(
            this,
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        setContentView(R.layout.activity_add_new_card)
        tv_save_card.setSafeOnClickListener {
            card_expiry = et_expiry.text.toString().trim()
            if (card_expiry.equals("")){
                LogUtils.shortToast(this, getString(R.string.please_enter_valid_expiry_date))
            }else{
                expiry_date=sdf.parse(card_expiry)
                expired= expiry_date!!.before(Date())
                validateAndSave()
            }
        }
        addTextWatcher()

        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "").equals("en")){
            val drawable = this.resources.getDrawable(R.drawable.card_bg)
            ll_card.background = drawable
        }else{
            val drawable = this.resources.getDrawable(R.drawable.arabic_card)
            ll_card.background = drawable
        }
    }

    private fun validateAndSave() {
        card_title = et_card_title.text.toString().trim()
        card_number = et_card_number.text.toString().trim()
        card_cvv = et_cvv.text.toString().trim()
        card_expiry = et_expiry.text.toString().trim()

        if (TextUtils.isEmpty(card_title)){
            et_card_title.requestFocus()
            et_card_title.error = getString(R.string.please_enter_valid_card_title)
        }else if (TextUtils.isEmpty(card_number)){
            et_card_number.requestFocus()
            et_card_number.error = getString(R.string.please_enter_valid_card_number)
        }else if (card_number.length<15){
            et_card_number.requestFocus()
            et_card_number.error = getString(R.string.please_enter_valid_card_number)
        }else if (TextUtils.isEmpty(card_cvv)){
            et_cvv.requestFocus()
            et_cvv.error = getString(R.string.please_enter_valid_cvv)
        }else if (card_cvv.length<3){
            et_cvv.requestFocus()
            et_cvv.error = getString(R.string.please_enter_valid_cvv)
        }else if (expired){
            LogUtils.shortToast(this, getString(R.string.card_is_expired))
        }else{
            save()
        }
    }

    private fun save() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        progressBar_add_card_activity.visibility= View.VISIBLE
        val builder = APIClient.createBuilder(arrayOf("user_id", "name", "number","cvv", "expiry_date", "type", "card_id"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(),
                card_title,
                card_number,
                card_cvv,
                card_expiry, "0", ""))
        val call = Utility.apiInterface.addDeleteCard(builder.build())
        call!!.enqueue(object : Callback<AddDeleteCardResponse?> {
            override fun onResponse(
                call: Call<AddDeleteCardResponse?>,
                response: Response<AddDeleteCardResponse?>
            ) {
                progressBar_add_card_activity.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            LogUtils.longToast(this@AddNewCardActivity, response.body()!!.message)
                            finish()
                        }
                    }else{
                        LogUtils.longToast(this@AddNewCardActivity, getString(R.string.response_isnt_successful))
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
                LogUtils.shortToast(this@AddNewCardActivity, getString(R.string.check_internet))
                progressBar_add_card_activity.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    companion object {
        private var instance: SharedPreferenceUtility? = null

        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }


    private fun addTextWatcher() {
        et_card_title.doOnTextChanged { text, _, _, _ ->
            tv_account_holdee_name_1.text = text
        }

        et_card_number.doOnTextChanged { text, _, _, _ ->
            tv_card_number_1.text = text
        }

        et_expiry.doOnTextChanged { text, _, _, _ ->
            tv_expiry_date_1.text = text
        }

        et_expiry.doAfterTextChanged {
            if (it!!.isNotEmpty() && it.length == 3) {
                val c: Char = it[it.length - 1]
                if ('/' == c) {
                    it.delete(it.length - 1, it.length)
                }
            }
            if (it.isNotEmpty() && it.length == 3) {
                val c: Char = it[it.length - 1]
                if (Character.isDigit(c) && TextUtils.split(it.toString(), "/").size <= 2) {
                    it.insert(it.length - 1, "/")
                }
            }
        }

        et_cvv.transformationMethod = AsteriskPasswordTransformationMethod()
    }
}
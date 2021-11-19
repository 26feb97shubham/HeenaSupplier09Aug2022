package com.dev.heenasupplier.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.fragment_add_new_card.*

class AddNewCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_card)
        tv_save_card.setOnClickListener {
            startActivity(Intent(this,PaymentFragmentActivity::class.java))
        }


        et_card_title.doOnTextChanged { text, start, before, count ->   tv_account_holdee_name.text = text }
        et_card_number.doOnTextChanged { text, start, before, count ->   tv_account_holdee_name.text = text }
        et_expiry.doOnTextChanged { text, start, before, count ->
            if(count == 2){
                et_expiry.setText("/")
            }
            tv_account_holdee_name.text = text }
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
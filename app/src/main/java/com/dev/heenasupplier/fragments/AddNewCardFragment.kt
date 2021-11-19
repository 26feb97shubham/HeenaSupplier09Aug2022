package com.dev.heenasupplier.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_add_new_card.view.*

class AddNewCardFragment : Fragment() {
    var mView : View ?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(
                R.layout.fragment_add_new_card, container, false)
        setUpViews()
        return mView
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setUpViews() {
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        mView!!.tv_save_card.setOnClickListener {
            findNavController().navigate(R.id.action_addNewCardFragment_to_paymentFragment)
        }

        mView!!.et_card_title.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mView!!.tv_account_holdee_name.text = s
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        mView!!.et_card_number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mView!!.tv_card_number.text = s
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        mView!!.et_expiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mView!!.tv_expiry_date.text = s
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "").equals("en")){
            val drawable = requireContext().resources.getDrawable(R.drawable.card_bg)
            mView!!.ll_card.background = drawable
        }else{
            val drawable = requireContext().resources.getDrawable(R.drawable.arabic_card)
            mView!!.ll_card.background = drawable
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
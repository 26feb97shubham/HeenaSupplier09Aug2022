package com.dev.heenasupplier.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.dev.heenasupplier.R
import com.dev.heenasupplier.extras.AsteriskPasswordTransformationMethod
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
            findNavController().navigate(R.id.myCardsFragment)
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

    private fun addTextWatcher(){
        mView!!.et_card_title.doOnTextChanged { text, start, before, count ->
            mView!!.tv_account_holdee_name_1.text = text
        }

        mView!!.et_card_number.doOnTextChanged { text, start, before, count ->
            mView!!.tv_card_number_1.text = text
        }

        mView!!.et_expiry.doOnTextChanged { text, start, before, count ->
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